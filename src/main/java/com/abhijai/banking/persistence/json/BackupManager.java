package com.abhijai.banking.persistence.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

final class BackupManager {
    private final Path backup;

    BackupManager(Path directory) {
        this.backup = directory.resolve("banking.json.bak");
    }

    boolean exists() {
        return Files.exists(backup);
    }

    void keepPrevious(byte[] previous) throws IOException {
        SnapshotManager.writeAtomically(backup, previous);
    }

    void restore(Path primary, SnapshotManager snapshots) throws IOException {
        byte[] previous = Files.readAllBytes(backup);
        snapshots.decode(previous);
        if (Files.exists(primary)) {
            byte[] current = Files.readAllBytes(primary);
            try {
                snapshots.decode(current);
            } catch (IOException damaged) {
                Path copy = primary.resolveSibling("banking.damaged-" + UUID.randomUUID() + ".json");
                SnapshotManager.writeAtomically(copy, current);
                SnapshotManager.writeAtomically(primary, previous);
                return;
            }
            throw new IllegalStateException("The current snapshot is valid. Recovery is unnecessary.");
        }
        SnapshotManager.writeAtomically(primary, previous);
    }
}
