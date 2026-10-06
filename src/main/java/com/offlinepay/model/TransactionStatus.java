package com.offlinepay.model;

/**
 * Represents the lifecycle status of an offline payment transaction.
 * In an interoperable offline payment system, transactions start as PENDING_SYNC
 * while offline, and transition to SYNCED when reconnected to the core banking system.
 */
public enum TransactionStatus {
    PENDING_SYNC("Pending Sync", "Offline payment recorded locally; awaiting bank server connection"),
    SYNCED("Synced & Settled", "Transaction batch synced with central banking ledger"),
    REJECTED("Rejected", "Declined by merchant during offline verification");

    private final String displayName;
    private final String description;

    TransactionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
