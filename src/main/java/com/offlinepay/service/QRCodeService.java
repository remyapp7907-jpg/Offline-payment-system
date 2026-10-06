package com.offlinepay.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service for generating, saving, and decoding QR codes using ZXing.
 */
public class QRCodeService {

    /**
     * Generates a QR Code BufferedImage from a given text payload.
     *
     * @param text   the payload string to encode
     * @param width  desired image width in pixels
     * @param height desired image height in pixels
     * @return BufferedImage containing the rendered QR code
     * @throws Exception if generation fails
     */
    public BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Saves a BufferedImage to a PNG file on disk.
     *
     * @param image      BufferedImage to save
     * @param targetFile Destination file (e.g. .png)
     * @throws IOException if writing fails
     */
    public void saveQRCodeToFile(BufferedImage image, File targetFile) throws IOException {
        if (!targetFile.getName().toLowerCase().endsWith(".png")) {
            targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".png");
        }
        ImageIO.write(image, "PNG", targetFile);
    }

    /**
     * Decodes a QR code directly from an image file.
     *
     * @param qrFile The image file (PNG/JPG) to read
     * @return Decoded text from the QR code
     * @throws IOException if reading the file fails
     * @throws NotFoundException if no valid QR code is found
     */
    public String decodeQRCodeFromFile(File qrFile) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(qrFile);
        if (bufferedImage == null) {
            throw new IOException("Could not read image file. Please ensure it is a valid image format (PNG, JPG).");
        }
        return decodeQRCodeFromImage(bufferedImage);
    }

    /**
     * Decodes a QR code from an in-memory BufferedImage.
     *
     * @param bufferedImage the image to decode
     * @return Decoded text payload
     * @throws Exception if decoding fails or no QR pattern is recognized
     */
    public String decodeQRCodeFromImage(BufferedImage bufferedImage) throws Exception {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }
}
