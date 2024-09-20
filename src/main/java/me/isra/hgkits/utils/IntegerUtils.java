package me.isra.hgkits.utils;

public final class IntegerUtils {
    public static int parsePositive(String text, int errorDefaultValue) {
        if (text == null) {
            return 1; 
        }

        int length = text.length();
        if (length == 1) {
            length = text.charAt(0) - 48;
            return (length > 9 || length < 0) ? errorDefaultValue : length;
        } 

        int result = 0;
        for (int i = 0; i < length; i++) {
            int value = text.charAt(i) - 48;
            if (value > 9 || value < 0) {
                return errorDefaultValue;
            }
            result = (result + value) * 10;
        } 
        return result / 10;
    }
}