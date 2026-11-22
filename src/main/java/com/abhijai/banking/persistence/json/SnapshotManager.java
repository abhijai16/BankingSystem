package com.abhijai.banking.persistence.json;

import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.persistence.json.dto.BankingStateDto;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

final class SnapshotManager {
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();

    BankingState decode(byte[] bytes) throws IOException {
        try {
            return mapper.readValue(bytes, BankingStateDto.class).toDomain();
        } catch (RuntimeException invalidData) {
            throw new IOException("Snapshot contains inconsistent banking data.", invalidData);
        }
    }

    byte[] encode(BankingState state) throws IOException {
        BankingStateDto dto = BankingStateDto.from(state);
        dto.toDomain(); // Check balances and both sides of every transfer before publishing.
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dto);
    }

    static void writeAtomically(Path target, byte[] data) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), ".banking-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            Files.move(temporary, target, ATOMIC_MOVE, REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
