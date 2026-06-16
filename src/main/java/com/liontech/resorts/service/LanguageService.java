package com.liontech.resorts.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class LanguageService {

    public record LanguageOption(String code, String name, String nativeName) {
    }

    public record CurrencyOption(String code, String label) {
    }

    public List<LanguageOption> supportedLanguages() {
        return List.of(
            new LanguageOption("en", "English", "English"),
            new LanguageOption("fr", "French", "Francais"),
            new LanguageOption("es", "Spanish", "Espanol"),
            new LanguageOption("ht", "Haitian Creole", "Kreyol Ayisyen")
        );
    }

    public List<CurrencyOption> supportedCurrencies() {
        return List.of(
            new CurrencyOption("USD", "USD - United States Dollar"),
            new CurrencyOption("CAD", "CAD - Canadian Dollar"),
            new CurrencyOption("EUR", "EUR - Euro"),
            new CurrencyOption("GBP", "GBP - British Pound"),
            new CurrencyOption("HTG", "HTG - Haitian Gourde")
        );
    }

    public List<String> supportedCountries() {
        return List.of(
            "United States",
            "Canada",
            "Haiti",
            "Dominican Republic",
            "Mexico",
            "Brazil",
            "Colombia",
            "United Kingdom",
            "France",
            "Spain",
            "Germany",
            "Italy",
            "Netherlands",
            "Nigeria",
            "South Africa",
            "Ghana",
            "Kenya",
            "United Arab Emirates",
            "Saudi Arabia",
            "India",
            "China",
            "Japan",
            "South Korea",
            "Australia",
            "New Zealand",
            "Jamaica",
            "Bahamas",
            "Turks and Caicos",
            "Barbados",
            "Other"
        );
    }

    public boolean isSupportedLanguage(String code) {
        return supportedLanguages().stream().anyMatch(language -> language.code().equalsIgnoreCase(code));
    }
}
