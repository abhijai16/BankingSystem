package com.abhijai.banking.persistence.json.dto;

import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.TransactionRecord;
import com.abhijai.banking.domain.TransactionType;
import com.abhijai.banking.domain.Transfer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public record BankingStateDto(int schemaVersion, List<CustomerDto> customers,
                             List<BankAccountDto> accounts, List<TransactionRecordDto> transactions,
                             List<TransferDto> transfers) {
    public static BankingStateDto from(BankingState state) {
        return new BankingStateDto(state.getSchemaVersion(),
                new TreeMap<>(state.getCustomers()).values().stream().map(CustomerDto::from).toList(),
                new TreeMap<>(state.getAccounts()).values().stream().map(BankAccountDto::from).toList(),
                state.getTransactions().stream().map(TransactionRecordDto::from).toList(),
                new TreeMap<>(state.getTransfers()).values().stream().map(TransferDto::from).toList());
    }

    public BankingState toDomain() {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException("Unsupported schema version " + schemaVersion);
        }
        BankingState state = BankingState.empty();
        customers.forEach(dto -> state.addCustomer(dto.toDomain()));
        accounts.forEach(dto -> state.addAccount(dto.toDomain()));
        transfers.forEach(dto -> state.addTransfer(dto.toDomain()));
        transactions.forEach(dto -> state.addTransaction(dto.toDomain()));
        reconcile(state);
        return state;
    }

    private static void reconcile(BankingState state) {
        Map<String, Money> balances = new HashMap<>();
        Set<String> transactionIds = new HashSet<>();
        Set<String> incoming = new HashSet<>();
        Set<String> outgoing = new HashSet<>();
        Money zero = new Money(BigDecimal.ZERO, "INR");
        for (TransactionRecord entry : state.getTransactions()) {
            String account = entry.getAccountNumber();
            Money next = balances.getOrDefault(account, zero).add(entry.getAmount());
            if (!transactionIds.add(entry.getTransactionId()) || !next.equals(entry.getResultingBalance())) {
                throw new IllegalArgumentException("Inconsistent statement for " + account);
            }
            balances.put(account, next);
            if (entry.getTransferReference() != null) {
                verifyTransferEntry(state, entry, incoming, outgoing);
            }
        }
        for (BankAccount account : state.getAccounts().values()) {
            if (!account.getBalance().equals(balances.get(account.getAccountNumber()))) {
                throw new IllegalArgumentException("Account balance does not match its statement");
            }
        }
        if (!incoming.equals(state.getTransfers().keySet()) || !outgoing.equals(incoming)) {
            throw new IllegalArgumentException("A transfer is missing a ledger entry");
        }
    }

    private static void verifyTransferEntry(BankingState state, TransactionRecord entry,
                                            Set<String> incoming, Set<String> outgoing) {
        String reference = entry.getTransferReference();
        Transfer transfer = state.getTransfers().get(reference);
        if (transfer == null || !transfer.getTimestamp().equals(entry.getTimestamp())) {
            throw new IllegalArgumentException("Unknown or inconsistent transfer reference");
        }
        boolean credit = entry.getType() == TransactionType.TRANSFER_IN;
        String account = credit ? transfer.getDestinationAccountNumber() : transfer.getSourceAccountNumber();
        Money amount = credit ? transfer.getAmount() : transfer.getAmount().negate();
        Set<String> seen = credit ? incoming : outgoing;
        if (!seen.add(reference) || !account.equals(entry.getAccountNumber()) || !amount.equals(entry.getAmount())) {
            throw new IllegalArgumentException("Transfer does not match its ledger entries");
        }
    }
}
