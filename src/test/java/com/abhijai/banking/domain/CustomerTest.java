
package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer createCustomer() {
        return new Customer("101", "abhijai123", "dummyHash");
    }

    private BankAccount createAccount(String accountNumber, String ownerId) {
        Money amount = new Money(new BigDecimal("500"), "INR");
        return new BankAccount(accountNumber, ownerId, amount);
    }

    // Constructor tests
    @Test
    void successfulCustomerCreation(){
        Customer customer = createCustomer();

        assertEquals("101", customer.getCustomerId());
        assertEquals("abhijai123", customer.getLoginId());
        assertEquals("dummyHash", customer.getPasswordHash());
        assertNotNull(customer.getCreatedAt());
        assertTrue(customer.getAccounts().isEmpty());
    }

    @Test
    void nullCustomerIdThrowsException(){
        assertThrows(NullPointerException.class, () ->
                new Customer(null, "abhijai123", "dummyHash"));
    }

    @Test
    void blankCustomerIdThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new Customer("", "abhijai123", "dummyHash"));
    }

    @Test
    void nullLoginIdThrowsException(){
        assertThrows(NullPointerException.class, () ->
                new Customer("101", null, "dummyHash"));
    }

    @Test
    void blankLoginIdThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new Customer("101", "", "dummyHash"));
    }

    @Test
    void nullPasswordHashThrowsException(){
        assertThrows(NullPointerException.class, () ->
                new Customer("101", "abhijai123", null));
    }

    @Test
    void blankPasswordHashThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new Customer("101", "abhijai123", ""));
    }

    // addAccount() tests
    @Test
    void successfullyAddAccount(){
        Customer customer = createCustomer();
        BankAccount account = createAccount("100001", "101");

        customer.addAccount(account);

        assertEquals(1, customer.getAccounts().size());
        assertEquals(account, customer.getAccounts().get(0));
    }

    @Test
    void addNullAccountThrowsException(){
        Customer customer = createCustomer();

        assertThrows(NullPointerException.class, () ->
                customer.addAccount(null));

        assertTrue(customer.getAccounts().isEmpty());
    }

    @Test
    void addingAnotherCustomersAccountThrowsException(){
        Customer customer = createCustomer();
        BankAccount account = createAccount("100002", "102");

        assertThrows(IllegalArgumentException.class, () ->
                customer.addAccount(account));

        assertTrue(customer.getAccounts().isEmpty());
    }

    @Test
    void addingDuplicateAccountThrowsException(){
        Customer customer = createCustomer();

        BankAccount first = createAccount("100001", "101");
        BankAccount second = createAccount("100001", "101");

        customer.addAccount(first);

        assertThrows(IllegalArgumentException.class, () ->
                customer.addAccount(second));

        assertEquals(1, customer.getAccounts().size());
    }

    // Collection protection test
    @Test
    void returnedAccountListCannotBeModified(){
        Customer customer = createCustomer();
        BankAccount account = createAccount("100001", "101");

        customer.addAccount(account);

        assertThrows(UnsupportedOperationException.class, () ->
                customer.getAccounts().clear());

        assertEquals(1, customer.getAccounts().size());
    }

    // changeLoginId() tests
    @Test
    void successfullyChangeLoginId(){
        Customer customer = createCustomer();

        customer.changeLoginId("newLogin123");

        assertEquals("newLogin123", customer.getLoginId());
    }

    @Test
    void changingLoginIdToNullThrowsException(){
        Customer customer = createCustomer();

        assertThrows(NullPointerException.class, () ->
                customer.changeLoginId(null));

        assertEquals("abhijai123", customer.getLoginId());
    }

    @Test
    void changingLoginIdToBlankThrowsException(){
        Customer customer = createCustomer();

        assertThrows(IllegalArgumentException.class, () ->
                customer.changeLoginId(""));

        assertEquals("abhijai123", customer.getLoginId());
    }


    // changePasswordHash() tests
    @Test
    void successfullyChangePasswordHash(){
        Customer customer = createCustomer();

        customer.changePasswordHash("newDummyHash");

        assertEquals("newDummyHash", customer.getPasswordHash());
    }

    @Test
    void changingPasswordHashToNullThrowsException(){
        Customer customer = createCustomer();

        assertThrows(NullPointerException.class, () ->
                customer.changePasswordHash(null));

        assertEquals("dummyHash", customer.getPasswordHash());
    }

    @Test
    void changingPasswordHashToBlankThrowsException(){
        Customer customer = createCustomer();

        assertThrows(IllegalArgumentException.class, () ->
                customer.changePasswordHash(""));

        assertEquals("dummyHash", customer.getPasswordHash());
    }
}
