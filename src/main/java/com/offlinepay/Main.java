package com.offlinepay;

import com.formdev.flatlaf.FlatLightLaf;
import com.offlinepay.ui.MainFrame;

import javax.swing.*;

/**
 * Main application entry point for OfflinePaymentSystem.
 * Initializes the modern Swing Look and Feel and displays the MainFrame.
 */
public class Main {
    public static void main(String[] args) {
        // Configure Look and Feel (FlatLaf modern clean UI, fallback to system LaF)
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default Java Look and Feel will be used as final fallback
            }
        }

        // Enable Anti-Aliasing for crisp fonts and graphics in Swing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
