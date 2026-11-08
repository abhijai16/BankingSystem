package com.abhijai.banking.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class BankAccount {

    private static final Money MINIMUM_OPENING_DEPOSIT = new Money(new BigDecimal("500"), "INR");

    private final String accountNumber;
    private final String ownerCustomerId;

    private Money balance;
    private AccountStatus status;

    private final Instant openedAt;
    private Instant closedAt;

    public BankAccount(String accountNumber, String ownerCustomerId, Money openingDeposit){
        Objects.requireNonNull(accountNumber, "Account number cannot be null");
        Objects.requireNonNull(ownerCustomerId, "Owner Customer Id cannot be null");
        Objects.requireNonNull(openingDeposit, "Opening Deposit cannot be null");

        if(accountNumber.isBlank()){
            throw new IllegalArgumentException("Account number cannot be blank");
        }

        if(ownerCustomerId.isBlank()){
            throw new IllegalArgumentException("Owner Customer ID cannot be blank");
        }

        if(openingDeposit.compareTo(MINIMUM_OPENING_DEPOSIT) < 0){
            throw new IllegalArgumentException(
                    "Minimum opening deposit must be ₹500"
            );
        }
        this.accountNumber = accountNumber;
        this.ownerCustomerId = ownerCustomerId;
        this.balance = openingDeposit;
        this.status = AccountStatus.ACTIVE;
        this.openedAt = Instant.now();
        this.closedAt = null;

    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public String getOwnerCustomerId() {
        return this.ownerCustomerId;
    }

    public Money getBalance() {
        return this.balance;
    }

    public AccountStatus getStatus() {
        return this.status;
    }

    public Instant getOpenedAt() {
        return this.openedAt;
    }

    public Instant getClosedAt() {
        return this.closedAt;
    }

    public void deposit(Money amount){
        Objects.requireNonNull(amount, "Amount cannot be null");

        if(this.status == AccountStatus.CLOSED){
            throw new IllegalStateException("Cannot deposit into a closed account");
        }

        if(!amount.isPositive()){
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        this.balance = balance.add(amount);

    }

    public void withdraw(Money amount){
        Objects.requireNonNull(amount, "Amount cannot be null");

        if(this.status == AccountStatus.CLOSED){
            throw new IllegalStateException("Cannot withdraw from a closed account");
        }

        if(!amount.isPositive()){
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if(this.balance.compareTo(amount)<0){
            throw new IllegalArgumentException("Insufficient balance");

        }
        this.balance = this.balance.subtract(amount);
    }

    public void closeAccount(){
        if(this.status == AccountStatus.CLOSED){
            throw new IllegalStateException("Account is already closed");
        }

        if(!this.balance.isZero()){
            throw new IllegalStateException("Cannot close account. Please withdraw or transfer the remaining balance first.");
        }
        this.status = AccountStatus.CLOSED;
        this.closedAt = Instant.now();
    }
}
