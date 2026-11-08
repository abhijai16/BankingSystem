package com.abhijai.banking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

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
        assertThrows(ArithmeticException.class, () -> {
            new Money(new BigDecimal("100.567"), "INR");
        });
    }

    // Constructor Not accepting any other currency than INR.
    @Test
    void shouldRejectUnsupportedCurrency(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> {
            new Money(BigDecimal.ZERO, "USD");
        });

        assertEquals("Only INR is supported", ex.getMessage());
    }

    // add() unit test
    @Test
    void shouldAddTwoMoneyAmountsCorrectly(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        Money result = a.add(b);

        assertEquals(new Money(new BigDecimal("700"), "INR"), result);
    }

    @Test
    void shouldNotModifyOriginalValueAfterAddition(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        Money result = a.add(b);

        assertEquals(new Money(new BigDecimal("500"), "INR"), a);
        assertEquals(new Money(new BigDecimal("200"), "INR"), b);
    }

//    @Test
//    void shouldRejectAdditionOfDifferentCurrencies(){
//        Money a = new Money(new BigDecimal("500"), "INR");
//        Money b = new Money(new BigDecimal("200"), "USD"); ---this will throw exception as we haven't allowed other currencies.---
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
//            a.add(b);
//        });
//
//        assertEquals("Cannot add different currencies", ex.getMessage());
//    }

    // subtract() unit test
    @Test
    void shouldSubtractTwoMoneyAmountCorrectly(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        Money result = a.subtract(b);

        assertEquals(new Money(new BigDecimal("300"), "INR"), result);
    }

    @Test
    void shouldAllowNegativeResultsAfterSubtraction(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        Money result = b.subtract(a);

        assertEquals(new Money(new BigDecimal("-300"), "INR"), result);
    }

    // negate() unit test
    @Test
    void shouldNegatePositiveAmountCorrectly(){
        Money a = new Money(new BigDecimal("500"), "INR");

        Money result = a.negate();

        assertEquals(new Money(new BigDecimal("-500"), "INR"), result);
    }

    @Test
    void shouldNotModifyOriginalAmountAfterNegation(){
        Money a = new Money(new BigDecimal("500"), "INR");

        a.negate();

        assertEquals(new Money(new BigDecimal("500"), "INR"), a);
    }

    //compareTo() unit test
    @Test
    void shouldReturnNegativeWhenAmountIsLessThanOther(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        int result = b.compareTo(a);

        assertTrue(result<0);
    }

    @Test
    void shouldReturnPositiveWhenAmountIsMoreThanOther(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("200"), "INR");

        int result = a.compareTo(b);

        assertTrue(result>0);
    }

    @Test
    void shouldReturnZeroWhenAmountIsEqualToOther(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("500.0"), "INR");

        int result = a.compareTo(b);

        assertEquals(0, result);
    }

    // equals() unit test
    @Test
    void shouldReturnTrueWhenTwoMoneyObjectsHaveSameValue(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("500.0"), "INR");

        assertEquals(a, b); // internally it calls a.equals(b)
    }

    @Test
    void shouldReturnFalseWhenTwoMoneyObjectsHaveDifferentValues(){
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("900.0"), "INR");

        assertNotEquals(a, b);
    }

    @Test
    void shouldReturnFalseWhenComparedWithDifferentObjectType(){
        Money a = new Money(new BigDecimal("500"), "INR");

        boolean result = a.equals("False");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenComparedWithSameReference(){
        Money a = new Money(new BigDecimal("500"), "INR");

        assertEquals(a, a);
    }

    // hashCode() unit test
    @Test
    void shouldReturnSameHashCodeForEqualObjects() {
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("500.0"), "INR");

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotStoreDuplicateMoneyObjectsInHashSet() {
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("500.0"), "INR");

        Set<Money> moneySet = new HashSet<>();

        moneySet.add(a);
        moneySet.add(b);

        assertEquals(1, moneySet.size());
    }

    @Test
    void shouldStoreDifferentMoneyObjectsInHashSet() {
        Money a = new Money(new BigDecimal("500"), "INR");
        Money b = new Money(new BigDecimal("700"), "INR");

        Set<Money> moneySet = new HashSet<>();

        moneySet.add(a);
        moneySet.add(b);

        assertEquals(2, moneySet.size());
    }
}