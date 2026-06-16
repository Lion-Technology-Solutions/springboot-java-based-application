package com.liontech.resorts.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.liontech.resorts.service.LanguageService;

@ControllerAdvice
public class GlobalModelAdvice {

    private final LanguageService languageService;

    public GlobalModelAdvice(LanguageService languageService) {
        this.languageService = languageService;
    }

    @ModelAttribute("supportedLanguages")
    public Object supportedLanguages() {
        return languageService.supportedLanguages();
    }

    @ModelAttribute("supportedCurrencies")
    public Object supportedCurrencies() {
        return languageService.supportedCurrencies();
    }

    @ModelAttribute("supportedCountries")
    public Object supportedCountries() {
        return languageService.supportedCountries();
    }
}
