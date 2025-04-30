package com.healthlink.utils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationService {
    private static TranslationService instance;
    private final DeepLTranslationService deepLService;
    private Locale currentLocale;
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    private TranslationService(String deepLApiKey) {
        this.deepLService = new DeepLTranslationService(deepLApiKey);
        this.currentLocale = Locale.ENGLISH;
    }

    public static synchronized TranslationService getInstance(String deepLApiKey) {
        if (instance == null) {
            instance = new TranslationService(deepLApiKey);
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        if (!locale.equals(this.currentLocale)) {
            this.currentLocale = locale;
            translationCache.clear(); // Clear cache when language changes
        }
    }

    public String getTranslation(String text) {
        // Check cache first
        if (translationCache.containsKey(text)) {
            return translationCache.get(text);
        }

        try {
            String targetLang = currentLocale.getLanguage().toUpperCase();
            String translated = deepLService.translate(text, targetLang);
            translationCache.put(text, translated);
            return translated;
        } catch (Exception e) {
            System.err.println("DeepL translation failed: " + e.getMessage());
            return text; // Fallback to original text
        }
    }
}