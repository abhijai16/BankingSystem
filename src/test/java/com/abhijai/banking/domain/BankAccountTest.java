package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BankAccountTest{

    //Constructor unit test
    @Test
    void successfulAccountCreation(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        BankAccount account = new BankAccount("100001", "101", amount);

        assertEquals("100001", account.getAccountNumber());
        assertEquals("101", account.getOwnerCustomerId());
        assertEquals(amount, account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getOpenedAt());
        assertNull(account.getClosedAt());
    }

    @Test
    void openingDepositBelowMinimumThrowsException(){
        Money amount = new Money(new BigDecimal("300"), "INR");

        assertThrows(IllegalArgumentException.class, () -> {
            new BankAccount("100001", "101", amount);
        });
    }

    @Test
    void nullAccountNumberThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");

        assertThrows(NullPointerException.class, () -> {
            new BankAccount(null, "101", amount);
        });
    }

    @Test
    void blankAccountNumberThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");

        assertThrows(IllegalArgumentException.class, () -> {
            new BankAccount("", "101", amount);
        });
    }

    @Test
    void nullOwnerIdThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");

        assertThrows(NullPointerException.class, () -> {
            new BankAccount("100001", null, amount);
        });
    }

    @Test
    void blankOwnerIdThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");

        assertThrows(IllegalArgumentException.class, () -> {
            new BankAccount("100001", "", amount);
        });
    }

    @Test
    void nullOpeningDepositThrowsException(){
        assertThrows(NullPointerException.class, () -> {
            new BankAccount("100001", "101", null);
        });
    }

    // deposit() unit test
    @Test
    void successfulDepositIncreasesBalance(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money depositAmount = new Money(new BigDecimal("300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.deposit(depositAmount);

        assertEquals(amount.add(depositAmount), account.getBalance());
    }

    @Test
    void zeroDepositThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money depositAmount = new Money(new BigDecimal("0"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalArgumentException.class, () -> {
             account.deposit(depositAmount);
        });
        assertEquals(amount, account.getBalance());
    }

    @Test
    void negativeDepositThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money depositAmount = new Money(new BigDecimal("-300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(depositAmount);
        });
        assertEquals(amount, account.getBalance());
    }

    @Test
    void nullDepositThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(NullPointerException.class, () -> {
            account.deposit(null);
        });
        assertEquals(amount, account.getBalance());
    }

    @Test
    void depositIntoClosedAccountThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money depositAmount = new Money(new BigDecimal("300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.withdraw(amount);
        account.closeAccount();

        assertThrows(IllegalStateException.class, () -> {
            account.deposit(depositAmount);
        });
        assertEquals(new Money(new BigDecimal("0"), "INR"), account.getBalance());
    }

    @Test
    void depositDoesNotChangeOriginalMoneyObject(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money depositAmount = new Money(new BigDecimal("300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.deposit(depositAmount);

        assertEquals(new Money(new BigDecimal("500"), "INR"), amount);
    }

    // withdraw() unit test
    @Test
    void successfulWithdrawalDecreasesBalance(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money withdrawalAmount = new Money(new BigDecimal("300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.withdraw(withdrawalAmount);

        assertEquals(amount.subtract(withdrawalAmount), account.getBalance());
    }

    @Test
    void fullBalanceWithdrawalIsAllowed() {
        Money amount = new Money(new BigDecimal("500"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.withdraw(amount);

        assertEquals(amount.subtract(amount), account.getBalance());
    }

    @Test
    void insufficientBalanceThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money withdrawalAmount = new Money(new BigDecimal("600"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(withdrawalAmount);
        });

        assertEquals(amount, account.getBalance());
    }

    @Test
    void zeroWithdrawalThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money withdrawalAmount = new Money(new BigDecimal("0"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(withdrawalAmount);
        });

        assertEquals(amount, account.getBalance());
    }

    @Test
    void negativeWithdrawalThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money withdrawalAmount = new Money(new BigDecimal("-200"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(withdrawalAmount);
        });

        assertEquals(amount, account.getBalance());
    }

    @Test
    void nullWithdrawalThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(NullPointerException.class, () -> {
            account.withdraw(null);
        });

        assertEquals(amount, account.getBalance());
    }

    @Test
    void withdrawalFromClosedAccountThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.withdraw(amount);
        account.closeAccount();

        assertThrows(IllegalStateException.class, () -> {
            account.withdraw(new Money(new BigDecimal("200"), "INR"));
        });

        assertEquals(amount.subtract(amount), account.getBalance());

    }

    @Test
    void withdrawalDoesNotChangeOriginalMoneyObject() {
        Money amount = new Money(new BigDecimal("500"), "INR");
        Money withdrawalAmount = new Money(new BigDecimal("300"), "INR");

        BankAccount account = new BankAccount("100001", "101", amount);
        account.withdraw(withdrawalAmount);

        assertEquals(new Money(new BigDecimal("500"), "INR"), amount);
        assertEquals(new Money(new BigDecimal("200"), "INR"), account.getBalance());
    }

    // closeAccount() unit test
    @Test
    void successfulAccountClosure(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        BankAccount account = new BankAccount("100001", "101", amount);

        account.withdraw(amount);
        account.closeAccount();

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        assertNotNull(account.getClosedAt());
        assertTrue(account.getBalance().isZero());
    }

    @Test
    void closingAccountWithNonZeroBalanceThrowsException(){
        Money amount = new Money(new BigDecimal("500"), "INR");
        BankAccount account = new BankAccount("100001", "101", amount);

        assertThrows(IllegalStateException.class, () -> {
            account.closeAccount();
        });

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNull(account.getClosedAt());
        assertEquals(amount, account.getBalance());
    }

    @Test
    void closingAlreadyClosedAccountThrowsException() {
        Money amount = new Money(new BigDecimal("500"), "INR");
        BankAccount account = new BankAccount("100001", "101", amount);

        account.withdraw(amount);
        account.closeAccount();

        assertThrows(IllegalStateException.class, () -> {
            account.closeAccount();
        });

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        assertNotNull(account.getClosedAt());
        assertTrue(account.getBalance().isZero());
    }

}
