package com.liontech.resorts.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liontech.resorts.domain.AccountRole;
import com.liontech.resorts.domain.UserAccount;
import com.liontech.resorts.dto.RegisterRequest;
import com.liontech.resorts.repository.UserAccountRepository;

@Service
public class AccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final LanguageService languageService;

    public AccountService(
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        LanguageService languageService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.languageService = languageService;
    }

    @Transactional
    public UserAccount registerGuest(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("An account already exists for this email address.");
        }

        String language = languageService.isSupportedLanguage(request.getPreferredLanguage())
            ? request.getPreferredLanguage().toLowerCase(Locale.ROOT)
            : "en";

        UserAccount account = new UserAccount();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setFirstName(request.getFirstName().trim());
        account.setLastName(request.getLastName().trim());
        account.setCountry(request.getCountry().trim());
        account.setPreferredLanguage(language);
        account.setRole(AccountRole.GUEST);
        account.setEnabled(true);
        return userAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return userAccountRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("Account not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(normalizeEmail(username))
            .orElseThrow(() -> new UsernameNotFoundException("Account not found."));

        return User.withUsername(account.getEmail())
            .password(account.getPasswordHash())
            .roles(account.getRole().name())
            .disabled(!account.isEnabled())
            .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
