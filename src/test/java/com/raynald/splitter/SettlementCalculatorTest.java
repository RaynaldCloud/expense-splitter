package com.raynald.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SettlementCalculatorTest {

    @Test
    void settlesASimpleCase() {
        Map<String, Long> balances = Map.of("Alice", 4000L, "Bob", -1250L, "Charlie", -2750L);

        List<Payment> payments = SettlementCalculator.settle(balances);

        assertEquals(List.of(
                new Payment("Charlie", "Alice", 2750),
                new Payment("Bob", "Alice", 1250)), payments);
    }

    @Test
    void needsNoPaymentsWhenEveryoneIsSettled() {
        Map<String, Long> balances = Map.of("Alice", 0L, "Bob", 0L);
        assertTrue(SettlementCalculator.settle(balances).isEmpty());
    }

    @Test
    void settlesEveryoneWithAtMostNMinusOnePayments() {
        Map<String, Long> balances = Map.of(
                "Alice", 5000L, "Bob", -2000L, "Charlie", -1500L, "Dana", 700L, "Evan", -2200L);

        List<Payment> payments = SettlementCalculator.settle(balances);

        // Apply every payment, and check that everyone ends up at exactly zero
        Map<String, Long> after = new HashMap<>(balances);
        for (Payment payment : payments) {
            after.merge(payment.from(), payment.amountCents(), Long::sum);
            after.merge(payment.to(), -payment.amountCents(), Long::sum);
        }
        after.values().forEach(balance -> assertEquals(0L, balance));

        assertTrue(payments.size() <= balances.size() - 1);
    }
}