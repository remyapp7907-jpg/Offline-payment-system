package com.offlinepay.model;

import java.time.LocalDate;

/**
 * Represents a simulated Bank-Authorized Spending Token.
 * 
 * In real-world offline payment designs (such as RBI offline digital rupee or CBDC frameworks),
 * a user pre-fetches a cryptographically signed token with an offline spending quota.
 * 
 * NOTE (Educational Simulation):
 * This is an educational proof-of-concept simulation and does not contain proprietary
 * hardware-level secure enclave keys or production bank cryptographic signatures.
 */
public class SpendingToken {
    private final String tokenId;
    private final String customerId;
    private final String customerName;
    private final String issuingBank;
    private final double maxAllowance;
    private double remainingBalance;
    private final LocalDate expiryDate;
    private final String simulatedBankSignature;

    public SpendingToken(String tokenId, String customerId, String customerName, 
                         String issuingBank, double maxAllowance, double remainingBalance, 
                         LocalDate expiryDate, String simulatedBankSignature) {
        this.tokenId = tokenId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.issuingBank = issuingBank;
        this.maxAllowance = maxAllowance;
        this.remainingBalance = remainingBalance;
        this.expiryDate = expiryDate;
        this.simulatedBankSignature = simulatedBankSignature;
    }

    public boolean canSpend(double amount) {
        return amount > 0 && amount <= remainingBalance;
    }

    public synchronized boolean deduct(double amount) {
        if (canSpend(amount)) {
            remainingBalance -= amount;
            return true;
        }
        return false;
    }

    public synchronized void refund(double amount) {
        remainingBalance = Math.min(maxAllowance, remainingBalance + amount);
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public double getMaxAllowance() {
        return maxAllowance;
    }

    public synchronized double getRemainingBalance() {
        return remainingBalance;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getSimulatedBankSignature() {
        return simulatedBankSignature;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Balance: ₹%.2f / ₹%.2f", 
                tokenId, issuingBank, remainingBalance, maxAllowance);
    }
}
