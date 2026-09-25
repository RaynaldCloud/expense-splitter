package com.raynald.splitter;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One shared cost: what it was for, who paid, how much, and who shares it. */
public class Expense {

    private final String description;
    private final String paidBy;
    private final long amountCents;
    private final List<String> sharedBy;

    public Expense(String description, String paidBy, long amountCents, List<String> sharedBy) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (sharedBy == null || sharedBy.isEmpty()) {
            throw new IllegalArgumentException("An expense must be shared by at least one person");
        }
        if (new HashSet<>(sharedBy).size() != sharedBy.size()) {
            throw new IllegalArgumentException("Each person can only be listed once");
        }
        this.description = description.trim();
        this.paidBy = paidBy;
        this.amountCents = amountCents;
        this.sharedBy = List.copyOf(sharedBy);
    }

    /**
     * Splits the amount as evenly as possible. When it doesn't divide exactly,
     * the leftover cents go one each to the first people in the list,
     * so the shares always add up to exactly the total.
     */
    
    public Map<String, Long> shares() {
        int people = sharedBy.size();
        long baseShare = amountCents / people;   // e.g. 1000 / 3 = 333
        long leftover = amountCents % people;    // e.g. 1000 % 3 = 1

        Map<String, Long> shares = new LinkedHashMap<>();
        for (int i = 0; i < people; i++) {
            long share = baseShare + (i < leftover ? 1 : 0);
            shares.put(sharedBy.get(i), share);
        }
        return shares;
    }

    public String getDescription() { return description; }
    public String getPaidBy() { return paidBy; }
    public long getAmountCents() { return amountCents; }
    public List<String> getSharedBy() { return sharedBy; }

    @Override
    public String toString() {
        return String.format("%s: %s paid by %s, split between %s",
                description, Money.format(amountCents), paidBy, String.join(", ", sharedBy));
    }
}