package com.apiCodeGenerator.common.apputil.utils.commonutil;

import java.security.SecureRandom;

public class PasswordGeneratorUtil {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?/";

    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword(int length, boolean lower, boolean upper, boolean digits, boolean symbols) {

        StringBuilder charPool = new StringBuilder();

        if (lower) charPool.append(LOWER);
        if (upper) charPool.append(UPPER);
        if (digits) charPool.append(DIGITS);
        if (symbols) charPool.append(SYMBOLS);

        if (charPool.isEmpty()) {
            throw new IllegalArgumentException("Select at least one character type!");
        }

        String pool = charPool.toString();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(pool.length());
            password.append(pool.charAt(idx));
        }

        return password.toString();
    }
//
//    public static void main(String[] args) {
//        System.out.println(generatePassword(20, true, true, true, true));
//    }
}
