package com.abhijai.banking.config;

import com.abhijai.banking.domain.Money;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

public final class BankingConfigLoader {

    private final String configFile;

    public BankingConfigLoader() {
        this("/banking.properties");
    }

    BankingConfigLoader(String configFile) {
        if (configFile == null || configFile.isBlank()) {
            throw new IllegalArgumentException("Configuration file path cannot be blank");
        }

        this.configFile = configFile;
    }

    public BankingConfig load(){

        Properties properties = new Properties();

        try(InputStream input = BankingConfigLoader.class.getResourceAsStream(configFile)){
            if(input == null){
                throw new IllegalStateException("Configuration file not found: " + configFile);
            }

            properties.load(input);

        }
        catch(IOException e){
            throw new IllegalStateException("Failed to load banking configuration", e);
        }

        Money minimumOpeningDeposit = getMoney(properties, "bank.minimum-opening-deposit");

        Money minimumTransactionAmount = getMoney(properties, "bank.minimum-transaction-amount");

        Money maximumTransactionAmount = getMoney(properties, "bank.maximum-transaction-amount");

        int maximumLoginAttempts = getInt(properties, "security.maximum-login-attempts");

        long loginCooldownMinutes = getLong(properties, "security.login-cooldown-minutes");

        return new BankingConfig(
                minimumOpeningDeposit,
                minimumTransactionAmount,
                maximumTransactionAmount,
                maximumLoginAttempts,
                loginCooldownMinutes
        );
    }

    private String getRequiredProperty(Properties properties, String key) {

        String value = properties.getProperty(key);

        if(value == null || value.isBlank()){
            throw new IllegalStateException("Missing or blank configuration property: " + key);
        }

        return value.trim();
    }

    private Money getMoney(Properties properties, String key){

        String value = getRequiredProperty(properties, key);

        try{
            return new Money(new BigDecimal(value), "INR");
        }
        catch(NumberFormatException | ArithmeticException e){
            throw new IllegalStateException("Invalid monetary configuration for property: " + key, e);
        }
    }

    private int getInt(Properties properties, String key) {

        String value = getRequiredProperty(properties, key);

        try{
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer configuration for property: " + key, e);
        }
    }

    private long getLong(Properties properties, String key) {

        String value = getRequiredProperty(properties, key);

        try{
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid long configuration for property: " + key, e);
        }
    }
}
