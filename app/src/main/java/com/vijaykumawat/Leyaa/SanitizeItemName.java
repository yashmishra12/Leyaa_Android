package com.vijaykumawat.Leyaa;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SanitizeItemName {

    public static String[] assetName = {"almond", "apple", "avocado", "bagel", "banana",
            "bandAid", "battery", "beer", "bellpepper", "biscuit", "bodylotion",
            "bodywash", "boot", "bottle", "bread", "broccoli", "broom", "bucket",
            "bugspray", "bulb", "butter", "cabbage", "calculator", "candle"};

    public static String[] realAssetName = {"Almond", "Apple", "Avocado", "Bagel", "Banana", "BandAid",
            "Battery", "Beer", "Bell Pepper", "Biscuit", "Body Lotion", "Body Wash", "Boots", "Bottle",
            "Bread", "Broccoli", "Broom", "Bucket", "Bug Spray", "Bulb", "Butter", "Cabbage", "Calculator",
    "Candle"};

    Stream<String> assetNameSanitized = Arrays.stream(assetName).map(SanitizeItemName::sanitizeItemName);

    public static String sanitizeItemName(String str ) {
        str = str.toLowerCase(Locale.ROOT);
        str = str.trim();
        str = str.replaceAll("\\s", "");
        if (str.endsWith("s")) {
            str = str.substring(0, str.length()-1);
        }
        List<String> alphanumeric = Arrays.asList("q", "w", "e", "r", "t", "y", "u", "i",
                                                    "o", "p", "l", "k", "j", "h", "g", "f",
                                                    "d", "s", "a", "z", "x", "c", "v", "b",
                                                    "n", "m", "1", "2", "3", "4", "5", "6",
                                                    "7", "8", "9", "0");

        String[] strChar = str.split("");

        for(String s: strChar) {
            if (!alphanumeric.contains(s)) {
                String firstLetter = strChar[0];
                if (alphanumeric.contains(firstLetter)) {
                    return firstLetter;
                }else {
                    return "imagenotfound";
                }
            }
        }

        return str;
    }
}
