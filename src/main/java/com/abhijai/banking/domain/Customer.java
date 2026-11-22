package com.abhijai.banking.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Customer {
    private final String customerId;
    private String loginId;
    private String passwordHash;
    private final Instant createdAt;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    private final List<BankAccount> accounts;

    public Customer(String customerId, String loginId, String passwordHash){
        this(customerId, loginId, passwordHash, Instant.now(), 0, null);
    }

    public static Customer restore(String customerId, String loginId, String passwordHash,
                                   Instant createdAt, int failedLoginAttempts, Instant lockedUntil) {
        return new Customer(customerId, loginId, passwordHash, createdAt, failedLoginAttempts, lockedUntil);
    }

    private Customer(String customerId, String loginId, String passwordHash,
                     Instant createdAt, int failedLoginAttempts, Instant lockedUntil) {
        Objects.requireNonNull(customerId, "Customer Id cannot be null");
        Objects.requireNonNull(loginId, "Login Id cannot be null");
        Objects.requireNonNull(passwordHash, "Password Hash cannot be null");

        if(customerId.isBlank()){
            throw new IllegalArgumentException("Customer Id cannot be blank");
        }

        if(loginId.isBlank()){
            throw new IllegalArgumentException("Login Id cannot be blank");
        }

        if(passwordHash.isBlank()){
            throw new IllegalArgumentException("Password Hash cannot be blank");
        }

        if (failedLoginAttempts < 0 || (lockedUntil != null && failedLoginAttempts == 0)) {
            throw new IllegalArgumentException("Invalid login attempt count");
        }
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time cannot be null");
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.customerId = customerId;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.accounts = new ArrayList<>();
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public String getLoginId() {
        return this.loginId;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isLoginLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    public void recordFailedLogin(Instant now, int limit, long cooldownMinutes) {
        if (isLoginLocked(now)) {
            return;
        }
        if (lockedUntil != null) {
            clearLoginFailures();
        }
        if (++failedLoginAttempts >= limit) {
            lockedUntil = now.plus(cooldownMinutes, ChronoUnit.MINUTES);
        }
    }

    public void clearLoginFailures() {
        failedLoginAttempts = 0;
        lockedUntil = null;
    }

    public List<BankAccount> getAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(accounts));
    }

    public void addAccount(BankAccount account) {
        Objects.requireNonNull(account, "Account cannot be null");

        if(!this.customerId.equals(account.getOwnerCustomerId())){
            throw new IllegalArgumentException(
                    "This account does not belong to this customer."
            );
        }

        for(BankAccount existingAccount : accounts){
            if(existingAccount.getAccountNumber().equals(account.getAccountNumber())){
                throw new IllegalArgumentException(
                        "Account is already associated with this customer."
                );
            }
        }

        this.accounts.add(account);
    }

    public void changeLoginId(String newLoginId){
        Objects.requireNonNull(newLoginId, "Login ID cannot be null");

        if(newLoginId.isBlank()){
            throw new IllegalArgumentException("Login ID cannot be blank");
        }

        this.loginId = newLoginId;
    }

    public void changePasswordHash(String newPasswordHash){
        Objects.requireNonNull(newPasswordHash, "Password hash cannot be null");

        if(newPasswordHash.isBlank()){
            throw new IllegalArgumentException("Password hash cannot be blank");
        }

        this.passwordHash = newPasswordHash;
    }
}
