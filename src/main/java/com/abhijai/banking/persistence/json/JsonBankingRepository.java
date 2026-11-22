package com.abhijai.banking.persistence.json;

import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.repository.BankingRepository;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public final class JsonBankingRepository implements BankingRepository {
    // File locks coordinate processes. The monitor also queues callers inside this JVM.
    private static final Object JVM_LOCK = new Object();
    private final Path directory;
    private final Path primary;
    private final BackupManager backups;
    private final SnapshotManager snapshots = new SnapshotManager();

    public JsonBankingRepository(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        this.primary = this.directory.resolve("banking.json");
        this.backups = new BackupManager(this.directory);
    }

    @Override
    public BankingState load() {
        return locked(() -> readCurrent().state());
    }

    @Override
    public void save(BankingState state) {
        Objects.requireNonNull(state, "State cannot be null");
        locked(() -> {
            commit(readCurrent().bytes(), state);
            return null;
        });
    }

    @Override
    public <T> T update(Function<BankingState, T> operation) {
        return locked(() -> {
            Snapshot current = readCurrent();
            T result = operation.apply(current.state());
            commit(current.bytes(), current.state());
            return result;
        });
    }

    public void restoreBackup() {
        locked(() -> {
            if (!backups.exists()) {
                throw new IllegalStateException("No previous snapshot is available in " + directory);
            }
            backups.restore(primary, snapshots);
            return null;
        });
    }

    private Snapshot readCurrent() throws IOException {
        if (Files.notExists(primary)) {
            if (backups.exists()) {
                throw new IllegalStateException("The main snapshot is missing. Recover it with --restore-backup.");
            }
            return new Snapshot(null, BankingState.empty());
        }
        byte[] bytes = Files.readAllBytes(primary);
        try {
            return new Snapshot(bytes, snapshots.decode(bytes));
        } catch (IOException invalidData) {
            throw new IllegalStateException("Cannot read banking.json. Check your copy or use --restore-backup.", invalidData);
        }
    }

    private void commit(byte[] previous, BankingState updated) throws IOException {
        byte[] next = snapshots.encode(updated);
        if (Arrays.equals(previous, next)) {
            return;
        }
        if (previous != null) {
            backups.keepPrevious(previous);
        }
        SnapshotManager.writeAtomically(primary, next);
    }

    private <T> T locked(IOOperation<T> operation) {
        synchronized (JVM_LOCK) {
            try {
                Files.createDirectories(directory);
                try (FileChannel channel = FileChannel.open(directory.resolve("banking.lock"), CREATE, WRITE);
                     FileLock lock = channel.lock()) {
                    return operation.run();
                }
            } catch (IOException error) {
                throw new IllegalStateException("Storage operation failed in " + directory + " (" + error.getMessage() + ").", error);
            }
        }
    }

    private record Snapshot(byte[] bytes, BankingState state) {}

    @FunctionalInterface
    private interface IOOperation<T> {
        T run() throws IOException;
    }
}
