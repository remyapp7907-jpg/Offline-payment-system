package com.offlinepay.service;

import com.offlinepay.model.TransactionRecord;
import com.offlinepay.model.TransactionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory store for transaction ledger records.
 * 
 * IMPORTANT LIMITATION (Educational Simulation):
 * In this educational version, transaction records are stored in memory.
 * A production offline device would store these in secure local flash memory (e.g. SQLite / encrypted store)
 * until network connectivity allows batch upload.
 */
public class TransactionStore {
    public interface StoreListener {
        void onStoreUpdated();
    }

    private final List<TransactionRecord> records = new CopyOnWriteArrayList<>();
    private final List<StoreListener> listeners = new CopyOnWriteArrayList<>();

    public void addRecord(TransactionRecord record) {
        records.add(0, record); // newest first
        notifyListeners();
    }

    public List<TransactionRecord> getAllRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public void addListener(StoreListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StoreListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (StoreListener listener : listeners) {
            listener.onStoreUpdated();
        }
    }

    /**
     * Simulates batch upload and settlement of offline transactions with the central bank server.
     * Transitions all PENDING_SYNC transactions to SYNCED.
     *
     * @return Number of transactions synced
     */
    public synchronized int simulateBankSync() {
        int count = 0;
        for (TransactionRecord record : records) {
            if (record.getStatus() == TransactionStatus.PENDING_SYNC) {
                record.markSynced();
                count++;
            }
        }
        if (count > 0) {
            notifyListeners();
        }
        return count;
    }

    public int getPendingCount() {
        int count = 0;
        for (TransactionRecord r : records) {
            if (r.getStatus() == TransactionStatus.PENDING_SYNC) {
                count++;
            }
        }
        return count;
    }

    public int getSyncedCount() {
        int count = 0;
        for (TransactionRecord r : records) {
            if (r.getStatus() == TransactionStatus.SYNCED) {
                count++;
            }
        }
        return count;
    }

    public int getRejectedCount() {
        int count = 0;
        for (TransactionRecord r : records) {
            if (r.getStatus() == TransactionStatus.REJECTED) {
                count++;
            }
        }
        return count;
    }

    public double getTotalAcceptedVolume() {
        double sum = 0;
        for (TransactionRecord r : records) {
            if (r.getStatus() != TransactionStatus.REJECTED) {
                sum += r.getAmount();
            }
        }
        return sum;
    }

    public void clearAll() {
        records.clear();
        notifyListeners();
    }
}
