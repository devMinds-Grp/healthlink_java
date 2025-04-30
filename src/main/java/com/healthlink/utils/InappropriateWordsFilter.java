package com.healthlink.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InappropriateWordsFilter {
    private static final Set<String> INAPPROPRIATE_WORDS = new HashSet<>(Arrays.asList(
            // Liste de mots inappropriés (à personnaliser selon vos besoins)
            "insulte", "grossier", "offensant", "vulgaire",
            // Ajoutez ici d'autres mots interdits
            "idiot","merde", "stupide"
    ));

    public static boolean containsInappropriateWords(String text) {
        if (text == null) return false;

        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (INAPPROPRIATE_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstInappropriateWord(String text) {
        if (text == null) return null;

        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (INAPPROPRIATE_WORDS.contains(word)) {
                return word;
            }
        }
        return null;
    }
}