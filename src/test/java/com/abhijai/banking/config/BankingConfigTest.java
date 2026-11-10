
package com.abhijai.banking.config;

import com.abhijai.banking.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankingConfigTest {

    private Money money(String amount) {
        return new Money(new BigDecimal(amount), "INR");
    }

    private BankingConfig createConfig() {
        return new BankingConfig(
                money("500"),
                money("1"),
                money("100000"),
                5,
                5
        );
    }

    // Successful creation
    @Test
    void successfullyCreateBankingConfig() {
        BankingConfig config = createConfig();

        assertEquals(money("500"), config.getMinimumOpeningDeposit());
        assertEquals(money("1"), config.getMinimumTransactionAmount());
        assertEquals(money("100000"), config.getMaximumTransactionAmount());
        assertEquals(5, config.getMaximumLoginAttempts());
        assertEquals(5L, config.getLoginCooldownMinutes());
    }

    // Null validation
    @Test
    void nullMinimumOpeningDepositThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new BankingConfig(null, money("1"), money("100000"), 5, 5));
    }

    @Test
    void nullMinimumTransactionAmountThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new BankingConfig(money("500"), null, money("100000"), 5, 5));
    }

    @Test
    void nullMaximumTransactionAmountThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new BankingConfig(money("500"), money("1"), null, 5, 5));
    }

    // Opening deposit validation
    @Test
    void openingDepositBelow500ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("499"), money("1"), money("100000"), 5, 5));
    }

    // Transaction amount validation
    @Test
    void minimumTransactionGreaterThanMaximumThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("200"), money("100"), 5, 5));
    }

    @Test
    void zeroMinimumTransactionAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("0"), money("100000"), 5, 5));
    }

    @Test
    void negativeMinimumTransactionAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("-1"), money("100000"), 5, 5));
    }

    // Login attempt validation
    @Test
    void zeroMaximumLoginAttemptsThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("1"), money("100000"), 0, 5));
    }

    @Test
    void negativeMaximumLoginAttemptsThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("1"), money("100000"), -1, 5));
    }

    // Cooldown validation
    @Test
    void zeroCooldownThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("1"), money("100000"), 5, 0));
    }

    @Test
    void negativeCooldownThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankingConfig(money("500"), money("1"), money("100000"), 5, -1));
    }
    
}
