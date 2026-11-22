package com.abhijai.banking.service;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.config.BankingConfigLoader;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.persistence.json.JsonBankingRepository;
import com.abhijai.banking.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {
    @TempDir Path directory;
    private final BankingConfig config = new BankingConfigLoader().load();
    private final Instant start = Instant.parse("2026-01-01T10:00:00Z");
    private final char[] password = "practice-Password".toCharArray();

    private AuthenticationService at(Instant time) {
        return new AuthenticationService(new JsonBankingRepository(directory), config, new PasswordHasher(),
                Clock.fixed(time, ZoneOffset.UTC));
    }

    @Test
    void registrationAndLoginAreCaseInsensitiveAndPasswordsAreSalted() throws Exception {
        var auth = at(start);
        String first = auth.register(" Alice ", password);
        String second = auth.register("albert", password);
        assertEquals(first, auth.login("ALICE", password));
        assertThrows(BankingException.class, () -> auth.register("alice", password));
        var state = new JsonBankingRepository(directory).load();
        assertNotEquals(state.requireCustomer(first).getPasswordHash(), state.requireCustomer(second).getPasswordHash());
        assertFalse(Files.readString(directory.resolve("banking.json")).contains("practice-Password"));
    }

    @Test
    void attemptCountAndCooldownSurviveRestartAndExpireAtTheBoundary() {
        var auth = at(start);
        String customer = auth.register("Alice", password);
        for (int i = 0; i < config.getMaximumLoginAttempts(); i++) {
            assertThrows(BankingException.class, () -> auth.login("Alice", "wrong-pass".toCharArray()));
        }
        assertTrue(assertThrows(BankingException.class, () -> at(start.plusSeconds(299)).login("Alice", password))
                .getMessage().contains("Too many"));
        assertEquals(customer, at(start.plusSeconds(300)).login("ALICE", password));
        var record = new JsonBankingRepository(directory).load().requireCustomer(customer);
        assertEquals(0, record.getFailedLoginAttempts());
        assertNull(record.getLockedUntil());
    }

    @Test
    void changingCredentialsVerifiesTheCurrentPasswordAndPreservesTheCustomerId() {
        var auth = at(start);
        String customer = auth.register("Alice", password);
        auth.register("other-user", password);
        assertThrows(BankingException.class, () -> auth.changeLoginId(customer, password, "OTHER-USER"));
        assertThrows(BankingException.class, () -> auth.changePassword(customer, "bad-password".toCharArray(), "replacement".toCharArray()));
        auth.changeLoginId(customer, password, "alice.new");
        auth.changePassword(customer, password, "replacement".toCharArray());
        var next = at(start);
        assertThrows(BankingException.class, () -> next.login("Alice", password));
        assertThrows(BankingException.class, () -> next.login("alice.new", password));
        assertEquals(customer, next.login("alice.new", "replacement".toCharArray()));
    }

    @Test
    void rejectsInvalidLoginIdsAndBlankOrShortPasswords() {
        var auth = at(start);
        assertThrows(BankingException.class, () -> auth.register("?", password));
        assertThrows(BankingException.class, () -> auth.register("alice", "small".toCharArray()));
        assertThrows(BankingException.class, () -> auth.register("alice", "          ".toCharArray()));
        assertTrue(new JsonBankingRepository(directory).load().getCustomers().isEmpty());
    }
}
