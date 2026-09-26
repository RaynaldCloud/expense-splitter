package com.raynald.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void parsesWholeAndDecimalAmountsIntoCents() {
        assertEquals(1200, Money.parse("12"));
        assertEquals(1250, Money.parse("12.5"));
        assertEquals(5, Money.parse("0.05"));
        assertEquals(720, Money.parse(" 7.20 "));
    }

    @Test
    void rejectsInvalidAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("0"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("-5"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("5.555"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse(""));
    }

    @Test
    void formatsCentsAsDollars() {
        assertEquals("S$12.50", Money.format(1250));
        assertEquals("S$0.05", Money.format(5));
        assertEquals("-S$5.00", Money.format(-500));
    }
}