package com.tripos.pos.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Shared currency-formatting helper — Sri Lankan Rupees (Rs.).
 *
 * <p>All monetary amounts displayed in the UI pass through this class so
 * the format is consistent across the cart table, bill summary, and receipt
 * preview.</p>
 *
 * <p>To adapt to a different currency or locale, change {@link #LOCALE} and
 * {@link #SYMBOL} here; no other files need editing.</p>
 */
public final class CurrencyFormatter {

    /**
     * The locale used for number formatting.
     * en_LK gives the right decimal / grouping separators for Sri Lanka.
     */
    private static final Locale LOCALE = new Locale("en", "LK");

    /**
     * Currency symbol prefix.  Rs. is the conventional prefix for
     * Sri Lankan Rupees (LKR) in local usage.
     */
    private static final String SYMBOL = "Rs. ";

    private static final NumberFormat FMT;

    static {
        FMT = NumberFormat.getNumberInstance(LOCALE);
        FMT.setMinimumFractionDigits(2);
        FMT.setMaximumFractionDigits(2);
    }

    private CurrencyFormatter() { /* utility class */ }

    /**
     * Formats {@code amount} as a currency string, e.g. {@code Rs. 1,250.00}.
     */
    public static String format(double amount) {
        return SYMBOL + FMT.format(amount);
    }

    /**
     * Formats {@code amount} as a signed discount, e.g. {@code −Rs. 245.00}.
     * Uses a proper minus sign (U+2212) rather than a hyphen.
     */
    public static String formatDiscount(double amount) {
        return "\u2212" + SYMBOL + FMT.format(amount);
    }
}
