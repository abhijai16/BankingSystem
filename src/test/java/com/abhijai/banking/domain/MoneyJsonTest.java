
package com.abhijai.banking.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyJsonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void shouldSerializeMoneyToJson() throws Exception {

        Money money = new Money(new BigDecimal("5000.00"), "INR");

        String json = mapper.writeValueAsString(money);

        assertTrue(json.contains("\"amount\":5000.00"));
        assertTrue(json.contains("\"currency\":\"INR\""));
    }

    @Test
    void shouldDeserializeJsonToMoney() throws Exception {

        String json = """
                {
                    "amount": 5000.00,
                    "currency": "INR"
                }
                """;

        Money money = mapper.readValue(json, Money.class);

        assertEquals(new BigDecimal("5000.00"), money.getAmount());
        assertEquals("INR", money.getCurrency());
    }

    @Test
    void shouldSerializeAndDeserializeMoneyCorrectly() throws Exception {

        Money original = new Money(new BigDecimal("2500.50"), "INR");

        String json = mapper.writeValueAsString(original);

        Money reconstructed = mapper.readValue(json, Money.class);

        assertEquals(original, reconstructed);
    }

    @Test
    void shouldRejectInvalidCurrencyDuringDeserialization() {

        String json = """
                {
                    "amount": 5000.00,
                    "currency": "USD"
                }
                """;

        assertThrows(Exception.class, () -> mapper.readValue(json, Money.class));
    }

    @Test
    void shouldFailWhenAmountIsMissing() {

        String json = """
                {
                    "currency": "INR"
                }
                """;

        assertThrows(Exception.class, () -> mapper.readValue(json, Money.class));
    }
}
