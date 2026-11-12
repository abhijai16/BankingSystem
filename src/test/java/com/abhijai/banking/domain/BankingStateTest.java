
package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BankingStateTest {

    // Helper method to create an empty valid BankingState
    private BankingState createEmptyState() {
        return new BankingState(1, new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    // 1. Constructor tests
    @Test
    void shouldCreateBankingStateSuccessfully() {

        BankingState state = createEmptyState();

        assertNotNull(state);
        assertEquals(1, state.getSchemaVersion());
    }

    @Test
    void shouldRejectUnsupportedSchemaVersion() {

        assertThrows(IllegalArgumentException.class, () -> new BankingState(
                        2,
                        new HashMap<>(),
                        new HashMap<>(),
                        new ArrayList<>(),
                        new HashMap<>()
                )
        );
    }

    // 2. Null validation tests
    @Test
    void shouldRejectNullCustomersMap() {

        assertThrows(
                NullPointerException.class,
                () -> new BankingState(
                        1,
                        null,
                        new HashMap<>(),
                        new ArrayList<>(),
                        new HashMap<>()
                )
        );
    }

    @Test
    void shouldRejectNullAccountsMap() {

        assertThrows(
                NullPointerException.class,
                () -> new BankingState(
                        1,
                        new HashMap<>(),
                        null,
                        new ArrayList<>(),
                        new HashMap<>()
                )
        );
    }

    @Test
    void shouldRejectNullTransactionsList() {

        assertThrows(
                NullPointerException.class,
                () -> new BankingState(
                        1,
                        new HashMap<>(),
                        new HashMap<>(),
                        null,
                        new HashMap<>()
                )
        );
    }

    @Test
    void shouldRejectNullTransfersMap() {

        assertThrows(
                NullPointerException.class,
                () -> new BankingState(
                        1,
                        new HashMap<>(),
                        new HashMap<>(),
                        new ArrayList<>(),
                        null
                )
        );
    }

    // 3. Defensive copying tests
    @Test
    void shouldNotBeAffectedWhenOriginalCustomersMapChanges() {

        Map<String, Customer> customers = new HashMap<>();

        BankingState state = new BankingState(
                1,
                customers,
                new HashMap<>(),
                new ArrayList<>(),
                new HashMap<>()
        );

        customers.clear();
        customers.put("C101", new Customer("C101", "user101", "hash"));

        assertTrue(state.getCustomers().isEmpty());
    }

    @Test
    void shouldNotBeAffectedWhenOriginalAccountsMapChanges() {

        Map<String, BankAccount> accounts = new HashMap<>();

        BankingState state = new BankingState(
                1,
                new HashMap<>(),
                accounts,
                new ArrayList<>(),
                new HashMap<>()
        );

        accounts.put(
                "1234567890",
                new BankAccount(
                        "1234567890",
                        "C101",
                        new Money(new java.math.BigDecimal("500"), "INR")
                )
        );

        assertTrue(state.getAccounts().isEmpty());
    }

    @Test
    void shouldNotBeAffectedWhenOriginalTransactionsListChanges() {

        List<TransactionRecord> transactions = new ArrayList<>();

        BankingState state = new BankingState(
                1,
                new HashMap<>(),
                new HashMap<>(),
                transactions,
                new HashMap<>()
        );

        transactions.clear();

        assertTrue(state.getTransactions().isEmpty());
    }

    @Test
    void shouldNotBeAffectedWhenOriginalTransfersMapChanges() {

        Map<String, Transfer> transfers = new HashMap<>();

        BankingState state = new BankingState(
                1,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                transfers
        );

        transfers.clear();

        assertTrue(state.getTransfers().isEmpty());
    }

    // 4. Unmodifiable collection tests
    @Test
    void shouldNotAllowModificationOfCustomersMap() {

        BankingState state = createEmptyState();

        assertThrows(
                UnsupportedOperationException.class,
                () -> state.getCustomers().put(
                        "C101",
                        new Customer("C101", "user101", "hash")
                )
        );
    }

    @Test
    void shouldNotAllowModificationOfAccountsMap() {

        BankingState state = createEmptyState();

        assertThrows(
                UnsupportedOperationException.class,
                () -> state.getAccounts().clear()
        );
    }

    @Test
    void shouldNotAllowModificationOfTransactionsList() {

        BankingState state = createEmptyState();

        assertThrows(
                UnsupportedOperationException.class,
                () -> state.getTransactions().clear()
        );
    }

    @Test
    void shouldNotAllowModificationOfTransfersMap() {

        BankingState state = createEmptyState();

        assertThrows(
                UnsupportedOperationException.class,
                () -> state.getTransfers().clear()
        );
    }

    // 5. Getter tests
    @Test
    void shouldReturnEmptyCollectionsForNewBankingState() {

        BankingState state = createEmptyState();

        assertTrue(state.getCustomers().isEmpty());
        assertTrue(state.getAccounts().isEmpty());
        assertTrue(state.getTransactions().isEmpty());
        assertTrue(state.getTransfers().isEmpty());
    }

    @Test
    void shouldReturnCorrectSchemaVersion() {

        BankingState state = createEmptyState();

        assertEquals(1, state.getSchemaVersion());
    }
}
