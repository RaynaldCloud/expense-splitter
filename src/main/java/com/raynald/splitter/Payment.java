package com.raynald.splitter;

public record Payment(String from, String to, long amountCents) {

    @Override
    public String toString() {
        return from + " pays " + to + " " + Money.format(amountCents);
    }
}