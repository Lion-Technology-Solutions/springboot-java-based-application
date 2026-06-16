package com.liontech.resorts.web;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.liontech.resorts.dto.RegisterRequest;
import com.liontech.resorts.service.AccountService;
import com.liontech.resorts.service.BusinessException;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(
        @Valid @ModelAttribute RegisterRequest registerRequest,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            accountService.registerGuest(registerRequest);
        } catch (BusinessException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your LionTech Resorts account is ready. Please sign in to book.");
        return "redirect:/login";
    }
}
