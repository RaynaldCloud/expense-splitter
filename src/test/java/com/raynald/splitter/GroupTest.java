package com.raynald.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupTest {

    private Group group;

    @BeforeEach
    void createGroup() {
        group = new Group();
        group.addMember("Alice");
        group.addMember("Bob");
        group.addMember("Charlie");
    }

    @Test
    void calculatesBalancesAcrossExpenses() {
        group.addExpense(new Expense("Dinner", "Alice", 6000, List.of("Alice", "Bob", "Charlie")));
        group.addExpense(new Expense("Taxi", "Bob", 1500, List.of("Bob", "Charlie")));

        assertEquals(Map.of("Alice", 4000L, "Bob", -1250L, "Charlie", -2750L), group.balances());
    }

    @Test
    void balancesAlwaysAddUpToZero() {
        group.addExpense(new Expense("Snacks", "Alice", 1000, List.of("Alice", "Bob", "Charlie")));
        group.addExpense(new Expense("Coffee", "Charlie", 777, List.of("Bob", "Charlie")));

        long total = group.balances().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(0, total);
    }

    @Test
    void rejectsDuplicateMembersAndPeopleOutsideTheGroup() {
        assertThrows(IllegalArgumentException.class, () -> group.addMember("Alice"));
        assertThrows(IllegalArgumentException.class,
                () -> group.addExpense(new Expense("Lunch", "Dave", 1000, List.of("Alice"))));
        assertThrows(IllegalArgumentException.class,
                () -> group.addExpense(new Expense("Lunch", "Alice", 1000, List.of("Dave"))));
    }

    @Test
    void removingAnExpenseUpdatesBalances() {
        group.addExpense(new Expense("Dinner", "Alice", 6000, List.of("Alice", "Bob", "Charlie")));
        group.removeExpense(0);

        assertEquals(Map.of("Alice", 0L, "Bob", 0L, "Charlie", 0L), group.balances());
    }
}