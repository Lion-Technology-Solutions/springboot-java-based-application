package com.liontech.resorts.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liontech.resorts.service.LanguageService;

@RestController
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping("/languages")
    public Map<String, Object> languages() {
        return Map.of(
            "languages", languageService.supportedLanguages(),
            "currencies", languageService.supportedCurrencies(),
            "countries", languageService.supportedCountries()
        );
    }
}
