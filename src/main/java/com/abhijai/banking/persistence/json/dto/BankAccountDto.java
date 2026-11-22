package com.abhijai.banking.persistence.json.dto;

import com.abhijai.banking.domain.AccountStatus;
import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.Money;

import java.time.Instant;

public record BankAccountDto(String accountNumber, String ownerCustomerId, Money balance,
                             AccountStatus status, String openedAt, String closedAt) {
    static BankAccountDto from(BankAccount account) {
        Instant closed = account.getClosedAt();
        return new BankAccountDto(account.getAccountNumber(), account.getOwnerCustomerId(),
                account.getBalance(), account.getStatus(), account.getOpenedAt().toString(),
                closed == null ? null : closed.toString());
    }

    BankAccount toDomain() {
        return BankAccount.restore(accountNumber, ownerCustomerId, balance, status,
                Instant.parse(openedAt), closedAt == null ? null : Instant.parse(closedAt));
    }
}
