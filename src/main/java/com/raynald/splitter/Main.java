package com.raynald.splitter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Group group = new Group();
        group.addMember("Alice");
        group.addMember("Bob");
        group.addMember("Charlie");

        group.addExpense(new Expense("Dinner", "Alice", Money.parse("60"),
                List.of("Alice", "Bob", "Charlie")));
        group.addExpense(new Expense("Taxi", "Bob", Money.parse("15"),
                List.of("Bob", "Charlie")));

        System.out.println("Expenses:");
        group.getExpenses().forEach(e -> System.out.println("  " + e));

        System.out.println("Balances:");
        group.balances().forEach((person, cents) -> {
            String status = cents > 0 ? "is owed " + Money.format(cents)
                          : cents < 0 ? "owes " + Money.format(-cents)
                          : "is settled";
            System.out.println("  " + person + " " + status);
        });
    }
}