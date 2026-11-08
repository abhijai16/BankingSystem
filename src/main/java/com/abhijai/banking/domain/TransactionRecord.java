
package com.abhijai.banking.domain;

import java.time.Instant;
import java.util.Objects;

public final class TransactionRecord {

    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final Money amount;
    private final Money resultingBalance;
    private final Instant timestamp;
    private final String transferReference;

    public TransactionRecord(
            String transactionId,
            String accountNumber,
            TransactionType type,
            Money amount,
            Money resultingBalance,
            Instant timestamp,
            String transferReference) {

        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(accountNumber, "Account number cannot be null");
        Objects.requireNonNull(type, "Transaction type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(resultingBalance, "Resulting balance cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if(transactionId.isBlank()){
            throw new IllegalArgumentException("Transaction ID cannot be blank");
        }

        if(accountNumber.isBlank()){
            throw new IllegalArgumentException("Account number cannot be blank");
        }

        if(amount.isZero()){
            throw new IllegalArgumentException("Transaction amount cannot be zero");
        }

        if(resultingBalance.isNegative()){
            throw new IllegalArgumentException("Resulting balance cannot be negative");
        }

        boolean isTransfer = type == TransactionType.TRANSFER_IN || type == TransactionType.TRANSFER_OUT;

        if(isTransfer){
            if(transferReference == null || transferReference.isBlank()){
                throw new IllegalArgumentException(
                        "Transfer reference is required for transfers");
            }
        }
        else if(transferReference != null){
            throw new IllegalArgumentException(
                    "Non-transfer transactions cannot have a transfer reference");
        }

        if((type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN) && !amount.isPositive()){
            throw new IllegalArgumentException(
                    "This transaction type requires a positive amount");
        }

        if((type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER_OUT) && amount.isPositive()){
            throw new IllegalArgumentException(
                    "This transaction type requires a negative amount");
        }

        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.timestamp = timestamp;
        this.transferReference = transferReference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getResultingBalance() {
        return resultingBalance;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTransferReference() {
        return transferReference;
    }
}
