package com.liontech.resorts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    @NotBlank
    @Size(max = 120)
    private String cardholderName;

    @NotBlank
    @Pattern(regexp = "[0-9 ]{12,23}", message = "Use a fake card number with 12 to 19 digits")
    private String cardNumber;

    @NotBlank
    @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{2}", message = "Use MM/YY")
    private String expiry;

    @NotBlank
    @Pattern(regexp = "[0-9]{3,4}", message = "Use 3 or 4 digits")
    private String cvc;

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}
