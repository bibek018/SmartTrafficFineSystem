package com.trafficfine.ui;

import com.trafficfine.dao.FineDAO;
import com.trafficfine.dao.VehicleDAO;
import com.trafficfine.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final FineDAO fineDAO = new FineDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public AdminDashboard() {
        setTitle("Admin Dashboard — Smart Traffic Fine System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 23, 42));

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(new Color(30, 41, 59));
        side.setPreferredSize(new Dimension(210, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("  🚦 STFS Admin");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logo.setForeground(new Color(226, 232, 240));
        logo.setBorder(new EmptyBorder(0, 16, 20, 0));
        side.add(logo);

        JLabel officer = new JLabel("  " + SessionManager.getCurrentUser().getFullName());
        officer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        officer.setForeground(new Color(100, 116, 139));
        officer.setBorder(new EmptyBorder(0, 16, 20, 0));
        side.add(officer);

        side.add(sideBtn("📷  Camera & Issue Fine", () -> openCamera()));
        side.add(sideBtn("📋  All Fines",           () -> openFines()));
        side.add(sideBtn("🚗  Vehicles",             () -> openVehicles()));
        side.add(sideBtn("👤  Add Owner",            () -> openAddUser()));
        side.add(Box.createVerticalGlue());
        side.add(sideBtn("🚪  Logout",               () -> logout()));

        return side;
    }

    // ── Main content ──────────────────────────────────────────────
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel pageTitle = new JLabel("Dashboard Overview");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(226, 232, 240));
        main.add(pageTitle, BorderLayout.NORTH);

        // Stats cards
        JPanel cards = new JPanel(new GridLayout(2, 2, 16, 16));
        cards.setBackground(new Color(15, 23, 42));
        cards.setBorder(new EmptyBorder(20, 0, 0, 0));

        int total   = fineDAO.countByStatus(null);
        int pending = fineDAO.countByStatus("PENDING");
        int paid    = fineDAO.countByStatus("PAID");
        int disputed= fineDAO.countByStatus("DISPUTED");
        double collected = fineDAO.totalCollected();
        int vehicles = vehicleDAO.getAllVehicles().size();

        cards.add(statCard("Total Fines Issued",   String.valueOf(total),    new Color(59, 130, 246), "📋"));
        cards.add(statCard("Pending Fines",        String.valueOf(pending),  new Color(245, 158, 11), "⏳"));
        cards.add(statCard("Fines Collected",      "₹" + String.format("%.0f", collected), new Color(16, 185, 129), "💰"));
        cards.add(statCard("Registered Vehicles",  String.valueOf(vehicles), new Color(139, 92, 246), "🚗"));

        main.add(cards, BorderLayout.CENTER);

        // Quick actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setBackground(new Color(15, 23, 42));
        actions.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton camBtn = actionButton("📷 Open Camera & Issue Fine", new Color(59, 130, 246));
        camBtn.addActionListener(e -> openCamera());
        JButton fineBtn = actionButton("📋 View All Fines", new Color(30, 41, 59));
        fineBtn.addActionListener(e -> openFines());

        actions.add(camBtn);
        actions.add(fineBtn);
        main.add(actions, BorderLayout.SOUTH);

        return main;
    }

    private JPanel statCard(String label, String value, Color accent, String emoji) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(accent);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLabel.setForeground(new Color(148, 163, 184));

        JPanel text = new JPanel();
        text.setBackground(new Color(30, 41, 59));
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(valLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(lblLabel);

        card.add(emojiLabel, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JButton sideBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(new Color(30, 41, 59));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(51, 65, 85));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(new Color(148, 163, 184));
            }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    // ── Navigation ─────────────────────────────────────────────────
    private void openCamera()   { new CameraScreen(this).setVisible(true); setVisible(false); }
    private void openFines()    { new AllFinesScreen(this).setVisible(true); setVisible(false); }
    private void openVehicles() { new VehicleManagementScreen(this).setVisible(true); setVisible(false); }
    private void openAddUser()  { new AddUserScreen(this).setVisible(true); }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginScreen().setVisible(true);
    }
}
