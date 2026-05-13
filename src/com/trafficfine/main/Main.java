package com.trafficfine.main;

import com.trafficfine.ui.LoginScreen;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // ── MUST be first — load Tesseract native DLL before anything else ──
        // This tells JNA exactly where tesseract-5.dll lives on disk
        // Change this path if Tesseract installed somewhere else on your PC
        System.setProperty("jna.library.path", "C:\\Program Files\\Tesseract-OCR");

        // Also add Tesseract to java.library.path as a fallback
        System.setProperty("java.library.path",
                System.getProperty("java.library.path") +
                        ";C:\\Program Files\\Tesseract-OCR");

        // ── Apply FlatLaf dark theme ─────────────────────────────────────────
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // ── Launch app ───────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            LoginScreen login = new LoginScreen();
            login.setVisible(true);
        });
    }
}