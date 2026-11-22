package com.abhijai.banking.domain;

import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.repository.AccountRepository;
import com.abhijai.banking.repository.CustomerRepository;
import com.abhijai.banking.repository.TransactionRepository;

import java.util.*;

public final class BankingState implements CustomerRepository, AccountRepository, TransactionRepository {
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

    public static BankingState empty() {
        return new BankingState(1, Map.of(), Map.of(), List.of(), Map.of());
    }

    public void addCustomer(Customer customer) {
        if (findByLogin(customer.getLoginId()).isPresent()
                || customers.putIfAbsent(customer.getCustomerId(), customer) != null) {
            throw new BankingException("That customer or login ID already exists.");
        }
    }

    public void addAccount(BankAccount account) {
        if (accounts.containsKey(account.getAccountNumber())) {
            throw new BankingException("Account number is already in use.");
        }
        requireCustomer(account.getOwnerCustomerId()).addAccount(account);
        accounts.put(account.getAccountNumber(), account);
    }

    public void addTransfer(Transfer transfer) {
        requireAccount(transfer.getSourceAccountNumber());
        requireAccount(transfer.getDestinationAccountNumber());
        if (transfers.putIfAbsent(transfer.getTransferReference(), transfer) != null) {
            throw new BankingException("Transfer reference is already in use.");
        }
    }

    public void addTransaction(TransactionRecord transaction) {
        requireAccount(transaction.getAccountNumber());
        transactions.add(transaction);
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
