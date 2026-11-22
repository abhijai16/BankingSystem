package com.abhijai.banking.repository;

import com.abhijai.banking.domain.TransactionRecord;

import java.util.List;

public interface TransactionRepository {
    List<TransactionRecord> getTransactions();

    default List<TransactionRecord> transactionsFor(String accountNumber) {
        return getTransactions().stream()
                .filter(record -> record.getAccountNumber().equals(accountNumber))
                .toList();
    }
}
