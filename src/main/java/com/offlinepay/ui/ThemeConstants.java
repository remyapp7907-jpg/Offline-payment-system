package com.offlinepay.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * UI styling constants and helper components for a polished, modern Swing appearance.
 */
public class ThemeConstants {
    // Color Palette
    public static final Color PRIMARY = new Color(37, 99, 235);         // Indigo / Blue 600
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);    // Blue 700
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255); // Blue 50
    public static final Color BG_MAIN = new Color(248, 250, 252);       // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);        // Slate 200
    public static final Color TEXT_DARK = new Color(15, 23, 42);        // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    
    // Status Colors
    public static final Color COLOR_PENDING = new Color(217, 119, 6);   // Amber 600
    public static final Color BG_PENDING = new Color(254, 243, 199);    // Amber 100
    public static final Color COLOR_SYNCED = new Color(16, 149, 93);    // Emerald 600
    public static final Color BG_SYNCED = new Color(209, 250, 229);     // Emerald 100
    public static final Color COLOR_REJECT = new Color(220, 38, 38);    // Red 600
    public static final Color BG_REJECT = new Color(254, 226, 226);     // Red 100

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_AMOUNT = new Font("Segoe UI", Font.BOLD, 22);

    /**
     * Creates a styled card border with internal padding.
     */
    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    /**
     * Creates a styled button.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled status badge label.
     */
    public static JLabel createBadge(String text, Color fgColor, Color bgColor) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(fgColor);
        label.setBackground(bgColor);
        label.setOpaque(true);
        label.setBorder(new CompoundBorder(
                new LineBorder(fgColor, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return label;
    }
}
