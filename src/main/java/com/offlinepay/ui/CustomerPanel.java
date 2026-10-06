package com.offlinepay.ui;

import com.offlinepay.model.PaymentPayload;
import com.offlinepay.model.SpendingToken;
import com.offlinepay.service.QRCodeService;
import com.offlinepay.service.TokenService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Customer Screen:
 * 1. Shows bank-authorized spending token and offline balance.
 * 2. Allows entering merchant name and payment amount.
 * 3. Generates QR code containing the payment payload and simulated auth hash.
 * 4. Enables saving the QR code as a PNG image file.
 */
public class CustomerPanel extends JPanel {
    private final TokenService tokenService;
    private final QRCodeService qrCodeService;

    // UI Components
    private JComboBox<SpendingToken> tokenComboBox;
    private JLabel balanceLabel;
    private JLabel bankLabel;
    private JTextField merchantField;
    private JTextField amountField;
    private JLabel qrImageLabel;
    private JTextArea detailsArea;
    private JButton generateButton;
    private JButton saveButton;
    private JButton quickSaveButton;
    private JLabel statusLabel;

    private BufferedImage currentQrImage;
    private PaymentPayload currentPayload;

    public CustomerPanel(TokenService tokenService, QRCodeService qrCodeService) {
        this.tokenService = tokenService;
        this.qrCodeService = qrCodeService;

        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        initComponents();
        updateTokenDisplay();
    }

    private void initComponents() {
        // TOP: Token Information Card
        JPanel tokenCard = createTokenInfoCard();
        add(tokenCard, BorderLayout.NORTH);

        // CENTER: Split into Left (Payment Form) and Right (QR Preview & Actions)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        JPanel formCard = createFormCard();
        JPanel qrCard = createQrCard();

        contentPanel.add(formCard);
        contentPanel.add(qrCard);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTokenInfoCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Bank-Authorized Spending Token (Interoperable Allowance)");
        title.setFont(ThemeConstants.FONT_HEADER);
        title.setForeground(ThemeConstants.PRIMARY_DARK);

        JLabel badge = ThemeConstants.createBadge("OFFLINE SPENDING ACTIVE", ThemeConstants.COLOR_SYNCED, ThemeConstants.BG_SYNCED);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(badge, BorderLayout.EAST);

        JPanel detailsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        detailsRow.setOpaque(false);

        JLabel selectLabel = new JLabel("Select Token:");
        selectLabel.setFont(ThemeConstants.FONT_BOLD);

        tokenComboBox = new JComboBox<>(tokenService.getAvailableTokens().toArray(new SpendingToken[0]));
        tokenComboBox.setFont(ThemeConstants.FONT_BODY);
        tokenComboBox.addActionListener(e -> {
            SpendingToken selected = (SpendingToken) tokenComboBox.getSelectedItem();
            if (selected != null) {
                tokenService.setActiveToken(selected);
                updateTokenDisplay();
            }
        });

        balanceLabel = new JLabel("Balance: ₹0.00");
        balanceLabel.setFont(ThemeConstants.FONT_BOLD);
        balanceLabel.setForeground(ThemeConstants.COLOR_SYNCED);

        bankLabel = new JLabel("Bank: -");
        bankLabel.setFont(ThemeConstants.FONT_BODY);
        bankLabel.setForeground(ThemeConstants.TEXT_MUTED);

        detailsRow.add(selectLabel);
        detailsRow.add(tokenComboBox);
        detailsRow.add(balanceLabel);
        detailsRow.add(new JSeparator(SwingConstants.VERTICAL));
        detailsRow.add(bankLabel);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(detailsRow, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JLabel header = new JLabel("1. Initiate Offline Payment");
        header.setFont(ThemeConstants.FONT_HEADER);
        header.setForeground(ThemeConstants.TEXT_DARK);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Merchant Name Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel mLabel = new JLabel("Merchant Name:");
        mLabel.setFont(ThemeConstants.FONT_BOLD);
        form.add(mLabel, gbc);

        // Row 0: Merchant Name Input
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        merchantField = new JTextField("Apollo Pharmacy", 18);
        merchantField.setFont(ThemeConstants.FONT_BODY);
        form.add(merchantField, gbc);

        // Row 1: Quick suggestion buttons
        gbc.gridx = 1; gbc.gridy = 1;
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        quickPanel.setOpaque(false);
        String[] samples = {"Apollo Pharmacy", "Metro Supermarket", "Sharma Store"};
        for (String sample : samples) {
            JButton chip = new JButton(sample);
            chip.setFont(ThemeConstants.FONT_SMALL);
            chip.setMargin(new Insets(2, 6, 2, 6));
            chip.setFocusPainted(false);
            chip.addActionListener(e -> merchantField.setText(sample));
            quickPanel.add(chip);
        }
        form.add(quickPanel, gbc);

        // Row 2: Amount Label
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel aLabel = new JLabel("Payment Amount (₹):");
        aLabel.setFont(ThemeConstants.FONT_BOLD);
        form.add(aLabel, gbc);

        // Row 2: Amount Input
        gbc.gridx = 1; gbc.gridy = 2;
        amountField = new JTextField("250.00", 18);
        amountField.setFont(ThemeConstants.FONT_BODY);
        form.add(amountField, gbc);

        // Row 3: Quick Amount buttons
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel quickAmtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        quickAmtPanel.setOpaque(false);
        String[] amounts = {"₹50", "₹100", "₹250", "₹500", "₹1000"};
        for (String amt : amounts) {
            JButton chip = new JButton(amt);
            chip.setFont(ThemeConstants.FONT_SMALL);
            chip.setMargin(new Insets(2, 6, 2, 6));
            chip.setFocusPainted(false);
            chip.addActionListener(e -> amountField.setText(amt.replace("₹", "") + ".00"));
            quickAmtPanel.add(chip);
        }
        form.add(quickAmtPanel, gbc);

        // Row 4: Generate Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        generateButton = ThemeConstants.createStyledButton("Generate Payment QR Code", ThemeConstants.PRIMARY, Color.WHITE);
        generateButton.addActionListener(e -> generatePaymentQR());
        form.add(generateButton, gbc);

        // Row 5: Educational Note
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        JLabel note = new JLabel("<html><small style='color:#64748B;'>" +
                "<b>How it works:</b> Customer generates an offline QR code embedding their " +
                "bank-authorized token, merchant name, and simulated authorization digest. " +
                "No internet is required on either device during transaction handover." +
                "</small></html>");
        form.add(note, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQrCard() {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(ThemeConstants.BG_CARD);
        card.setBorder(ThemeConstants.createCardBorder());

        JLabel header = new JLabel("2. Generated Offline QR Code");
        header.setFont(ThemeConstants.FONT_HEADER);
        header.setForeground(ThemeConstants.TEXT_DARK);
        card.add(header, BorderLayout.NORTH);

        // QR Image Preview Center
        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(new Color(245, 247, 250));
        previewBox.setBorder(BorderFactory.createDashedBorder(ThemeConstants.BORDER, 3, 3));
        previewBox.setPreferredSize(new Dimension(280, 280));

        qrImageLabel = new JLabel("No QR Generated Yet", SwingConstants.CENTER);
        qrImageLabel.setFont(ThemeConstants.FONT_BODY);
        qrImageLabel.setForeground(ThemeConstants.TEXT_MUTED);
        previewBox.add(qrImageLabel, BorderLayout.CENTER);

        // Bottom: Details & Action Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));
        bottomPanel.setOpaque(false);

        detailsArea = new JTextArea(4, 25);
        detailsArea.setFont(ThemeConstants.FONT_MONO);
        detailsArea.setEditable(false);
        detailsArea.setBackground(new Color(248, 250, 252));
        detailsArea.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER));
        detailsArea.setText("Payment details will appear here after generation.");
        JScrollPane scroll = new JScrollPane(detailsArea);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnPanel.setOpaque(false);

        saveButton = ThemeConstants.createStyledButton("Save QR as PNG...", new Color(15, 23, 42), Color.WHITE);
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveQrCodeWithChooser());

        quickSaveButton = ThemeConstants.createStyledButton("Quick Save (qr_payment.png)", ThemeConstants.PRIMARY_DARK, Color.WHITE);
        quickSaveButton.setEnabled(false);
        quickSaveButton.addActionListener(e -> quickSaveQrCode());

        btnPanel.add(saveButton);
        btnPanel.add(quickSaveButton);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(ThemeConstants.FONT_SMALL);

        bottomPanel.add(scroll, BorderLayout.NORTH);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        card.add(previewBox, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private void generatePaymentQR() {
        SpendingToken token = tokenService.getActiveToken();
        if (token == null) {
            showError("No spending token selected.");
            return;
        }

        String merchant = merchantField.getText().trim();
        if (merchant.isEmpty()) {
            showError("Please enter a merchant name.");
            merchantField.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric amount (e.g. 250.00).");
            amountField.requestFocus();
            return;
        }

        if (amount <= 0) {
            showError("Payment amount must be greater than zero.");
            return;
        }

        if (!token.canSpend(amount)) {
            showError(String.format("Amount (₹%.2f) exceeds available offline token balance (₹%.2f).", 
                    amount, token.getRemainingBalance()));
            return;
        }

        try {
            // Create payment payload
            currentPayload = PaymentPayload.create(token, merchant, amount);
            String qrString = currentPayload.toQrString();

            // Render 260x260 QR Code
            currentQrImage = qrCodeService.generateQRCodeImage(qrString, 260, 260);

            // Update UI
            qrImageLabel.setIcon(new ImageIcon(currentQrImage));
            qrImageLabel.setText("");

            detailsArea.setText(String.format(
                    "TX ID: %s\n" +
                    "Merchant: %s\n" +
                    "Amount: ₹%.2f (%s)\n" +
                    "Token: %s (Holder: %s)\n" +
                    "Simulated Hash: %s",
                    currentPayload.getTxId(),
                    currentPayload.getMerchantName(),
                    currentPayload.getAmount(),
                    currentPayload.getCurrency(),
                    currentPayload.getTokenId(),
                    currentPayload.getCustomerName(),
                    currentPayload.getSimulatedAuthDigest()
            ));

            saveButton.setEnabled(true);
            quickSaveButton.setEnabled(true);
            statusLabel.setForeground(ThemeConstants.COLOR_SYNCED);
            statusLabel.setText("QR Code generated successfully! Ready to save as PNG.");

        } catch (Exception ex) {
            showError("Failed to generate QR code: " + ex.getMessage());
        }
    }

    private void saveQrCodeWithChooser() {
        if (currentQrImage == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payment QR Code Image");
        fileChooser.setSelectedFile(new File(System.getProperty("user.dir"), 
                "Payment_" + (currentPayload != null ? currentPayload.getTxId() : "QR") + ".png"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            try {
                qrCodeService.saveQRCodeToFile(currentQrImage, targetFile);
                statusLabel.setForeground(ThemeConstants.COLOR_SYNCED);
                statusLabel.setText("Saved: " + targetFile.getName());
                JOptionPane.showMessageDialog(this, 
                        "QR Code saved successfully to:\n" + targetFile.getAbsolutePath() +
                        "\n\nYou can now switch to the 'Merchant Screen' tab and scan this PNG file.", 
                        "QR Code Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Error saving image: " + ex.getMessage());
            }
        }
    }

    private void quickSaveQrCode() {
        if (currentQrImage == null) return;
        File defaultFile = new File(System.getProperty("user.dir"), "qr_payment.png");
        try {
            qrCodeService.saveQRCodeToFile(currentQrImage, defaultFile);
            statusLabel.setForeground(ThemeConstants.COLOR_SYNCED);
            statusLabel.setText("Quick-saved: qr_payment.png in workspace directory");
            JOptionPane.showMessageDialog(this, 
                    "QR Code quick-saved to project root:\n" + defaultFile.getAbsolutePath() +
                    "\n\nNow open the 'Merchant Screen' tab and select 'qr_payment.png' to scan!", 
                    "Quick Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Error saving image: " + ex.getMessage());
        }
    }

    public void updateTokenDisplay() {
        SpendingToken token = tokenService.getActiveToken();
        if (token != null) {
            balanceLabel.setText(String.format("Available: ₹%.2f / ₹%.2f", 
                    token.getRemainingBalance(), token.getMaxAllowance()));
            bankLabel.setText("Issuing Bank: " + token.getIssuingBank() + " (Token: " + token.getTokenId() + ")");
        }
    }

    private void showError(String message) {
        statusLabel.setForeground(ThemeConstants.COLOR_REJECT);
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Validation Notice", JOptionPane.WARNING_MESSAGE);
    }
}
