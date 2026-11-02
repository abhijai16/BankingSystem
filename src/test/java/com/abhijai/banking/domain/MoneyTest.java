package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {

    //isZero() Unit Test
    @Test
    void shouldReturnTrueWhenAmountIsZero(){
        Money money = new Money(BigDecimal.ZERO, "INR");

        boolean result = money.isZero();

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenAmountIsPositive(){
        Money money = new Money(new BigDecimal("100"), "INR");

        boolean result = money.isZero();

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenAmountIsNegative(){
        Money money = new Money(new BigDecimal("-50"), "INR");

        boolean result = money.isZero();

        assertFalse(result);
    }

    // Constructor two decimal place check
    @Test
    void shouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> {
            new Money(new BigDecimal("100.567"), "INR");
        });

        assertEquals("Amount accepted upto two decimal places", ex.getMessage());
    }

    // Constructor Not accepting any other currency than INR.
    @Test
    void shouldRejectUnsupportedCurrency(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> {
            new Money(BigDecimal.ZERO, "USD");
        });

        assertEquals("Only INR is supported", ex.getMessage());
    }

    


}
