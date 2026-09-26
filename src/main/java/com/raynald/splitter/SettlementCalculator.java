package com.raynald.splitter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class SettlementCalculator {

    private SettlementCalculator() {}

    /** A person and an amount: how much they're owed, or how much they owe. */
    private record Party(String name, long amount) {}

    public static List<Payment> settle(Map<String, Long> balances) {
        // Largest amount first; ties broken by name so the result is always the same
        Comparator<Party> largestFirst = Comparator.comparingLong(Party::amount).reversed()
                .thenComparing(Party::name);

        PriorityQueue<Party> owedMoney = new PriorityQueue<>(largestFirst);
        PriorityQueue<Party> oweMoney = new PriorityQueue<>(largestFirst);

        balances.forEach((name, cents) -> {
            if (cents > 0) {
                owedMoney.add(new Party(name, cents));
            } else if (cents < 0) {
                oweMoney.add(new Party(name, -cents));  // store as a positive amount
            }
            // cents == 0: already settled, nothing to do
        });

        List<Payment> payments = new ArrayList<>();
        while (!owedMoney.isEmpty() && !oweMoney.isEmpty()) {
            Party creditor = owedMoney.poll();  // owed the most
            Party debtor = oweMoney.poll();     // owes the most

            long amount = Math.min(creditor.amount(), debtor.amount());
            payments.add(new Payment(debtor.name(), creditor.name(), amount));

            // Whoever isn't fully settled goes back in line with what's left
            if (creditor.amount() > amount) {
                owedMoney.add(new Party(creditor.name(), creditor.amount() - amount));
            }
            if (debtor.amount() > amount) {
                oweMoney.add(new Party(debtor.name(), debtor.amount() - amount));
            }
        }
        return payments;
    }
}