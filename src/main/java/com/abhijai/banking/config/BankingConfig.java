package com.abhijai.banking.config;

import com.abhijai.banking.domain.Money;

import java.math.BigDecimal;
import java.util.Objects;

public final class BankingConfig{
    private final Money minimumOpeningDeposit;
    private final Money minimumTransactionAmount;
    private final Money maximumTransactionAmount;
    private final int maximumLoginAttempts;
    private final long loginCooldownMinutes;

    public BankingConfig(
            Money minimumOpeningDeposit,
            Money minimumTransactionAmount,
            Money maximumTransactionAmount,
            int maximumLoginAttempts,
            long loginCooldownMinutes) {

        Objects.requireNonNull(minimumOpeningDeposit, "Minimum opening deposit cannot be null.");
        Objects.requireNonNull(minimumTransactionAmount, "Minimum transaction amount cannot be null.");
        Objects.requireNonNull(maximumTransactionAmount, "Maximum transaction amount cannot be null.");

        if(minimumTransactionAmount.compareTo(maximumTransactionAmount) > 0){
            throw new IllegalArgumentException("Minimum transaction amount cannot be greater than maximum transaction amount.");
        }

        if(minimumOpeningDeposit.compareTo(new Money(new BigDecimal("500"),"INR")) < 0) {
            throw new IllegalArgumentException("Minimum opening deposit must be atleast ₹500");
        }

        if(maximumLoginAttempts <= 0){
            throw new IllegalArgumentException("Maximum login attempts must be positive.");
        }

        if(loginCooldownMinutes <= 0){
            throw new IllegalArgumentException("Login cooldown minutes must be positive.");
        }

        if(!minimumTransactionAmount.isPositive()){
            throw new IllegalArgumentException("Minimum transaction amount must be positive");
        }

        if(!maximumTransactionAmount.isPositive()){
            throw new IllegalArgumentException("Maximum transaction amount must be positive");
        }

        this.minimumOpeningDeposit = minimumOpeningDeposit;
        this.minimumTransactionAmount = minimumTransactionAmount;
        this.maximumTransactionAmount = maximumTransactionAmount;
        this.maximumLoginAttempts = maximumLoginAttempts;
        this.loginCooldownMinutes = loginCooldownMinutes;
    }

    public Money getMinimumOpeningDeposit() {
        return minimumOpeningDeposit;
    }

    public Money getMinimumTransactionAmount() {
        return minimumTransactionAmount;
    }

    public Money getMaximumTransactionAmount() {
        return maximumTransactionAmount;
    }

    public int getMaximumLoginAttempts() {
        return maximumLoginAttempts;
    }

    public long getLoginCooldownMinutes() {
        return loginCooldownMinutes;
    }
}