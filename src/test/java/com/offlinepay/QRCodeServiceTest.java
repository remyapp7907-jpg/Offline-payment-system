package com.offlinepay;

import com.offlinepay.model.PaymentPayload;
import com.offlinepay.model.SpendingToken;
import com.offlinepay.model.TransactionRecord;
import com.offlinepay.model.TransactionStatus;
import com.offlinepay.service.QRCodeService;
import com.offlinepay.service.TokenService;
import com.offlinepay.service.TransactionStore;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating the core offline payment workflow:
 * 1. Bank-Authorized Spending Token allowance validation and deduction.
 * 2. Payment payload serialization into standardized QR protocol.
 * 3. ZXing QR code image generation and PNG saving.
 * 4. ZXing QR decoding and payload reconstruction.
 * 5. Offline transaction ledger recording with PENDING_SYNC status.
 * 6. Store-and-forward bank synchronization cycle.
 */
public class QRCodeServiceTest {

    @Test
    public void testFullOfflinePaymentLifecycle() throws Exception {
        // 1. Setup simulated Bank-Authorized Spending Token
        SpendingToken token = new SpendingToken(
                "TOK-TEST-9901",
                "CUST-TEST-01",
                "Aditya Test",
                "State Bank of India",
                5000.00,
                3000.00,
                LocalDate.now().plusMonths(3),
                "SIMULATED-SIG-OK"
        );

        assertTrue(token.canSpend(250.00), "Should allow spending within balance");
        assertFalse(token.canSpend(3500.00), "Should reject spending beyond balance");

        // 2. Generate Payment Payload
        String merchantName = "Apollo Pharmacy";
        double amount = 250.00;
        PaymentPayload payload = PaymentPayload.create(token, merchantName, amount);

        assertNotNull(payload.getTxId());
        assertEquals("Apollo Pharmacy", payload.getMerchantName());
        assertEquals(250.00, payload.getAmount());
        assertEquals("TOK-TEST-9901", payload.getTokenId());

        // 3. Serialize and generate QR Image using ZXing
        QRCodeService qrService = new QRCodeService();
        String qrContent = payload.toQrString();
        assertTrue(qrContent.startsWith("OFFLINE_PAY_V1"));

        BufferedImage qrImage = qrService.generateQRCodeImage(qrContent, 260, 260);
        assertNotNull(qrImage);
        assertEquals(260, qrImage.getWidth());
        assertEquals(260, qrImage.getHeight());

        // 4. Save QR Image to PNG file
        File tempPng = File.createTempFile("test_payment_qr_", ".png");
        tempPng.deleteOnExit();
        qrService.saveQRCodeToFile(qrImage, tempPng);
        assertTrue(tempPng.exists() && tempPng.length() > 0, "PNG file should exist and not be empty");

        // 5. Merchant scans and decodes QR Image from file using ZXing
        String decodedText = qrService.decodeQRCodeFromFile(tempPng);
        assertEquals(qrContent, decodedText, "Decoded QR string must match original payload");

        PaymentPayload decodedPayload = PaymentPayload.parse(decodedText);
        assertEquals(payload.getTxId(), decodedPayload.getTxId());
        assertEquals(payload.getMerchantName(), decodedPayload.getMerchantName());
        assertEquals(payload.getAmount(), decodedPayload.getAmount());
        assertEquals(payload.getTokenId(), decodedPayload.getTokenId());

        // 6. Merchant accepts payment offline
        assertTrue(token.deduct(amount), "Balance should be deducted");
        assertEquals(2750.00, token.getRemainingBalance(), 0.001);

        TransactionStore store = new TransactionStore();
        TransactionRecord record = TransactionRecord.fromPayload(
                decodedPayload, 
                TransactionStatus.PENDING_SYNC, 
                "Offline approved; pending bank sync"
        );
        store.addRecord(record);

        assertEquals(1, store.getAllRecords().size());
        assertEquals(TransactionStatus.PENDING_SYNC, store.getAllRecords().get(0).getStatus());
        assertEquals(1, store.getPendingCount());

        // 7. Simulate Bank Sync (Go Online)
        int syncedCount = store.simulateBankSync();
        assertEquals(1, syncedCount);
        assertEquals(0, store.getPendingCount());
        assertEquals(1, store.getSyncedCount());
        assertEquals(TransactionStatus.SYNCED, store.getAllRecords().get(0).getStatus());
    }
}
