package com.raynald.splitter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private Money() {}  // only has static helpers, so it's never created as an object

    /** "12.5" becomes 1250. Rejects zero, negatives, and more than 2 decimal places. */
    public static long parse(String text) {
        BigDecimal dollars;
        try {
            dollars = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid amount");
        }
        if (dollars.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        try {
            return dollars.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount can have at most 2 decimal places");
        }
    }

    /** 1250 becomes "S$12.50", and -500 becomes "-S$5.00". */
    public static String format(long cents) {
        String sign = cents < 0 ? "-" : "";
        long abs = Math.abs(cents);
        return String.format("%sS$%d.%02d", sign, abs / 100, abs % 100);
    }
}