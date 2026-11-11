package com.abhijai.banking.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankingConfigLoaderTest {

    private final BankingConfigLoader loader = new BankingConfigLoader();

    @Test
    void shouldLoadConfigurationSuccessfully() {

        BankingConfig config = loader.load();

        assertNotNull(config);
    }

    @Test
    void shouldLoadMinimumOpeningDeposit() {

        BankingConfig config = loader.load();

        assertNotNull(config.getMinimumOpeningDeposit());
        assertTrue(config.getMinimumOpeningDeposit().isPositive());
    }

    @Test
    void shouldLoadMinimumTransactionAmount() {

        BankingConfig config = loader.load();

        assertNotNull(config.getMinimumTransactionAmount());
        assertTrue(config.getMinimumTransactionAmount().isPositive());
    }

    @Test
    void shouldLoadMaximumTransactionAmount() {

        BankingConfig config = loader.load();

        assertNotNull(config.getMaximumTransactionAmount());

        assertTrue(config.getMaximumTransactionAmount().compareTo(config.getMinimumTransactionAmount()) >= 0);
    }

    @Test
    void shouldLoadValidLoginSettings() {

        BankingConfig config = loader.load();

        assertTrue(config.getMaximumLoginAttempts() > 0);
        assertTrue(config.getLoginCooldownMinutes() > 0);
    }

    //Missing opening deposit unit test.
    @Test
    void shouldThrowExceptionWhenOpeningDepositIsMissing() {

        BankingConfigLoader loader = new BankingConfigLoader("/missing-opening-deposit.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("bank.minimum-opening-deposit"));
    }

    //Blank opening deposit unit test.
    @Test
    void shouldThrowExceptionWhenOpeningDepositIsBlank() {

        BankingConfigLoader loader = new BankingConfigLoader("/blank-opening-deposit.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("bank.minimum-opening-deposit"));
    }

    //Invalid opening deposit unit test.
    @Test
    void shouldThrowExceptionWhenOpeningDepositIsInvalid() {

        BankingConfigLoader loader = new BankingConfigLoader("/invalid-opening-deposit.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("bank.minimum-opening-deposit"));
    }

    //Invalid login attempts unit test.
    @Test
    void shouldThrowExceptionWhenMaximumLoginAttemptsIsInvalid(){
        BankingConfigLoader loader = new BankingConfigLoader("/invalid-login-attempts.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("security.maximum-login-attempts"));
    }

    //Invalid login cooldown unit test.
    @Test
    void shouldThrowExceptionWhenLoginCooldownMinutesIsInvalid(){
        BankingConfigLoader loader = new BankingConfigLoader("/invalid-login-cooldown.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("security.login-cooldown-minutes"));
    }

    //Missing configuration file unit test.
    @Test
    void shouldThrowExceptionWhenConfigurationFileIsMissing(){
        BankingConfigLoader loader = new BankingConfigLoader("/non-existent.properties");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("/non-existent.properties"));
    }

    //Invalid business configuration(minimum transaction>maximum transaction)
    @Test
    void shouldThrowExceptionWhenMinimumTransactionAmountExceedsMaximum(){
        BankingConfigLoader loader = new BankingConfigLoader("/invalid-transaction-limits.properties");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loader.load());

        assertTrue(exception.getMessage().contains("Minimum transaction amount cannot be greater than maximum transaction amount."));

    }
}
