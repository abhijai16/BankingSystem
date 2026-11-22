package com.abhijai.banking.service;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.TransactionRecord;
import com.abhijai.banking.domain.TransactionType;
import com.abhijai.banking.domain.Transfer;
import com.abhijai.banking.domain.TransferStatus;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.repository.BankingRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public final class TransactionService {
    private static final Pattern REFERENCE = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private final BankingRepository repository;
    private final BankingConfig config;

    public TransactionService(BankingRepository repository, BankingConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public TransactionRecord deposit(String customerId, String accountNumber, Money amount) {
        checkAmount(amount);
        return repository.update(state -> {
            BankAccount account = state.requireOwnedAccount(customerId, accountNumber);
            account.deposit(amount);
            return record(state, account, TransactionType.DEPOSIT, amount, Instant.now(), null);
        });
    }

    public TransactionRecord withdraw(String customerId, String accountNumber, Money amount) {
        checkAmount(amount);
        return repository.update(state -> {
            BankAccount account = state.requireOwnedAccount(customerId, accountNumber);
            account.withdraw(amount);
            return record(state, account, TransactionType.WITHDRAWAL, amount.negate(), Instant.now(), null);
        });
    }

    public Transfer transfer(String customerId, String from, String to, Money amount, String reference) {
        if (!REFERENCE.matcher(reference).matches()) {
            throw new BankingException("A transfer reference must be 1 to 64 letters, digits, dots, hyphens or underscores.");
        }
        return repository.update(state -> {
            BankAccount source = state.requireOwnedAccount(customerId, from);
            Transfer existing = state.getTransfers().get(reference);
            if (existing != null) {
                if (from.equals(existing.getSourceAccountNumber()) && to.equals(existing.getDestinationAccountNumber())
                        && amount.equals(existing.getAmount())) {
                    return existing;
                }
                throw new BankingException("That reference belongs to a different transfer.");
            }
            checkAmount(amount);
            BankAccount destination = state.requireAccount(to);
            Instant now = Instant.now();
            Transfer transfer = new Transfer(reference, from, to, amount, now, TransferStatus.COMPLETED);
            source.withdraw(amount);
            destination.deposit(amount);
            state.addTransfer(transfer);
            record(state, source, TransactionType.TRANSFER_OUT, amount.negate(), now, reference);
            record(state, destination, TransactionType.TRANSFER_IN, amount, now, reference);
            return transfer;
        });
    }

    private void checkAmount(Money amount) {
        if (amount.compareTo(config.getMinimumTransactionAmount()) < 0
                || amount.compareTo(config.getMaximumTransactionAmount()) > 0) {
            throw new BankingException("Enter an amount between " + config.getMinimumTransactionAmount()
                    + " and " + config.getMaximumTransactionAmount() + ".");
        }
    }

    static TransactionRecord record(BankingState state, BankAccount account, TransactionType type,
                                    Money signedAmount, Instant time, String reference) {
        TransactionRecord entry = new TransactionRecord(UUID.randomUUID().toString(), account.getAccountNumber(),
                type, signedAmount, account.getBalance(), time, reference);
        state.addTransaction(entry);
        return entry;
    }
}
