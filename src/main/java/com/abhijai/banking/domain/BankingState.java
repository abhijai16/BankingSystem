package com.abhijai.banking.domain;

import java.util.*;

public final class BankingState {
    private final int schemaVersion;

    private final Map<String, Customer> customers;
    private final Map<String, BankAccount> accounts;
    private final List<TransactionRecord> transactions;
    private final Map<String, Transfer> transfers;

    public BankingState(
            int schemaVersion,
            Map<String, Customer> customers,
            Map<String, BankAccount> accounts,
            List<TransactionRecord> transactions,
            Map<String, Transfer> transfers
    ){
        Objects.requireNonNull(customers, "Customers map cannot be null");
        Objects.requireNonNull(accounts, "Accounts map cannot be null");
        Objects.requireNonNull(transactions, "Transactions list cannot be null");
        Objects.requireNonNull(transfers, "Transfers map cannot be null");

        if(schemaVersion != 1){
            throw new IllegalArgumentException("Unsupported schema version");
        }

        this.accounts = new HashMap<>(accounts);
        this.customers = new HashMap<>(customers);
        this.transactions = new ArrayList<>(transactions);
        this.transfers = new HashMap<>(transfers);
        this.schemaVersion = schemaVersion;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public Map<String, Customer> getCustomers() {
        return Collections.unmodifiableMap(customers);
    }

    public Map<String, BankAccount> getAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    public List<TransactionRecord> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Map<String, Transfer> getTransfers() {
        return Collections.unmodifiableMap(transfers);
    }
}
