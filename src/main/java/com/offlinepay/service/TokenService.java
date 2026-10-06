package com.offlinepay.service;

import com.offlinepay.model.SpendingToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service to manage simulated Bank-Authorized Spending Tokens.
 * 
 * Interoperability Concept:
 * Tokens issued by diverse banks (SBI, HDFC, Canara) adhere to a shared offline standard,
 * allowing any participating merchant to accept them offline without real-time bank routing.
 */
public class TokenService {
    private final List<SpendingToken> tokens = new ArrayList<>();
    private SpendingToken activeToken;

    public TokenService() {
        initializeSampleTokens();
    }

    private void initializeSampleTokens() {
        tokens.clear();
        tokens.add(new SpendingToken(
                "TOK-SBI-8821",
                "CUST-RAHUL-101",
                "Rahul Verma",
                "State Bank of India (SBI)",
                5000.00,
                3500.00,
                LocalDate.now().plusMonths(3),
                "SIG-RSA2048-SBI-OFFLINE-AUTH-098A"
        ));

        tokens.add(new SpendingToken(
                "TOK-HDFC-4109",
                "CUST-PRIYA-202",
                "Priya Nair",
                "HDFC Bank",
                10000.00,
                7250.00,
                LocalDate.now().plusMonths(6),
                "SIG-RSA2048-HDFC-OFFLINE-AUTH-551C"
        ));

        tokens.add(new SpendingToken(
                "TOK-CANARA-1234",
                "CUST-AMAL-303",
                "Amal Joseph",
                "Canara Bank",
                3000.00,
                1800.00,
                LocalDate.now().plusMonths(2),
                "SIG-RSA2048-CANARA-OFFLINE-AUTH-882E"
        ));

        // Default active token is the first one
        activeToken = tokens.get(0);
    }

    public List<SpendingToken> getAvailableTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public SpendingToken getActiveToken() {
        return activeToken;
    }

    public void setActiveToken(SpendingToken token) {
        this.activeToken = token;
    }

    public SpendingToken findTokenById(String tokenId) {
        for (SpendingToken token : tokens) {
            if (token.getTokenId().equalsIgnoreCase(tokenId)) {
                return token;
            }
        }
        return null;
    }

    public void resetTokens() {
        initializeSampleTokens();
    }
}
