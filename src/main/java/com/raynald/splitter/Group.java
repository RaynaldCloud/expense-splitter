package com.raynald.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A group of people sharing expenses. Works out how much each person is up or down. */
public class Group {

    private final Set<String> members = new LinkedHashSet<>();
    private final List<Expense> expenses = new ArrayList<>();

    public void addMember(String name) {
        String cleaned = (name == null) ? "" : name.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (!members.add(cleaned)) {
            throw new IllegalArgumentException(cleaned + " is already in the group");
        }
    }

    public void addExpense(Expense expense) {
        if (!members.contains(expense.getPaidBy())) {
            throw new IllegalArgumentException(expense.getPaidBy() + " is not in the group");
        }
        for (String person : expense.getSharedBy()) {
            if (!members.contains(person)) {
                throw new IllegalArgumentException(person + " is not in the group");
            }
        }
        expenses.add(expense);
    }

    public void removeExpense(int index) {
        if (index < 0 || index >= expenses.size()) {
            throw new IllegalArgumentException("That expense no longer exists");
        }
        expenses.remove(index);
    }

    /**
     * Each person's balance in cents.
     * Positive: others owe them money. Negative: they owe money. Always adds up to zero.
     */
    public Map<String, Long> balances() {
        Map<String, Long> balances = new LinkedHashMap<>();
        for (String member : members) {
            balances.put(member, 0L);
        }
        for (Expense expense : expenses) {
            // The payer is up by the full amount...
            balances.merge(expense.getPaidBy(), expense.getAmountCents(), Long::sum);
            // ...and each person sharing it is down by their share
            for (Map.Entry<String, Long> share : expense.shares().entrySet()) {
                balances.merge(share.getKey(), -share.getValue(), Long::sum);
            }
        }
        return balances;
    }

    public Set<String> getMembers() { return Collections.unmodifiableSet(members); }
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }
}