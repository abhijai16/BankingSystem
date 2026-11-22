package com.abhijai.banking.repository;

import com.abhijai.banking.domain.BankingState;

import java.util.function.Function;

public interface BankingRepository {
    BankingState load();

    void save(BankingState state);

    <T> T update(Function<BankingState, T> operation);
}
