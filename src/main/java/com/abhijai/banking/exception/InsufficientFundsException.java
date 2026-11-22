package com.abhijai.banking.exception;

public final class InsufficientFundsException extends BankingException {
    public InsufficientFundsException() {
        super("Insufficient balance");
    }
}
