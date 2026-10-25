package com.bughunters.posclient.util;

import java.math.BigDecimal;

public class CurrencyFormatter {
    public static String format(BigDecimal amount) {
        return amount != null ? amount.toString() : "0.00";
    }
}
