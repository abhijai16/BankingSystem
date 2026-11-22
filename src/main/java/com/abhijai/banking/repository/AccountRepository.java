package com.abhijai.banking.repository;

import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.exception.AccountNotFoundException;

import java.util.Map;

public interface AccountRepository {
    Map<String, BankAccount> getAccounts();

    default BankAccount requireAccount(String accountNumber) {
        BankAccount account = getAccounts().get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    default BankAccount requireOwnedAccount(String customerId, String accountNumber) {
        BankAccount account = requireAccount(accountNumber);
        if (!account.getOwnerCustomerId().equals(customerId)) {
            throw new AccountNotFoundException();
        }
        return account;
    }
}
