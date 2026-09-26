package com.raynald.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpenseTest {

    @Test
    void splitsEvenlyWhenAmountDivides() {
        Expense dinner = new Expense("Dinner", "Alice", 6000, List.of("Alice", "Bob", "Charlie"));
        assertEquals(Map.of("Alice", 2000L, "Bob", 2000L, "Charlie", 2000L), dinner.shares());
    }

    @Test
    void givesLeftoverCentsToFirstPeopleSoSharesAddUpExactly() {
        Expense snacks = new Expense("Snacks", "Alice", 1000, List.of("Alice", "Bob", "Charlie"));
        List<Long> shares = new ArrayList<>(snacks.shares().values());

        assertEquals(List.of(334L, 333L, 333L), shares);
        assertEquals(1000L, shares.stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void rejectsInvalidExpenses() {
        assertThrows(IllegalArgumentException.class,
                () -> new Expense("Lunch", "Alice", 1000, List.of("Bob", "Bob")));
        assertThrows(IllegalArgumentException.class,
                () -> new Expense("Lunch", "Alice", 1000, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Expense("  ", "Alice", 1000, List.of("Alice")));
    }
}