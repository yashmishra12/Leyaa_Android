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
            "bandaid", "battery", "beer", "bellpepper", "biscuit", "bodylotion",
            "bodywash", "boot", "bottle", "bread", "broccoli", "broom", "bucket",
            "bugspray", "bulb", "butter", "cabbage", "calculator", "candle", "canopener",
            "cap", "carrot", "cashew", "cauliflower", "cereal", "cheese", "cherry",
            "chicken", "chilli", "chip", "chocolate", "choppingboard", "cigarette",
            "cleaner", "clock", "clove", "coffee", "cola", "conditioner", "condom",
            "contactlen", "converse", "cookie", "cookingoil", "corn", "cornflake", "croc",
            "cucumber", "deodorant", "detergent", "dishwasher", "donut", "egg",
            "eggplant", "energydrink", "facewash", "fish", "flour", "flower", "fork",
            "formalshoe", "freshener", "garlic", "ginger", "glove", "glue",
            "grape", "guava", "handwash", "hat", "highheel", "honey", "icecream",
            "jalapeno", "jam", "jelly", "juice", "ketchup", "kitchenroll", "knife",
            "lamp", "lemon", "lentil", "lobster", "lube", "mango", "meat",
            "milk", "mitten", "mop", "mouthwash", "mug", "musclecream", "mushroom",
            "nailclipper", "nailpolish", "napkin", "notebook", "okra", "onion",
            "orange", "pad", "pea", "peanutbutter", "pen", "pencil", "pineapple",
            "plate", "pomegranate", "popcorn", "potato", "pulse", "pumpkin",
            "radish", "rainboot", "ramen", "rice", "rum", "runningshoe", "salt",
            "sanitizer", "sausage", "scissor", "shampoo", "shoe", "slipper",
            "smoke", "smoothie", "snack", "soap", "soda", "spinach",
            "spoon", "sportsshoe", "squash", "stirfry", "strawberry", "studylamp",
            "sugar", "sunglasse", "sunscreen", "sweetpotato", "tampon", "tea",
            "thermometer", "tissuepaper", "toiletbrush", "toiletpaper", "tomato",
            "toothbrush", "toothpaste", "tortilla", "trashbag", "tuna", "turkey",
            "turnip", "vacuumcleaner", "vodka", "watch", "watermelon", "wetwipe",
            "whiskey", "wine", "yogurt"};

    public static String[] realAssetName = {"Almond", "Apple", "Avocado", "Bagel", "Banana", "BandAid",
            "Battery", "Beer", "Bell Pepper", "Biscuit", "Body Lotion", "Body Wash", "Boots", "Bottle",
            "Bread", "Broccoli", "Broom", "Bucket", "Bug Spray", "Bulb", "Butter", "Cabbage", "Calculator",
            "Candle", "Can Opener", "Cap", "Carrot", "Cashew", "Cauliflower", "Cereal", "Cheese", "Cherry", "Chicken",
            "Chilli", "Chips", "Chocolate", "Chopping Board", "Cigarette", "Cleaner", "Clock", "Clove", "Coffee", "Cola",
            "Conditioner", "Condom", "Contact Lens", "Converse", "Cookie", "Cooking Oil", "Corn", "Corn Flake", "Crocs", "Cucumber",
            "Deodorant", "Detergent", "Dish Washer", "Donut", "Egg", "Eggplant", "Energy Drink", "Face Wash", "Fish", "Flour",
            "Flower", "Fork", "Formal Shoes", "Freshener", "Garlic", "Ginger", "Gloves", "Glue", "Grapes", "Guava", "Hand Wash",
            "Hat", "High Heels", "Honey", "Ice Cream", "Jalapeno", "Jam", "Jelly", "Juice", "Ketchup", "Kitchen Roll", "Knife",
            "Lamp", "Lemon", "Lentil", "Lobster", "Lube", "Mango", "Meat", "Milk", "Mittens", "Mop", "Mouth Wash", "Mug",
    "Muscle Cream", "Mushroom", "Nail Clipper", "Nail Polish", "Napkin", "Notebook", "Okra", "Onion", "Orange", "Pad", "Pea",
    "Peanut Butter", "Pen", "Pencil", "Pineapple", "Plate", "Pomegranate", "Popcorn", "Potato", "Pulse", "Pumpkin", "Radish", "Rain Boots",
    "Ramen", "Rice", "Rum", "Running Shoes", "Salt", "Sanitizer", "Sausage", "Scissors", "Shampoo", "Shoes", "Slippers", "Smoke",
    "Smoothie", "Snack", "Soap", "Soda", "Spinach", "Spoon", "Sports Shoes", "Squash", "Stir Fry", "Strawberry", "Study Lamp",
    "Sugar", "Sun Glasses", "Sun Screen", "Sweet Potato", "Tampon", "Tea", "Thermometer", "Tissue Paper", "Toilet Brush", "Toilet Paper",
    "Tomato", "Toothbrush", "Toothpaste", "Tortilla", "Trash Bag", "Tuna", "Turkey", "Turnip", "Vacuum Cleaner", "Vodka", "Watch", "Watermelon",
    "Wet Wipes", "Whiskey", "Wine", "Yogurt"};

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

        if (Arrays.asList(assetName).contains(str)) {
            return str;
        }
        else if (alphanumeric.contains(str.substring(0,1))) {
            String letter = str.substring(0,1);
            switch (letter) {
                case "0":
                    return "zero";
                case "1":
                    return "one";
                case "2":
                    return "two";
                case "3":
                    return "three";
                case "4":
                    return "four";
                case "5":
                    return "five";
                case "6":
                    return "six";
                case "7":
                    return "seven";
                case "8":
                    return "eight";
                case "9":
                    return "nine";
                default:
                    return letter;
            }
        } else {
            return "imagenotfound";
        }


    }
}
