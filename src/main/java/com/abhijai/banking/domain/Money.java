package com.abhijai.banking.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency){
        // Null values are not accepted.
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        // INR and inr both are accepted.
        currency = currency.toUpperCase(Locale.ROOT);

        // For this version only currency type indian is only accepted.
        if(!currency.equals("INR")){
            throw new IllegalArgumentException("Only INR is supported");
        }

        // Amount can be only upto 2 decimal place.
        this.amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money add(Money other){
        Objects.requireNonNull(other, "Money cannot be null");

        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Cannot add different currencies");
        }

        BigDecimal sum = this.amount.add(other.amount);

        return new Money(sum, this.currency);
    }

    public Money subtract(Money other){
        Objects.requireNonNull(other, "Money cannot be null");

        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }

        BigDecimal difference = this.amount.subtract(other.amount);

        return new Money(difference, this.currency);
    }

    public int compareTo(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");

        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Cannot compare different currencies");
        }

        return this.amount.compareTo(other.amount);
    }

    public boolean isPositive(){
        return this.amount.compareTo(BigDecimal.ZERO) > 0;

    }

    public boolean isZero(){
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public Money negate(){
        return new Money(this.amount.negate(), this.currency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Money)) {
            return false;
        }

        Money other = (Money) obj;

        return this.amount.equals(other.amount) && this.currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount, this.currency);
    }

    @Override
    public String toString() {
        return this.currency + " " + this.amount.toPlainString();
    }
}
