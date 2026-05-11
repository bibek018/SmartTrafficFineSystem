package com.trafficfine.ui;

import com.trafficfine.dao.FineDAO;
import com.trafficfine.dao.VehicleDAO;
import com.trafficfine.model.Fine;
import com.trafficfine.model.Vehicle;
import com.trafficfine.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class OwnerDashboard extends JFrame {

    private final FineDAO fineDAO = new FineDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private JTable finesTable;
    private DefaultTableModel tableModel;

    public OwnerDashboard() {
        setTitle("My Traffic Fines — " + SessionManager.getCurrentUser().getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 580);
        setLocationRelativeTo(null);
        initUI();
        loadMyFines();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(15, 23, 42));

        // ── Header ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel headerLeft = new JPanel();
        headerLeft.setBackground(new Color(30, 41, 59));
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        JLabel greeting = new JLabel("Welcome, " + SessionManager.getCurrentUser().getFullName());
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 18));
        greeting.setForeground(new Color(226, 232, 240));
        JLabel sub = new JLabel("View and pay your traffic fines below");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        headerLeft.add(greeting);
        headerLeft.add(sub);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(51, 65, 85));
        btnLogout.setForeground(new Color(226, 232, 240));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(8, 16, 8, 16));

        header.add(headerLeft, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        // ── Stats row ─────────────────────────────────────────────
        int ownerId = SessionManager.getCurrentUser().getId();
        List<Fine> myFines = fineDAO.getFinesByOwner(ownerId);
        long pending = myFines.stream().filter(f -> "PENDING".equals(f.getStatus())).count();
        double totalDue = myFines.stream().filter(f -> "PENDING".equals(f.getStatus())).mapToDouble(Fine::getFineAmount).sum();
        long vehicles   = vehicleDAO.getVehiclesByOwner(ownerId).size();

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setBackground(new Color(15, 23, 42));
        statsRow.setBorder(new EmptyBorder(16, 20, 8, 20));
        statsRow.add(miniStat("Pending Fines",   String.valueOf(pending),              new Color(245, 158, 11)));
        statsRow.add(miniStat("Total Due",        "₹" + String.format("%.0f", totalDue),new Color(239, 68, 68)));
        statsRow.add(miniStat("My Vehicles",      String.valueOf(vehicles),             new Color(59, 130, 246)));

        // ── Fines table ───────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setBackground(new Color(15, 23, 42));
        tablePanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel tableTitle = new JLabel("My Fines");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(new Color(226, 232, 240));

        String[] cols = {"Fine #", "Plate", "Violation", "Amount (₹)", "Location", "Status", "Issued On"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        finesTable = new JTable(tableModel);
        finesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        finesTable.setForeground(new Color(226, 232, 240));
        finesTable.setBackground(new Color(15, 23, 42));
        finesTable.setGridColor(new Color(30, 41, 59));
        finesTable.setRowHeight(34);
        finesTable.setSelectionBackground(new Color(51, 65, 85));
        finesTable.setFillsViewportHeight(true);
        JTableHeader th = finesTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(30, 41, 59));
        th.setForeground(new Color(148, 163, 184));

        finesTable.getColumnModel().getColumn(5).setCellRenderer(new AllFinesScreen.StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(finesTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        scroll.getViewport().setBackground(new Color(15, 23, 42));

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        // ── Bottom bar ────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setBackground(new Color(15, 23, 42));
        JButton btnPay     = actionBtn("✅ Pay Fine",       new Color(16, 185, 129));
        JButton btnDispute = actionBtn("⚠ Dispute Fine",  new Color(245, 158, 11));
        JButton btnRefresh = actionBtn("🔄 Refresh",       new Color(51, 65, 85));
        bottomBar.add(btnPay);
        bottomBar.add(btnDispute);
        bottomBar.add(btnRefresh);

        root.add(header,   BorderLayout.NORTH);
        root.add(statsRow, BorderLayout.NORTH);
        // Use a compound layout
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(15, 23, 42));
        contentArea.add(statsRow, BorderLayout.NORTH);
        contentArea.add(tablePanel, BorderLayout.CENTER);
        contentArea.add(bottomBar, BorderLayout.SOUTH);

        root.add(header,      BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);

        // ── Listeners ─────────────────────────────────────────────
        btnLogout.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            new LoginScreen().setVisible(true);
        });

        btnPay.addActionListener(e -> {
            Fine f = getSelectedFine();
            if (f == null) return;
            if ("PAID".equals(f.getStatus())) {
                JOptionPane.showMessageDialog(this, "This fine is already paid.");
                return;
            }
            int res = JOptionPane.showConfirmDialog(this,
                "Pay fine of ₹" + f.getFineAmount() + " for " + f.getViolationName() + "?\n(Simulated payment)",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                fineDAO.payFine(f.getId());
                JOptionPane.showMessageDialog(this, "✓ Payment successful! Fine #" + f.getId() + " marked as PAID.");
                loadMyFines();
            }
        });

        btnDispute.addActionListener(e -> {
            Fine f = getSelectedFine();
            if (f == null) return;
            if ("PAID".equals(f.getStatus())) {
                JOptionPane.showMessageDialog(this, "Cannot dispute a paid fine.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Dispute fine #" + f.getId() + "?",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                fineDAO.disputeFine(f.getId());
                loadMyFines();
            }
        });

        btnRefresh.addActionListener(e -> loadMyFines());
    }

    private void loadMyFines() {
        tableModel.setRowCount(0);
        int ownerId = SessionManager.getCurrentUser().getId();
        for (Fine f : fineDAO.getFinesByOwner(ownerId)) {
            tableModel.addRow(new Object[]{
                f.getId(), f.getPlateNumber(), f.getViolationName(),
                String.format("%.2f", f.getFineAmount()), f.getLocation(),
                f.getStatus(),
                f.getIssuedAt() != null ? f.getIssuedAt().toString().substring(0, 16) : "—"
            });
        }
        finesTable.getColumnModel().getColumn(5).setCellRenderer(new AllFinesScreen.StatusCellRenderer());
    }

    private Fine getSelectedFine() {
        int row = finesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a fine from the table first.");
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int ownerId = SessionManager.getCurrentUser().getId();
        return fineDAO.getFinesByOwner(ownerId).stream()
            .filter(f -> f.getId() == id).findFirst().orElse(null);
    }

    private JPanel miniStat(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(accent);
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLbl.setForeground(new Color(148, 163, 184));
        JPanel text = new JPanel();
        text.setBackground(new Color(30, 41, 59));
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(valLbl);
        text.add(nameLbl);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        return b;
    }
}
