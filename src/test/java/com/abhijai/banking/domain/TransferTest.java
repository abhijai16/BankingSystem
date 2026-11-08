
package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TransferTest {

    private Money money(String amount) {
        return new Money(new BigDecimal(amount), "INR");
    }

    private Transfer createTransfer() {
        return new Transfer(
                "TRF-101",
                "100001",
                "100002",
                money("2000"),
                Instant.now(),
                TransferStatus.COMPLETED
        );
    }

    // Successful creation tests
    @Test
    void successfullyCreateTransfer() {
        Transfer transfer = createTransfer();

        assertEquals("TRF-101", transfer.getTransferReference());
        assertEquals("100001", transfer.getSourceAccountNumber());
        assertEquals("100002", transfer.getDestinationAccountNumber());
        assertEquals(money("2000"), transfer.getAmount());
        assertNotNull(transfer.getTimestamp());
        assertEquals(TransferStatus.COMPLETED, transfer.getStatus());
    }

    // Null validation tests
    @Test
    void nullTransferReferenceThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        null, "100001", "100002",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void nullSourceAccountThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        "TRF-101", null, "100002",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void nullDestinationAccountThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        "TRF-101", "100001", null,
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void nullAmountThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100002",
                        null, Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void nullTimestampThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100002",
                        money("2000"), null,
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void nullStatusThrowsException() {
        assertThrows(NullPointerException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100002",
                        money("2000"), Instant.now(), null
                ));
    }

    // Blank validation tests
    @Test
    void blankTransferReferenceThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "", "100001", "100002",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void blankSourceAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "TRF-101", "", "100002",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void blankDestinationAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    // Account validation tests
    @Test
    void sameSourceAndDestinationAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100001",
                        money("2000"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    // Amount validation tests
    @Test
    void zeroTransferAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100002",
                        money("0"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }

    @Test
    void negativeTransferAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transfer(
                        "TRF-101", "100001", "100002",
                        money("-500"), Instant.now(),
                        TransferStatus.COMPLETED
                ));
    }
}
