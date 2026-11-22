package com.abhijai.banking.domain;

import com.abhijai.banking.exception.InsufficientFundsException;

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
        this(accountNumber, ownerCustomerId, requireOpeningDeposit(openingDeposit),
                AccountStatus.ACTIVE, Instant.now(), null);
    }

    private static Money requireOpeningDeposit(Money openingDeposit) {
        Objects.requireNonNull(openingDeposit, "Opening Deposit cannot be null");
        if (openingDeposit.compareTo(MINIMUM_OPENING_DEPOSIT) < 0) {
            throw new IllegalArgumentException("Minimum opening deposit must be ₹500");
        }
        return openingDeposit;
    }

    public static BankAccount restore(String accountNumber, String ownerCustomerId, Money balance,
                                      AccountStatus status, Instant openedAt, Instant closedAt) {
        return new BankAccount(accountNumber, ownerCustomerId, balance, status, openedAt, closedAt);
    }

    private BankAccount(String accountNumber, String ownerCustomerId, Money balance,
                        AccountStatus status, Instant openedAt, Instant closedAt) {
        Objects.requireNonNull(accountNumber, "Account number cannot be null");
        Objects.requireNonNull(ownerCustomerId, "Owner Customer Id cannot be null");
        Objects.requireNonNull(balance, "Balance cannot be null");
        Objects.requireNonNull(status, "Account status cannot be null");
        Objects.requireNonNull(openedAt, "Opening time cannot be null");

        if(accountNumber.isBlank()){
            throw new IllegalArgumentException("Account number cannot be blank");
        }

        if(ownerCustomerId.isBlank()){
            throw new IllegalArgumentException("Owner Customer ID cannot be blank");
        }

        if (balance.isNegative() || (status == AccountStatus.CLOSED && !balance.isZero())) {
            throw new IllegalArgumentException("Invalid account balance");
        }
        if ((status == AccountStatus.CLOSED) != (closedAt != null)
                || (closedAt != null && closedAt.isBefore(openedAt))) {
            throw new IllegalArgumentException("Invalid account closure date");
        }
        this.accountNumber = accountNumber;
        this.ownerCustomerId = ownerCustomerId;
        this.balance = balance;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;

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
            throw new InsufficientFundsException();

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
