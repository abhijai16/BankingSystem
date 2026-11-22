package com.abhijai.banking.exception;

/** A banking operation was rejected because one of its rules was violated. */
public class BankingException extends IllegalArgumentException {
    public BankingException(String message) {
        super(message);
    }
}
