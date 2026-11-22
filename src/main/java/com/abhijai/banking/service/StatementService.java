package com.abhijai.banking.service;

import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.domain.TransactionRecord;
import com.abhijai.banking.repository.BankingRepository;

import java.util.List;

public final class StatementService {
    private final BankingRepository repository;

    public StatementService(BankingRepository repository) {
        this.repository = repository;
    }

    public Statement statement(String customerId, String accountNumber) {
        BankingState state = repository.load();
        BankAccount account = state.requireOwnedAccount(customerId, accountNumber);
        return new Statement(account, state.transactionsFor(accountNumber));
    }

    public record Statement(BankAccount account, List<TransactionRecord> entries) {}
}
