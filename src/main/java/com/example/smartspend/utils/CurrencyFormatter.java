package com.example.smartspend.utils;
import java.text.DecimalFormat;

public class CurrencyFormatter {
    public static String format(double amount) {
        DecimalFormat formatter = new DecimalFormat("$#,##0.00");
        return formatter.format(amount);
    }
}