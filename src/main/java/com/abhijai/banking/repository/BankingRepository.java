package com.abhijai.banking.repository;

import com.abhijai.banking.domain.BankingState;

import java.util.function.Function;

public interface BankingRepository {
    BankingState load();

    void save(BankingState state);

    /** Apply a change to a fresh snapshot and commit it only if the operation returns normally. */
    <T> T update(Function<BankingState, T> operation);
}
