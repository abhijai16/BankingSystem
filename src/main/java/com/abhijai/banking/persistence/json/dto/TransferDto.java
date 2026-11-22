package com.abhijai.banking.persistence.json.dto;

import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.Transfer;
import com.abhijai.banking.domain.TransferStatus;

import java.time.Instant;

public record TransferDto(String transferReference, String sourceAccountNumber,
                          String destinationAccountNumber, Money amount, String timestamp,
                          TransferStatus status) {
    static TransferDto from(Transfer transfer) {
        return new TransferDto(transfer.getTransferReference(), transfer.getSourceAccountNumber(),
                transfer.getDestinationAccountNumber(), transfer.getAmount(),
                transfer.getTimestamp().toString(), transfer.getStatus());
    }

    Transfer toDomain() {
        return new Transfer(transferReference, sourceAccountNumber, destinationAccountNumber, amount,
                Instant.parse(timestamp), status);
    }
}
