package com.offlinepay.ui;

import com.offlinepay.model.TransactionRecord;
import com.offlinepay.model.TransactionStatus;
import com.offlinepay.service.TransactionStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Transaction History Screen:
 * 1. Displays all recorded transactions with their status (specifically PENDING_SYNC).
 * 2. Provides summary counters (Pending count, Synced count, Total Volume).
 * 3. Includes 'Simulate Bank Sync' to demonstrate how offline store-and-forward batches
 *    are settled when internet connectivity is re-established.
 */
public class HistoryPanel extends JPanel implements TransactionStore.StoreListener {
    private final TransactionStore transactionStore;

    // Summary Metric Labels
    private JLabel totalCountLabel;
    private JLabel pendingCountLabel;
    private JLabel syncedCountLabel;
    private JLabel volumeLabel;

    // Table Components
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    private static final String[] COLUMN_NAMES = {
            "Status", "Transaction ID", "Date & Time", "Customer Name", 
            "Merchant Name", "Amount (₹)", "Token ID", "Ledger Remarks / Settlement"
    };

    public HistoryPanel(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
        this.transactionStore.addListener(this);

        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // TOP: Metrics & Control Header
        JPanel topCard = createTopHeaderCard();
        add(topCard, BorderLayout.NORTH);

        // CENTER: Table Card
        JPanel tableCard = createTableCard();
        add(tableCard, BorderLayout.CENTER);

        // SOUTH: Educational Concept Footer
        JPanel footerCard = createFooterCard();
        add(footerCard, BorderLayout.SOUTH);
    }

    private JPanel createTopHeaderCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        // Title and actions
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel title = new JLabel("Transaction History & Offline Settlement Ledger");
        title.setFont(ThemeConstants.FONT_HEADER);
        title.setForeground(ThemeConstants.TEXT_DARK);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton syncBtn = ThemeConstants.createStyledButton("🌐 Simulate Bank Sync (Go Online)", ThemeConstants.PRIMARY, Color.WHITE);
        syncBtn.setToolTipText("Simulate connecting to the bank server to settle all pending-sync transactions");
        syncBtn.addActionListener(e -> triggerBankSync());

        JButton clearBtn = new JButton("Clear History");
        clearBtn.setFont(ThemeConstants.FONT_SMALL);
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to clear all in-memory transaction records?", 
                    "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                transactionStore.clearAll();
            }
        });

        btnPanel.add(syncBtn);
        btnPanel.add(clearBtn);

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(btnPanel, BorderLayout.EAST);

        // Metrics Banner
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsPanel.setOpaque(false);

        metricsPanel.add(createMetricBox("Total Transactions", totalCountLabel = new JLabel("0"), ThemeConstants.PRIMARY_DARK));
        metricsPanel.add(createMetricBox("Pending Sync (Offline)", pendingCountLabel = new JLabel("0"), ThemeConstants.COLOR_PENDING));
        metricsPanel.add(createMetricBox("Synced & Settled", syncedCountLabel = new JLabel("0"), ThemeConstants.COLOR_SYNCED));
        metricsPanel.add(createMetricBox("Accepted Volume", volumeLabel = new JLabel("₹0.00"), ThemeConstants.TEXT_DARK));

        card.add(titleBar, BorderLayout.NORTH);
        card.add(metricsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricBox(String title, JLabel valueLabel, Color valueColor) {
        JPanel box = new JPanel(new BorderLayout(4, 4));
        box.setBackground(ThemeConstants.PRIMARY_LIGHT);
        box.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(ThemeConstants.FONT_SMALL);
        tLbl.setForeground(ThemeConstants.TEXT_MUTED);

        valueLabel.setFont(ThemeConstants.FONT_TITLE);
        valueLabel.setForeground(valueColor);

        box.add(tLbl, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setFont(ThemeConstants.FONT_BODY);
        transactionTable.setRowHeight(32);
        transactionTable.getTableHeader().setFont(ThemeConstants.FONT_BOLD);
        transactionTable.getTableHeader().setBackground(new Color(241, 245, 249));
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setAutoCreateRowSorter(true);

        // Custom Renderer for Status column (Column 0)
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String text = (value != null) ? value.toString() : "";

                if (text.contains("Pending Sync")) {
                    label.setForeground(ThemeConstants.COLOR_PENDING);
                    label.setText("⏳ " + text);
                } else if (text.contains("Synced")) {
                    label.setForeground(ThemeConstants.COLOR_SYNCED);
                    label.setText("✅ " + text);
                } else if (text.contains("Rejected")) {
                    label.setForeground(ThemeConstants.COLOR_REJECT);
                    label.setText("❌ " + text);
                }
                return label;
            }
        });

        // Set column preferred widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        transactionTable.getColumnModel().getColumn(7).setPreferredWidth(230);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER));

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(241, 245, 249));
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel info = new JLabel("<html><small style='color:#475569;'>" +
                "<b>Offline Store-and-Forward Cycle:</b> When offline payments are accepted by a merchant, " +
                "records are held in local ledger with status <b>'Pending Sync'</b>. " +
                "Clicking 'Simulate Bank Sync' simulates network reconnection where batch transactions are uploaded " +
                "and cleared by the central banking server." +
                "</small></html>");
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void triggerBankSync() {
        int pending = transactionStore.getPendingCount();
        if (pending == 0) {
            JOptionPane.showMessageDialog(this, 
                    "No offline transactions pending synchronization.\nAll accepted transactions are already settled!", 
                    "Bank Sync Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show a brief simulated synchronization progress dialog
        int synced = transactionStore.simulateBankSync();
        JOptionPane.showMessageDialog(this, 
                String.format("Bank Synchronization Successful!\n\n" +
                        "• Synced Batches: %d transaction(s)\n" +
                        "• Response Code: HTTP 200 OK (SETTLED)\n" +
                        "• Settlement Protocol: Interoperable Bank Clearinghouse Simulation\n\n" +
                        "All pending-sync records have been updated to 'Synced & Settled'.", synced),
                "Simulated Bank Sync Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<TransactionRecord> list = transactionStore.getAllRecords();

        for (TransactionRecord r : list) {
            tableModel.addRow(new Object[]{
                    r.getStatus().getDisplayName(),
                    r.getTxId(),
                    r.getTimestamp(),
                    r.getCustomerName(),
                    r.getMerchantName(),
                    String.format("%.2f", r.getAmount()),
                    r.getTokenId(),
                    r.getRemarks()
            });
        }

        // Update metrics
        totalCountLabel.setText(String.valueOf(list.size()));
        pendingCountLabel.setText(String.valueOf(transactionStore.getPendingCount()));
        syncedCountLabel.setText(String.valueOf(transactionStore.getSyncedCount()));
        volumeLabel.setText(String.format("₹%.2f", transactionStore.getTotalAcceptedVolume()));
    }

    @Override
    public void onStoreUpdated() {
        SwingUtilities.invokeLater(this::refreshTable);
    }
}
