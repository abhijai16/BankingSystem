package com.abhijai.banking.persistence.json.dto;

import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.TransactionRecord;
import com.abhijai.banking.domain.TransactionType;

import java.time.Instant;

public record TransactionRecordDto(String transactionId, String accountNumber, TransactionType type,
                                   Money amount, Money resultingBalance, String timestamp,
                                   String transferReference) {
    static TransactionRecordDto from(TransactionRecord record) {
        return new TransactionRecordDto(record.getTransactionId(), record.getAccountNumber(),
                record.getType(), record.getAmount(), record.getResultingBalance(),
                record.getTimestamp().toString(), record.getTransferReference());
    }

    TransactionRecord toDomain() {
        return new TransactionRecord(transactionId, accountNumber, type, amount, resultingBalance,
                Instant.parse(timestamp), transferReference);
    }
}
