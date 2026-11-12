package com.abhijai.banking.repository;

import com.abhijai.banking.domain.BankingState;

public interface BankingRepository {

    BankingState load();

    void save(BankingState state);
}