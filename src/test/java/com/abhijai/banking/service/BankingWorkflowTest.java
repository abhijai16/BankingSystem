package com.abhijai.banking.service;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.config.BankingConfigLoader;
import com.abhijai.banking.domain.*;
import com.abhijai.banking.exception.AccountNotFoundException;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.exception.InsufficientFundsException;
import com.abhijai.banking.persistence.json.JsonBankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class BankingWorkflowTest {
    @TempDir Path directory;
    private final BankingConfig config = new BankingConfigLoader().load();
    private JsonBankingRepository repository;
    private AccountService accounts;
    private TransactionService transactions;
    private StatementService statements;
    private String source;
    private String destination;

    @BeforeEach
    void openTwoAccounts() {
        repository = new JsonBankingRepository(directory);
        accounts = new AccountService(repository, config);
        transactions = new TransactionService(repository, config);
        statements = new StatementService(repository);
        repository.update(state -> {
            state.addCustomer(new Customer("A", "alice", "fixture"));
            state.addCustomer(new Customer("B", "bob", "fixture"));
            return null;
        });
        source = accounts.open("A", money("1000")).getAccountNumber();
        destination = accounts.open("B", money("500")).getAccountNumber();
    }

    @Test
    void cashOperationsRecordSignedAmountsAndExactRunningBalances() {
        assertEquals(money("1001.25"), transactions.deposit("A", source, money("1.25")).getResultingBalance());
        assertEquals(money("-501.30"), transactions.withdraw("A", source, money("501.30")).getAmount());
        StatementService.Statement result = statements.statement("A", source);
        assertEquals(money("499.95"), result.account().getBalance());
        assertEquals(List.of(TransactionType.DEPOSIT, TransactionType.DEPOSIT, TransactionType.WITHDRAWAL),
                result.entries().stream().map(TransactionRecord::getType).toList());
        BankAccount reloaded = new AccountService(new JsonBankingRepository(directory), config).accounts("A").get(0);
        assertEquals(result.account().getOpenedAt(), reloaded.getOpenedAt());
        assertEquals(money("499.95"), reloaded.getBalance());
    }

    @Test
    void transferWritesTwoMatchingEntriesAndSurvivesRestart() {
        Transfer transfer = transactions.transfer("A", source, destination, money("200"), "demo-1");
        BankingState state = new JsonBankingRepository(directory).load();
        assertEquals(money("800"), state.requireAccount(source).getBalance());
        assertEquals(money("700"), state.requireAccount(destination).getBalance());
        assertEquals(TransferStatus.COMPLETED, state.getTransfers().get("demo-1").getStatus());
        List<TransactionRecord> entries = state.getTransactions().stream().filter(e -> "demo-1".equals(e.getTransferReference())).toList();
        assertEquals(2, entries.size());
        assertEquals(List.of(TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN), entries.stream().map(TransactionRecord::getType).toList());
        assertEquals(List.of(money("-200"), money("200")), entries.stream().map(TransactionRecord::getAmount).toList());
        assertTrue(entries.stream().allMatch(e -> e.getTimestamp().equals(transfer.getTimestamp())));
        assertSame(state.requireAccount(source), state.requireCustomer("A").getAccounts().get(0));
    }

    @Test
    void retryWithSameReferenceIsIdempotentAndDifferentDetailsAreRejected() throws Exception {
        transactions.transfer("A", source, destination, money("200"), "retry-1");
        byte[] original = Files.readAllBytes(directory.resolve("banking.json"));
        var restarted = new TransactionService(new JsonBankingRepository(directory), config);
        assertEquals("retry-1", restarted.transfer("A", source, destination, money("200.00"), "retry-1").getTransferReference());
        assertThrows(BankingException.class, () -> restarted.transfer("A", source, destination, money("201"), "retry-1"));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("banking.json")));
        assertEquals(4, repository.load().getTransactions().size());
    }

    @Test
    void failingDestinationAfterDebitLeavesBothPrimaryAndBackupUntouched() throws Exception {
        transactions.withdraw("B", destination, money("500"));
        accounts.close("B", destination);
        byte[] primary = Files.readAllBytes(directory.resolve("banking.json"));
        byte[] backup = Files.readAllBytes(directory.resolve("banking.json.bak"));
        assertThrows(IllegalStateException.class,
                () -> transactions.transfer("A", source, destination, money("200"), "failed-1"));
        assertArrayEquals(primary, Files.readAllBytes(directory.resolve("banking.json")));
        assertArrayEquals(backup, Files.readAllBytes(directory.resolve("banking.json.bak")));
        assertEquals(money("1000"), repository.load().requireAccount(source).getBalance());
        assertTrue(repository.load().getTransfers().isEmpty());
    }

    @Test
    void accountsAndStatementsRequireOwnership() {
        assertThrows(AccountNotFoundException.class, () -> transactions.deposit("B", source, money("5")));
        assertThrows(AccountNotFoundException.class, () -> transactions.withdraw("B", source, money("5")));
        assertThrows(AccountNotFoundException.class, () -> transactions.transfer("B", source, destination, money("5"), "other"));
        assertThrows(AccountNotFoundException.class, () -> statements.statement("B", source));
        assertThrows(AccountNotFoundException.class, () -> accounts.close("B", source));
        assertEquals(List.of(destination), accounts.accounts("B").stream().map(BankAccount::getAccountNumber).toList());
    }

    @Test
    void rejectsSelfTransfersMissingRecipientsAndInsufficientFunds() {
        assertThrows(IllegalArgumentException.class, () -> transactions.transfer("A", source, source, money("1"), "self"));
        assertThrows(AccountNotFoundException.class, () -> transactions.transfer("A", source, "missing", money("1"), "missing"));
        assertThrows(InsufficientFundsException.class, () -> transactions.transfer("A", source, destination, money("1001"), "over"));
        assertThrows(InsufficientFundsException.class, () -> transactions.withdraw("A", source, money("1001")));
        assertEquals(2, repository.load().getTransactions().size());
    }

    @Test
    void boundariesComeFromTheConfiguredPolicy() {
        var custom = new BankingConfig(money("750"), money("2"), money("25"), 5, 5);
        var customAccounts = new AccountService(repository, custom);
        var customTransactions = new TransactionService(repository, custom);
        assertThrows(BankingException.class, () -> customAccounts.open("A", money("749.99")));
        String number = customAccounts.open("A", money("750")).getAccountNumber();
        assertThrows(BankingException.class, () -> customTransactions.deposit("A", number, money("1.99")));
        assertThrows(BankingException.class, () -> customTransactions.transfer("A", number, destination, money("25.01"), "limit"));
        assertEquals(money("775"), customTransactions.deposit("A", number, money("25")).getResultingBalance());
        assertEquals(money("773"), customTransactions.withdraw("A", number, money("2")).getResultingBalance());
    }

    @Test
    void closingAnEmptyAccountKeepsHistoryAndTimestamps() {
        assertThrows(IllegalStateException.class, () -> accounts.close("A", source));
        transactions.withdraw("A", source, money("1000"));
        accounts.close("A", source);
        var result = new StatementService(new JsonBankingRepository(directory)).statement("A", source);
        assertEquals(AccountStatus.CLOSED, result.account().getStatus());
        assertNotNull(result.account().getClosedAt());
        assertEquals(money("0"), result.account().getBalance());
        assertEquals(2, result.entries().size());
        assertThrows(IllegalStateException.class, () -> transactions.deposit("A", source, money("10")));
    }

    @Test
    void concurrentWritersKeepAllDeposits() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> deposits = () -> {
                var independent = new TransactionService(new JsonBankingRepository(directory), config);
                for (int i = 0; i < 5; i++) {
                    independent.deposit("A", source, money("1.01"));
                }
                return null;
            };
            List<Future<Void>> workers = pool.invokeAll(List.of(deposits, deposits));
            for (Future<Void> worker : workers) {
                worker.get();
            }
        } finally {
            pool.shutdownNow();
        }
        var result = statements.statement("A", source);
        assertEquals(money("1010.10"), result.account().getBalance());
        assertEquals(11, result.entries().size());
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), "INR");
    }
}
