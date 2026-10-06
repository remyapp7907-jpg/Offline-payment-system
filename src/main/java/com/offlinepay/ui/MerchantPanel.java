package com.offlinepay.ui;

import com.offlinepay.model.PaymentPayload;
import com.offlinepay.model.SpendingToken;
import com.offlinepay.model.TransactionRecord;
import com.offlinepay.model.TransactionStatus;
import com.offlinepay.service.QRCodeService;
import com.offlinepay.service.TokenService;
import com.offlinepay.service.TransactionStore;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Merchant Screen:
 * 1. Merchant selects and scans a QR image file from the computer.
 * 2. System decodes the QR using ZXing and displays payment info (Merchant Name, Amount, Token ID, etc.).
 * 3. Merchant verifies details and chooses to Accept or Reject the payment.
 * 4. Accepted payments are recorded with 'PENDING_SYNC' status in the local ledger.
 */
public class MerchantPanel extends JPanel {
    private final QRCodeService qrCodeService;
    private final TokenService tokenService;
    private final TransactionStore transactionStore;
    private final Runnable onBalanceChangedCallback;

    // Merchant Configuration
    private JTextField currentMerchantField;

    // Scan & Preview Components
    private JLabel previewImageLabel;
    private JLabel scanStatusLabel;

    // Display Information Fields
    private JLabel displayAmountLabel;
    private JLabel displayMerchantLabel;
    private JLabel displayCustomerLabel;
    private JLabel displayTokenLabel;
    private JLabel displayBankLabel;
    private JLabel displayTxIdLabel;
    private JLabel displayTimeLabel;
    private JLabel displayHashLabel;
    private JLabel matchWarningLabel;

    // Action Buttons
    private JButton acceptButton;
    private JButton rejectButton;

    // Active Scanned Payload
    private PaymentPayload activePayload;
    private File activeFile;

    public MerchantPanel(QRCodeService qrCodeService, TokenService tokenService, 
                         TransactionStore transactionStore, Runnable onBalanceChangedCallback) {
        this.qrCodeService = qrCodeService;
        this.tokenService = tokenService;
        this.transactionStore = transactionStore;
        this.onBalanceChangedCallback = onBalanceChangedCallback;

        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        initComponents();
        resetDisplayFields();
    }

    private void initComponents() {
        // TOP: Merchant Terminal Identity Header
        JPanel terminalCard = createTerminalCard();
        add(terminalCard, BorderLayout.NORTH);

        // CENTER: 2 Columns (Left: Scanner & Preview, Right: Scanned Details & Decision)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        JPanel scanCard = createScanCard();
        JPanel detailsCard = createDetailsCard();

        contentPanel.add(scanCard);
        contentPanel.add(detailsCard);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTerminalCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        left.setOpaque(false);

        JLabel title = new JLabel("Merchant Terminal:");
        title.setFont(ThemeConstants.FONT_HEADER);
        title.setForeground(ThemeConstants.TEXT_DARK);

        currentMerchantField = new JTextField("Apollo Pharmacy", 16);
        currentMerchantField.setFont(ThemeConstants.FONT_BODY);
        currentMerchantField.setToolTipText("Name of this merchant store (used to verify incoming payments)");

        JButton setMerchantBtn = new JButton("Set Terminal Name");
        setMerchantBtn.setFont(ThemeConstants.FONT_SMALL);
        setMerchantBtn.addActionListener(e -> {
            String name = currentMerchantField.getText().trim();
            if (name.isEmpty()) {
                currentMerchantField.setText("Apollo Pharmacy");
            }
            checkMerchantMatch();
            JOptionPane.showMessageDialog(this, "Terminal Merchant Name set to: " + currentMerchantField.getText(), 
                    "Terminal Configured", JOptionPane.INFORMATION_MESSAGE);
        });

        left.add(title);
        left.add(currentMerchantField);
        left.add(setMerchantBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);
        JLabel offlineBadge = ThemeConstants.createBadge("OFFLINE VERIFICATION READY", ThemeConstants.PRIMARY_DARK, ThemeConstants.PRIMARY_LIGHT);
        right.add(offlineBadge);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel createScanCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JLabel header = new JLabel("1. Select & Scan Customer QR Image");
        header.setFont(ThemeConstants.FONT_HEADER);
        header.setForeground(ThemeConstants.TEXT_DARK);
        card.add(header, BorderLayout.NORTH);

        // Preview box
        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(new Color(245, 247, 250));
        previewBox.setBorder(BorderFactory.createDashedBorder(ThemeConstants.BORDER, 3, 3));
        previewBox.setPreferredSize(new Dimension(260, 260));

        previewImageLabel = new JLabel("No QR Image Loaded", SwingConstants.CENTER);
        previewImageLabel.setFont(ThemeConstants.FONT_BODY);
        previewImageLabel.setForeground(ThemeConstants.TEXT_MUTED);
        previewBox.add(previewImageLabel, BorderLayout.CENTER);

        // Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 6, 8));
        actionPanel.setOpaque(false);

        JButton browseButton = ThemeConstants.createStyledButton("📁 Browse & Scan QR Image (File Chooser)", ThemeConstants.PRIMARY, Color.WHITE);
        browseButton.addActionListener(e -> browseAndScanImage());

        JButton quickScanButton = ThemeConstants.createStyledButton("⚡ Quick Scan (qr_payment.png in workspace)", new Color(71, 85, 105), Color.WHITE);
        quickScanButton.addActionListener(e -> quickScanWorkspaceImage());

        scanStatusLabel = new JLabel("Select an image file generated by the customer.", SwingConstants.CENTER);
        scanStatusLabel.setFont(ThemeConstants.FONT_SMALL);
        scanStatusLabel.setForeground(ThemeConstants.TEXT_MUTED);

        actionPanel.add(browseButton);
        actionPanel.add(quickScanButton);

        JPanel bottom = new JPanel(new BorderLayout(4, 6));
        bottom.setOpaque(false);
        bottom.add(actionPanel, BorderLayout.CENTER);
        bottom.add(scanStatusLabel, BorderLayout.SOUTH);

        card.add(previewBox, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JLabel header = new JLabel("2. Scanned Payment Details & Decision");
        header.setFont(ThemeConstants.FONT_HEADER);
        header.setForeground(ThemeConstants.TEXT_DARK);
        card.add(header, BorderLayout.NORTH);

        // Center Details Grid
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Big Amount Banner
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JPanel amountBanner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        amountBanner.setBackground(ThemeConstants.PRIMARY_LIGHT);
        amountBanner.setBorder(new EmptyBorder(8, 8, 8, 8));

        displayAmountLabel = new JLabel("₹ 0.00");
        displayAmountLabel.setFont(ThemeConstants.FONT_AMOUNT);
        displayAmountLabel.setForeground(ThemeConstants.PRIMARY_DARK);
        amountBanner.add(displayAmountLabel);
        detailsPanel.add(amountBanner, gbc);

        // Merchant Match Alert
        gbc.gridy = 1;
        matchWarningLabel = new JLabel(" ", SwingConstants.CENTER);
        matchWarningLabel.setFont(ThemeConstants.FONT_BOLD);
        detailsPanel.add(matchWarningLabel, gbc);

        // Fields
        gbc.gridwidth = 1;
        int row = 2;
        displayMerchantLabel = addDetailRow(detailsPanel, gbc, row++, "Merchant Name:", "-");
        displayCustomerLabel = addDetailRow(detailsPanel, gbc, row++, "Customer / Holder:", "-");
        displayTokenLabel = addDetailRow(detailsPanel, gbc, row++, "Spending Token ID:", "-");
        displayBankLabel = addDetailRow(detailsPanel, gbc, row++, "Issuing Bank:", "-");
        displayTxIdLabel = addDetailRow(detailsPanel, gbc, row++, "Transaction ID:", "-");
        displayTimeLabel = addDetailRow(detailsPanel, gbc, row++, "Timestamp:", "-");
        displayHashLabel = addDetailRow(detailsPanel, gbc, row++, "Simulated Auth Hash:", "-");

        card.add(detailsPanel, BorderLayout.CENTER);

        // Decision Buttons at bottom
        JPanel decisionPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        decisionPanel.setOpaque(false);
        decisionPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        acceptButton = ThemeConstants.createStyledButton("✓ Accept Payment (Store Offline)", ThemeConstants.COLOR_SYNCED, Color.WHITE);
        acceptButton.setEnabled(false);
        acceptButton.addActionListener(e -> acceptPayment());

        rejectButton = ThemeConstants.createStyledButton("✗ Reject Payment", ThemeConstants.COLOR_REJECT, Color.WHITE);
        rejectButton.setEnabled(false);
        rejectButton.addActionListener(e -> rejectPayment());

        decisionPanel.add(acceptButton);
        decisionPanel.add(rejectButton);

        card.add(decisionPanel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel addDetailRow(JPanel parent, GridBagConstraints gbc, int row, String labelText, String defaultValue) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeConstants.FONT_BOLD);
        parent.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel(defaultValue);
        val.setFont(ThemeConstants.FONT_BODY);
        val.setForeground(ThemeConstants.TEXT_DARK);
        parent.add(val, gbc);

        return val;
    }

    private void browseAndScanImage() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setDialogTitle("Select QR Code Image (PNG / JPG)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            processImageFile(fileChooser.getSelectedFile());
        }
    }

    private void quickScanWorkspaceImage() {
        File file = new File(System.getProperty("user.dir"), "qr_payment.png");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, 
                    "File 'qr_payment.png' was not found in the workspace root.\n" +
                    "Please go to the Customer Screen and click 'Quick Save (qr_payment.png)' first,\n" +
                    "or click 'Browse & Scan' to select any saved PNG image file.",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        processImageFile(file);
    }

    private void processImageFile(File file) {
        try {
            // Load and display thumbnail
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                showScanError("File is not a valid image format.");
                return;
            }

            // Scale thumbnail nicely for preview box
            Image scaled = img.getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            previewImageLabel.setIcon(new ImageIcon(scaled));
            previewImageLabel.setText("");

            // Decode QR payload using ZXing service
            String rawDecoded = qrCodeService.decodeQRCodeFromFile(file);

            // Parse payment payload
            activePayload = PaymentPayload.parse(rawDecoded);
            activeFile = file;

            // Populate scanned details in UI
            populateScannedDetails(activePayload);

            scanStatusLabel.setForeground(ThemeConstants.COLOR_SYNCED);
            scanStatusLabel.setText("QR Decoded Successfully from: " + file.getName());

            // Enable decision buttons
            acceptButton.setEnabled(true);
            rejectButton.setEnabled(true);

        } catch (com.google.zxing.NotFoundException nfe) {
            showScanError("No QR Code detected in image: " + file.getName());
            resetDisplayFields();
        } catch (IllegalArgumentException iae) {
            showScanError("QR payload format error: " + iae.getMessage());
            resetDisplayFields();
        } catch (Exception ex) {
            showScanError("Scan error: " + ex.getMessage());
            resetDisplayFields();
        }
    }

    private void populateScannedDetails(PaymentPayload p) {
        displayAmountLabel.setText(String.format("₹ %.2f", p.getAmount()));
        displayMerchantLabel.setText(p.getMerchantName());
        displayCustomerLabel.setText(p.getCustomerName() + " (" + p.getCustomerId() + ")");
        displayTokenLabel.setText(p.getTokenId());

        // Find token to show bank
        SpendingToken token = tokenService.findTokenById(p.getTokenId());
        if (token != null) {
            displayBankLabel.setText(token.getIssuingBank());
        } else {
            displayBankLabel.setText("Interoperable Partner Bank (External Token)");
        }

        displayTxIdLabel.setText(p.getTxId());
        displayTimeLabel.setText(p.getTimestamp());
        displayHashLabel.setText(p.getSimulatedAuthDigest() + " [Valid Simulation]");

        checkMerchantMatch();
    }

    private void checkMerchantMatch() {
        if (activePayload == null) {
            matchWarningLabel.setText(" ");
            return;
        }

        String terminalName = currentMerchantField.getText().trim();
        if (terminalName.equalsIgnoreCase(activePayload.getMerchantName())) {
            matchWarningLabel.setText("✓ Merchant Name matches this terminal");
            matchWarningLabel.setForeground(ThemeConstants.COLOR_SYNCED);
        } else {
            matchWarningLabel.setText("⚠ Note: Addressed to '" + activePayload.getMerchantName() + "' (Terminal is '" + terminalName + "')");
            matchWarningLabel.setForeground(ThemeConstants.COLOR_PENDING);
        }
    }

    private void acceptPayment() {
        if (activePayload == null) return;

        // Check if token exists in local simulation to deduct
        SpendingToken token = tokenService.findTokenById(activePayload.getTokenId());
        if (token != null) {
            if (!token.canSpend(activePayload.getAmount())) {
                JOptionPane.showMessageDialog(this, 
                        "Cannot accept payment: Insufficient offline token balance!\n" +
                        "Available: ₹" + token.getRemainingBalance() + " | Required: ₹" + activePayload.getAmount(),
                        "Token Limit Exceeded", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Deduct balance
            token.deduct(activePayload.getAmount());
            if (onBalanceChangedCallback != null) {
                onBalanceChangedCallback.run();
            }
        }

        // Create transaction record with PENDING_SYNC status
        TransactionRecord record = TransactionRecord.fromPayload(
                activePayload, 
                TransactionStatus.PENDING_SYNC, 
                "Offline approved by " + currentMerchantField.getText().trim() + "; pending batch sync."
        );

        transactionStore.addRecord(record);

        // Show confirmation receipt
        JOptionPane.showMessageDialog(this, 
                String.format("Payment of ₹%.2f from %s ACCEPTED OFFLINE!\n\n" +
                        "Transaction ID: %s\n" +
                        "Status: PENDING_SYNC (Stored locally in merchant ledger)\n" +
                        "Token: %s\n\n" +
                        "This transaction is now recorded in 'Transaction History' and will be settled\n" +
                        "when the terminal connects to the bank network.",
                        activePayload.getAmount(), activePayload.getCustomerName(),
                        activePayload.getTxId(), activePayload.getTokenId()),
                "Payment Accepted (Offline)", JOptionPane.INFORMATION_MESSAGE);

        // Disable buttons to prevent duplicate acceptance
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
        scanStatusLabel.setForeground(ThemeConstants.COLOR_SYNCED);
        scanStatusLabel.setText("✓ Transaction " + activePayload.getTxId() + " accepted and queued as PENDING_SYNC");
    }

    private void rejectPayment() {
        if (activePayload == null) return;

        String reason = JOptionPane.showInputDialog(this, 
                "Enter reason for rejection (optional):", 
                "Merchant Declined by " + currentMerchantField.getText().trim());

        if (reason == null) return; // User cancelled dialog
        if (reason.trim().isEmpty()) {
            reason = "Declined by merchant terminal";
        }

        // Create transaction record with REJECTED status
        TransactionRecord record = TransactionRecord.fromPayload(
                activePayload, 
                TransactionStatus.REJECTED, 
                reason
        );

        transactionStore.addRecord(record);

        JOptionPane.showMessageDialog(this, 
                String.format("Payment of ₹%.2f was REJECTED.\nReason: %s", 
                        activePayload.getAmount(), reason),
                "Payment Rejected", JOptionPane.WARNING_MESSAGE);

        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
        scanStatusLabel.setForeground(ThemeConstants.COLOR_REJECT);
        scanStatusLabel.setText("✗ Transaction " + activePayload.getTxId() + " was rejected.");
    }

    private void resetDisplayFields() {
        activePayload = null;
        activeFile = null;
        displayAmountLabel.setText("₹ 0.00");
        displayMerchantLabel.setText("-");
        displayCustomerLabel.setText("-");
        displayTokenLabel.setText("-");
        displayBankLabel.setText("-");
        displayTxIdLabel.setText("-");
        displayTimeLabel.setText("-");
        displayHashLabel.setText("-");
        matchWarningLabel.setText(" ");
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }

    private void showScanError(String message) {
        scanStatusLabel.setForeground(ThemeConstants.COLOR_REJECT);
        scanStatusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Scan Error", JOptionPane.ERROR_MESSAGE);
    }
}
