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
}
