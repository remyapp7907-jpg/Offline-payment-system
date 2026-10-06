package com.offlinepay.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Standardized data payload encoded into the offline QR code.
 * 
 * In real interoperable offline payment protocols, this structure holds
 * the transaction parameters and digital signature exchanged between customer and merchant.
 * 
 * IMPORTANT LIMITATION (Educational Simulation):
 * The authorization hash in this class is a simple simulated SHA-256 digest
 * meant for demonstration purposes and is NOT cryptographically secure against
 * tampering in a real-world adversarial banking environment.
 */
public class PaymentPayload {
    public static final String PROTOCOL_HEADER = "OFFLINE_PAY_V1";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String txId;
    private final String tokenId;
    private final String customerId;
    private final String customerName;
    private final String merchantName;
    private final double amount;
    private final String currency;
    private final String timestamp;
    private final String simulatedAuthDigest;

    public PaymentPayload(String txId, String tokenId, String customerId, 
                          String customerName, String merchantName, double amount, 
                          String currency, String timestamp, String simulatedAuthDigest) {
        this.txId = txId;
        this.tokenId = tokenId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.merchantName = merchantName;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.simulatedAuthDigest = simulatedAuthDigest;
    }

    /**
     * Factory method to generate a new simulated offline payment payload.
     */
    public static PaymentPayload create(SpendingToken token, String merchantName, double amount) {
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String currency = "INR";
        String authDigest = generateSimulatedDigest(txId, token.getTokenId(), merchantName, amount, timestamp);

        return new PaymentPayload(
                txId,
                token.getTokenId(),
                token.getCustomerId(),
                token.getCustomerName(),
                merchantName.trim(),
                amount,
                currency,
                timestamp,
                authDigest
        );
    }

    /**
     * Serializes this payload into a pipe-delimited format suitable for QR code encoding.
     */
    public String toQrString() {
        return String.join("|",
                PROTOCOL_HEADER,
                escape(txId),
                escape(tokenId),
                escape(customerId),
                escape(customerName),
                escape(merchantName),
                String.format("%.2f", amount),
                escape(currency),
                escape(timestamp),
                escape(simulatedAuthDigest)
        );
    }

    /**
     * Parses a raw QR code string into a PaymentPayload.
     * Throws IllegalArgumentException if the QR content does not follow the format.
     */
    public static PaymentPayload parse(String rawQrContent) throws IllegalArgumentException {
        if (rawQrContent == null || rawQrContent.trim().isEmpty()) {
            throw new IllegalArgumentException("QR content is empty.");
        }

        String[] parts = rawQrContent.split("\\|", -1);
        if (parts.length < 10) {
            throw new IllegalArgumentException("Invalid QR code format for OfflinePaymentSystem. " +
                    "Expected 10 segments, found: " + parts.length);
        }

        if (!PROTOCOL_HEADER.equals(parts[0])) {
            throw new IllegalArgumentException("Unrecognized protocol header: '" + parts[0] + 
                    "'. Expected: " + PROTOCOL_HEADER);
        }

        String txId = unescape(parts[1]);
        String tokenId = unescape(parts[2]);
        String customerId = unescape(parts[3]);
        String customerName = unescape(parts[4]);
        String merchantName = unescape(parts[5]);
        double amount;
        try {
            amount = Double.parseDouble(parts[6]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount in QR data: " + parts[6]);
        }
        String currency = unescape(parts[7]);
        String timestamp = unescape(parts[8]);
        String authDigest = unescape(parts[9]);

        return new PaymentPayload(txId, tokenId, customerId, customerName, 
                                  merchantName, amount, currency, timestamp, authDigest);
    }

    /**
     * Helper to compute a simulated hash for the educational demonstration.
     */
    public static String generateSimulatedDigest(String txId, String tokenId, String merchant, double amount, String ts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = txId + ":" + tokenId + ":" + merchant + ":" + amount + ":" + ts + ":SIMULATED_SALT";
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return "SIM-AUTH-" + Math.abs(txId.hashCode());
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("|", "_");
    }

    private static String unescape(String s) {
        return s == null ? "" : s;
    }

    public String getTxId() {
        return txId;
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

    public String getMerchantName() {
        return merchantName;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSimulatedAuthDigest() {
        return simulatedAuthDigest;
    }
}
