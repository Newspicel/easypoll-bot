package de.fbrettnich.easypoll.utils;

import java.text.DecimalFormat;

public class FormatUtil {

    /**
     * Format number to decimal number with '.'
     *
     * @param number custom int
     * @return number in decimal format with '.'
     */
    public static String decimalFormat(int number) {
        DecimalFormat decimalFormat = new DecimalFormat();
        return decimalFormat.format(number).replace(",", ".");
    }
}
