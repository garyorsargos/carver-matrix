package com.fmc.starterApp.utils;

import java.util.Random;

public class AppUtils {

    public String randomKey(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String randomString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        randomString = randomString.replaceAll("\\?", "x");
        randomString = randomString.replaceAll("%", "s");
        randomString = randomString.replaceAll("&", "5");
        randomString = randomString.replaceAll("=", "j");


        return randomString;
    }
}