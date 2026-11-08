
package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TransactionRecordTest{

    //helper class
    private Money money(String amount) {
        return new Money(new BigDecimal(amount), "INR");
    }

    private TransactionRecord createDeposit() {
        return new TransactionRecord(
                "TXN-101",
                "100001",
                TransactionType.DEPOSIT,
                money("1000"),
                money("1500"),
                Instant.now(),
                null
        );
    }

    // Valid record tests
    @Test
    void successfullyCreateDepositRecord(){
        TransactionRecord record = createDeposit();

        assertEquals("TXN-101", record.getTransactionId());
        assertEquals("100001", record.getAccountNumber());
        assertEquals(TransactionType.DEPOSIT, record.getType());
        assertEquals(money("1000"), record.getAmount());
        assertEquals(money("1500"), record.getResultingBalance());
        assertNotNull(record.getTimestamp());
        assertNull(record.getTransferReference());
    }

    @Test
    void successfullyCreateTransferRecord(){
        TransactionRecord record = new TransactionRecord(
                "TXN-102",
                "100001",
                TransactionType.TRANSFER_OUT,
                money("-500"),
                money("1000"),
                Instant.now(),
                "TRF-101"
        );

        assertEquals(TransactionType.TRANSFER_OUT, record.getType());
        assertEquals(money("-500"), record.getAmount());
        assertEquals("TRF-101", record.getTransferReference());
    }

    // Null validation tests
    @Test
    void nullTransactionIdThrowsException(){
        assertThrows(NullPointerException.class, () ->
                new TransactionRecord(
                        null, "100001", TransactionType.DEPOSIT,
                        money("100"), money("600"), Instant.now(), null
                ));
    }

    @Test
    void nullAmountThrowsException(){
        assertThrows(NullPointerException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.DEPOSIT,
                        null, money("600"), Instant.now(), null
                ));
    }

    // Amount validation tests
    @Test
    void zeroAmountThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.DEPOSIT,
                        money("0"), money("600"), Instant.now(), null
                ));
    }

    @Test
    void negativeDepositThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.DEPOSIT,
                        money("-500"), money("600"), Instant.now(), null
                ));
    }

    @Test
    void positiveWithdrawalThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.WITHDRAWAL,
                        money("500"), money("600"), Instant.now(), null
                ));
    }

    @Test
    void negativeResultingBalanceThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.DEPOSIT,
                        money("500"), money("-100"), Instant.now(), null
                ));
    }

    // Transfer reference tests
    @Test
    void transferWithoutReferenceThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.TRANSFER_OUT,
                        money("-500"), money("1000"), Instant.now(), null
                ));
    }

    @Test
    void depositWithTransferReferenceThrowsException(){
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionRecord(
                        "TXN-101", "100001", TransactionType.DEPOSIT,
                        money("500"), money("1000"), Instant.now(), "TRF-101"
                ));
    }
}
