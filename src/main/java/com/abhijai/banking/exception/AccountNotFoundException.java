package com.abhijai.banking.exception;

public final class AccountNotFoundException extends BankingException {
    public AccountNotFoundException() {
        super("Account unavailable. Check the account number.");
    }
}
