package com.offlinepay.model;

import java.time.LocalDateTime;

/**
 * In-memory transaction record stored in the local offline ledger.
 * 
 * Demonstrates the "store-and-forward" model of offline transactions:
 * 1. Upon offline merchant approval, the transaction is marked PENDING_SYNC.
 * 2. When the merchant or customer device reconnects to the network,
 *    the transaction is batched and synced with the bank, changing status to SYNCED.
 */
public class TransactionRecord {
    private final String txId;
    private final String timestamp;
    private final String customerId;
    private final String customerName;
    private final String merchantName;
    private final double amount;
    private final String currency;
    private final String tokenId;
    private TransactionStatus status;
    private String remarks;
    private String syncTimestamp;

    public TransactionRecord(String txId, String timestamp, String customerId, 
                             String customerName, String merchantName, double amount, 
                             String currency, String tokenId, TransactionStatus status, 
                             String remarks) {
        this.txId = txId;
        this.timestamp = timestamp;
        this.customerId = customerId;
        this.customerName = customerName;
        this.merchantName = merchantName;
        this.amount = amount;
        this.currency = currency;
        this.tokenId = tokenId;
        this.status = status;
        this.remarks = remarks;
        this.syncTimestamp = "-";
    }

    public static TransactionRecord fromPayload(PaymentPayload payload, TransactionStatus status, String remarks) {
        return new TransactionRecord(
                payload.getTxId(),
                payload.getTimestamp(),
                payload.getCustomerId(),
                payload.getCustomerName(),
                payload.getMerchantName(),
                payload.getAmount(),
                payload.getCurrency(),
                payload.getTokenId(),
                status,
                remarks
        );
    }

    public synchronized void markSynced() {
        this.status = TransactionStatus.SYNCED;
        this.syncTimestamp = LocalDateTime.now().format(PaymentPayload.DATE_FORMATTER);
        this.remarks = "Settled with central bank clearing ledger";
    }

    public String getTxId() {
        return txId;
    }

    public String getTimestamp() {
        return timestamp;
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

    public String getTokenId() {
        return tokenId;
    }

    public synchronized TransactionStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public synchronized String getRemarks() {
        return remarks;
    }

    public synchronized void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public synchronized String getSyncTimestamp() {
        return syncTimestamp;
    }
}
