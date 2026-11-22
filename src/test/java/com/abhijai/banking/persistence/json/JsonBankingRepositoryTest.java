package com.abhijai.banking.persistence.json;

import com.abhijai.banking.config.BankingConfigLoader;
import com.abhijai.banking.domain.Customer;
import com.abhijai.banking.domain.Money;
import com.abhijai.banking.service.AccountService;
import com.abhijai.banking.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonBankingRepositoryTest {
    @TempDir Path directory;
    private JsonBankingRepository repository;
    private Path main;
    private Path backup;
    private String number;

    @BeforeEach
    void saveTwoBalances() {
        repository = new JsonBankingRepository(directory);
        main = directory.resolve("banking.json");
        backup = directory.resolve("banking.json.bak");
        var config = new BankingConfigLoader().load();
        repository.update(state -> {
            state.addCustomer(new Customer("c", "alice", "fixture"));
            return null;
        });
        number = new AccountService(repository, config).open("c", new Money(new BigDecimal("500"), "INR")).getAccountNumber();
        new TransactionService(repository, config).deposit("c", number, new Money(new BigDecimal("10"), "INR"));
    }

    @Test
    void damagedPrimaryIsPreservedAndRecoveryUsesThePreviousCommit() throws Exception {
        Files.writeString(main, "{broken");
        assertThrows(IllegalStateException.class, repository::load);
        assertEquals("{broken", Files.readString(main));
        repository.restoreBackup();
        assertEquals(new BigDecimal("500.00"), repository.load().requireAccount(number).getBalance().getAmount());
        try (var files = Files.list(directory)) {
            Path damaged = files.filter(p -> p.getFileName().toString().startsWith("banking.damaged-")).findFirst().orElseThrow();
            assertEquals("{broken", Files.readString(damaged));
        }
    }

    @Test
    void missingPrimaryWithExistingBackupNeverStartsAnEmptyBank() throws Exception {
        Files.delete(main);
        assertThrows(IllegalStateException.class, repository::load);
        repository.restoreBackup();
        assertEquals(1, repository.load().getAccounts().size());
    }

    @Test
    void validPrimaryIsProtectedAndUnusableBackupCannotReplaceIt() throws Exception {
        byte[] healthy = Files.readAllBytes(main);
        assertThrows(IllegalStateException.class, repository::restoreBackup);
        assertArrayEquals(healthy, Files.readAllBytes(main));
        Files.writeString(main, "broken current");
        Files.writeString(backup, "broken backup");
        assertThrows(IllegalStateException.class, repository::restoreBackup);
        assertEquals("broken current", Files.readString(main));
    }

    @Test
    void rejectsAnAccountBalanceThatDisagreesWithTheLedger() throws Exception {
        String json = Files.readString(main).replaceFirst("510\\.00", "810.00");
        Files.writeString(main, json);
        assertThrows(IllegalStateException.class, repository::load);
        assertEquals(json, Files.readString(main));
    }

    @Test
    void failureToWriteTheBackupLeavesThePriorCommitInPlace() throws Exception {
        byte[] before = Files.readAllBytes(main);
        Files.delete(backup);
        Files.createDirectory(backup);
        Files.writeString(backup.resolve("block-replacement"), "fixture");
        var service = new TransactionService(repository, new BankingConfigLoader().load());
        assertThrows(IllegalStateException.class, () -> service.deposit("c", number, new Money(BigDecimal.TEN, "INR")));
        assertArrayEquals(before, Files.readAllBytes(main));
    }
}
