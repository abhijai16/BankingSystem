package com.abhijai.banking.service;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.TransactionType;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.repository.BankingRepository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class AccountService {
    private final BankingRepository repository;
    private final BankingConfig config;

    public AccountService(BankingRepository repository, BankingConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public BankAccount open(String customerId, Money deposit) {
        if (deposit.compareTo(config.getMinimumOpeningDeposit()) < 0) {
            throw new BankingException("The minimum opening deposit is " + config.getMinimumOpeningDeposit() + ".");
        }
        return repository.update(state -> {
            state.requireCustomer(customerId);
            String number;
            do {
                number = Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
            } while (state.getAccounts().containsKey(number));
            BankAccount account = new BankAccount(number, customerId, deposit);
            state.addAccount(account);
            TransactionService.record(state, account, TransactionType.DEPOSIT, deposit, account.getOpenedAt(), null);
            return account;
        });
    }

    public List<BankAccount> accounts(String customerId) {
        return repository.load().requireCustomer(customerId).getAccounts().stream()
                .sorted(Comparator.comparing(BankAccount::getOpenedAt).thenComparing(BankAccount::getAccountNumber))
                .toList();
    }

    public void close(String customerId, String accountNumber) {
        repository.update(state -> {
            state.requireOwnedAccount(customerId, accountNumber).closeAccount();
            return null;
        });
    }
}
