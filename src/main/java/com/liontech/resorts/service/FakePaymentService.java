package com.liontech.resorts.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.liontech.resorts.dto.PaymentRequest;

@Service
public class FakePaymentService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom secureRandom = new SecureRandom();

    public record PaymentResult(boolean approved, String transactionId, String maskedCard, String message) {
    }

    public PaymentResult authorize(PaymentRequest request, BigDecimal amount, String currency) {
        String digits = request.getCardNumber().replaceAll("\\s+", "");
        String maskedCard = mask(digits);
        boolean approved = digits.length() >= 12 && digits.length() <= 19 && !digits.endsWith("0000");

        if (!approved) {
            return new PaymentResult(
                false,
                transactionId("DECLINED"),
                maskedCard,
                "Fake payment declined. Use any test card number that does not end in 0000."
            );
        }

        return new PaymentResult(
            true,
            transactionId("LTR"),
            maskedCard,
            "Approved " + currency + " " + amount + " using LionTech FakePay gateway."
        );
    }

    private String transactionId(String prefix) {
        StringBuilder builder = new StringBuilder(prefix).append("-");
        for (int i = 0; i < 10; i++) {
            builder.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    private String mask(String digits) {
        if (digits.length() <= 4) {
            return "****";
        }
        String lastFour = digits.substring(digits.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
