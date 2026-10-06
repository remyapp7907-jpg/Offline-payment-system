package com.offlinepay.ui;

import com.offlinepay.service.QRCodeService;
import com.offlinepay.service.TokenService;
import com.offlinepay.service.TransactionStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window for OfflinePaymentSystem.
 * Provides tabbed navigation between Customer Screen, Merchant Screen,
 * Transaction Ledger, and Project Architecture & Limitations.
 */
public class MainFrame extends JFrame {
    private final TokenService tokenService;
    private final QRCodeService qrCodeService;
    private final TransactionStore transactionStore;

    private CustomerPanel customerPanel;
    private MerchantPanel merchantPanel;
    private HistoryPanel historyPanel;

    public MainFrame() {
        this.tokenService = new TokenService();
        this.qrCodeService = new QRCodeService();
        this.transactionStore = new TransactionStore();

        initFrame();
    }

    private void initFrame() {
        setTitle("OfflinePaymentSystem - Interoperable Offline Payment Simulation (S3 BTech Project)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        // Main Layout Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(ThemeConstants.BG_MAIN);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeConstants.FONT_BOLD);
        tabbedPane.setBackground(ThemeConstants.BG_MAIN);

        // Instantiate Panels
        customerPanel = new CustomerPanel(tokenService, qrCodeService);
        merchantPanel = new MerchantPanel(qrCodeService, tokenService, transactionStore, () -> {
            customerPanel.updateTokenDisplay();
        });
        historyPanel = new HistoryPanel(transactionStore);
        JPanel architecturePanel = createArchitecturePanel();

        // Add Tabs
        tabbedPane.addTab("👤 Customer Screen (Pay)", customerPanel);
        tabbedPane.addTab("🏪 Merchant Screen (Scan & Verify)", merchantPanel);
        tabbedPane.addTab("📋 Transaction History (Pending Sync)", historyPanel);
        tabbedPane.addTab("📖 Architecture & Viva Notes", architecturePanel);

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainContainer);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Left Branding
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel appTitle = new JLabel("OfflinePaymentSystem");
        appTitle.setFont(ThemeConstants.FONT_TITLE);
        appTitle.setForeground(ThemeConstants.PRIMARY_DARK);

        JLabel appSub = new JLabel("Educational Proof-of-Concept: Interoperable Offline Payments using Bank-Authorized Spending Tokens");
        appSub.setFont(ThemeConstants.FONT_SMALL);
        appSub.setForeground(ThemeConstants.TEXT_MUTED);

        left.add(appTitle);
        left.add(appSub);

        // Right Badges and Info Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        right.setOpaque(false);

        JLabel simBadge = ThemeConstants.createBadge("MODE: OFFLINE SIMULATION", ThemeConstants.COLOR_PENDING, ThemeConstants.BG_PENDING);

        JButton infoBtn = new JButton("ℹ Limitations & Info");
        infoBtn.setFont(ThemeConstants.FONT_SMALL);
        infoBtn.setFocusPainted(false);
        infoBtn.addActionListener(e -> showLimitationsDialog());

        right.add(simBadge);
        right.add(infoBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createArchitecturePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeConstants.BG_CARD);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JEditorPane notesPane = new JEditorPane();
        notesPane.setContentType("text/html");
        notesPane.setEditable(false);
        notesPane.setBackground(ThemeConstants.BG_CARD);

        notesPane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #1E293B;'>" +
                "<h2 style='color:#1D4ED8; margin-bottom:4px;'>Project Overview: Interoperable Offline Payments</h2>" +
                "<p style='color:#64748B;'><b>Course:</b> S3 BTech Project &bull; <b>Design Pattern:</b> Store-and-Forward Token Model</p>" +
                "<hr style='border: 0; border-top: 1px solid #E2E8F0;'/>" +

                "<h3 style='color:#0F172A;'>1. Key Concepts Demonstrated:</h3>" +
                "<ul>" +
                "<li><b>Bank-Authorized Spending Token:</b> A pre-allocated digital allowance issued by an authorized bank (e.g. SBI, HDFC) when the user is online. Allows bounded offline spending without continuous gateway connectivity.</li>" +
                "<li><b>Interoperability:</b> Standardized payload protocol (<code>OFFLINE_PAY_V1</code>) allows any merchant terminal to decode and verify tokens issued by any participating bank.</li>" +
                "<li><b>Dynamic QR Code Handover:</b> The payment details, token reference, and simulated authorization signature are serialized and presented via a dynamic QR code.</li>" +
                "<li><b>Store-and-Forward Cycle:</b> Scanned transactions are accepted offline and stored locally with <b>PENDING_SYNC</b> status. Once connectivity returns, the merchant batches and transmits them to the bank clearinghouse for final settlement.</li>" +
                "</ul>" +

                "<h3 style='color:#DC2626;'>2. Important College Project Limitations:</h3>" +
                "<ul>" +
                "<li><b>Simulation Only:</b> This application is an educational prototype and does not integrate with production banking networks (e.g., NPCI, UPI, Visa, Mastercard).</li>" +
                "<li><b>Cryptographic Security Limitation:</b> This simulation does NOT claim cryptographic security against adversarial attacks. In real production CBDC/offline wallets, hardware security modules (HSMs), Trusted Execution Environments (TEE/eSE), and certified public key infrastructure (PKI ECDSA) are mandatory.</li>" +
                "<li><b>In-Memory Storage:</b> Transaction records and token states are held in memory for this first prototype version.</li>" +
                "</ul>" +

                "<h3 style='color:#0F172A;'>3. Demonstration Guide for Evaluators:</h3>" +
                "<ol>" +
                "<li><b>Step 1 (Customer Screen):</b> Select an authorized bank token, enter merchant name (e.g., <i>Apollo Pharmacy</i>), enter amount (e.g., <i>₹250</i>), and click <b>Generate Payment QR Code</b>.</li>" +
                "<li><b>Step 2 (Save QR Image):</b> Click <b>Save QR as PNG...</b> (or <b>Quick Save</b> to save directly as <code>qr_payment.png</code>).</li>" +
                "<li><b>Step 3 (Merchant Screen):</b> Switch to Merchant Screen and click <b>Browse & Scan QR Image</b> (or <b>Quick Scan</b>). The system uses ZXing to decode the QR and display the verified payment parameters.</li>" +
                "<li><b>Step 4 (Accept/Reject):</b> Click <b>Accept Payment</b>. The transaction is recorded in the ledger with <b>Pending Sync</b> status.</li>" +
                "<li><b>Step 5 (Transaction History):</b> Switch to Transaction History to view the offline ledger, then click <b>Simulate Bank Sync</b> to demonstrate online batch clearing.</li>" +
                "</ol>" +
                "</body></html>");

        notesPane.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(notesPane);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void showLimitationsDialog() {
        JOptionPane.showMessageDialog(this,
                "PROJECT LIMITATIONS & ARCHITECTURAL SCOPE:\n\n" +
                "1. Educational Simulation:\n" +
                "   This system is an S3 BTech educational simulation of offline payment concepts.\n" +
                "   It is not connected to real banking systems, NPCI, or UPI rails.\n\n" +
                "2. Cryptographic Security Note:\n" +
                "   The QR payload uses a simulated hash for concept demonstration.\n" +
                "   It does NOT claim production cryptographic security or anti-tamper guarantees.\n\n" +
                "3. Storage Scope:\n" +
                "   Transactions are stored in memory for this initial prototype.\n\n" +
                "4. Purpose:\n" +
                "   Demonstrates interoperability, bank-authorized token allowances, and the\n" +
                "   offline store-and-forward settlement lifecycle.",
                "System Scope & Educational Limitations",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
