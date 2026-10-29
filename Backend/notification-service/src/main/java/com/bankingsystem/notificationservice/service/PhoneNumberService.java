package com.bankingsystem.notificationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberService {

    private final String defaultCountryCode;

    public PhoneNumberService(
            @Value(
                    "${banking.sms.default-country-code:+91}"
            )
            String defaultCountryCode
    ) {
        this.defaultCountryCode =
                normalizeCountryCode(
                        defaultCountryCode
                );
    }

    public String normalize(
            String phoneNumber
    ) {
        if (phoneNumber == null ||
                phoneNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Phone number is required"
            );
        }

        String normalized =
                phoneNumber
                        .trim()
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("(", "")
                        .replace(")", "");

        if (normalized.startsWith("00")) {
            normalized =
                    "+" +
                            normalized.substring(2);
        }

        if (normalized.startsWith("+")) {
            validateInternationalNumber(
                    normalized
            );

            return normalized;
        }

        /*
         * Handle Indian numbers stored as:
         * 9876543210
         */
        if (normalized.matches("\\d{10}")) {
            normalized =
                    defaultCountryCode +
                            normalized;

            validateInternationalNumber(
                    normalized
            );

            return normalized;
        }

        /*
         * Handle Indian numbers stored as:
         * 919876543210
         */
        if (normalized.matches("91\\d{10}")) {
            normalized =
                    "+" +
                            normalized;

            validateInternationalNumber(
                    normalized
            );

            return normalized;
        }

        throw new IllegalArgumentException(
                "Phone number must use a valid " +
                        "international format"
        );
    }

    public String mask(
            String phoneNumber
    ) {
        if (phoneNumber == null ||
                phoneNumber.isBlank()) {

            return "****";
        }

        String digits =
                phoneNumber.replaceAll(
                        "\\D",
                        ""
                );

        if (digits.length() <= 4) {
            return "****";
        }

        return "******" +
                digits.substring(
                        digits.length() - 4
                );
    }

    private void validateInternationalNumber(
            String phoneNumber
    ) {
        if (!phoneNumber.matches(
                "\\+[1-9]\\d{7,14}"
        )) {
            throw new IllegalArgumentException(
                    "Phone number is not in a valid " +
                            "international format"
            );
        }
    }

    private String normalizeCountryCode(
            String countryCode
    ) {
        if (countryCode == null ||
                countryCode.isBlank()) {

            throw new IllegalStateException(
                    "Default country code is required"
            );
        }

        String normalized =
                countryCode.trim();

        if (!normalized.startsWith("+")) {
            normalized =
                    "+" +
                            normalized;
        }

        if (!normalized.matches(
                "\\+[1-9]\\d{0,3}"
        )) {
            throw new IllegalStateException(
                    "Default country code is invalid"
            );
        }

        return normalized;
    }
}