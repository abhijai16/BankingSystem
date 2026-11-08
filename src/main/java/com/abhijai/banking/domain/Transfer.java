
package com.abhijai.banking.domain;

import java.time.Instant;
import java.util.Objects;

public final class Transfer {

    private final String transferReference;
    private final String sourceAccountNumber;
    private final String destinationAccountNumber;
    private final Money amount;
    private final Instant timestamp;
    private final TransferStatus status;

    public Transfer(
            String transferReference,
            String sourceAccountNumber,
            String destinationAccountNumber,
            Money amount,
            Instant timestamp,
            TransferStatus status) {

        Objects.requireNonNull(transferReference, "Transfer reference cannot be null");
        Objects.requireNonNull(sourceAccountNumber, "Source account cannot be null");
        Objects.requireNonNull(destinationAccountNumber, "Destination account cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        Objects.requireNonNull(status, "Transfer status cannot be null");

        if(transferReference.isBlank()){
            throw new IllegalArgumentException("Transfer reference cannot be blank");
        }

        if(sourceAccountNumber.isBlank()){
            throw new IllegalArgumentException("Source account cannot be blank");
        }

        if(destinationAccountNumber.isBlank()){
            throw new IllegalArgumentException("Destination account cannot be blank");
        }

        if(sourceAccountNumber.equals(destinationAccountNumber)){
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different");
        }

        if(!amount.isPositive()){
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        this.transferReference = transferReference;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTransferReference(){
        return transferReference;
    }

    public String getSourceAccountNumber(){
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber(){
        return destinationAccountNumber;
    }

    public Money getAmount(){
        return amount;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

    public TransferStatus getStatus(){
        return status;
    }
}
