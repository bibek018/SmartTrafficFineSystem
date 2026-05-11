package com.trafficfine.ui;

import com.trafficfine.dao.FineDAO;
import com.trafficfine.model.Fine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AllFinesScreen extends JFrame {

    private final JFrame parent;
    private final FineDAO fineDAO = new FineDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public AllFinesScreen(JFrame parent) {
        this.parent = parent;
        setTitle("All Traffic Fines");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        initUI();
        loadFines(fineDAO.getAllFines());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { goBack(); }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Top bar ──────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(15, 23, 42));

        JLabel title = new JLabel("All Fines");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(226, 232, 240));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(new Color(15, 23, 42));

        txtSearch = new JTextField(16);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBackground(new Color(30, 41, 59));
        txtSearch.setForeground(new Color(226, 232, 240));
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setToolTipText("Search by plate number");

        JButton btnSearch = actionBtn("🔍 Search", new Color(59, 130, 246));
        JButton btnAll    = actionBtn("Show All",   new Color(51, 65, 85));
        JButton btnBack   = actionBtn("← Back",     new Color(30, 41, 59));

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnAll);
        searchPanel.add(btnBack);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);

        // ── Table ────────────────────────────────────────────────
        String[] cols = {"ID", "Plate", "Owner", "Violation", "Amount (₹)", "Location", "Officer", "Status", "Issued At"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(new Color(30, 41, 59));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        scroll.getViewport().setBackground(new Color(15, 23, 42));

        // ── Bottom action bar ────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomBar.setBackground(new Color(15, 23, 42));

        JButton btnPay     = actionBtn("✅ Mark as Paid",   new Color(16, 185, 129));
        JButton btnDispute = actionBtn("⚠ Dispute Fine",   new Color(245, 158, 11));
        JButton btnView    = actionBtn("🖼 View Snapshot", new Color(59, 130, 246));

        bottomBar.add(btnPay);
        bottomBar.add(btnDispute);
        bottomBar.add(btnView);

        root.add(topBar,   BorderLayout.NORTH);
        root.add(scroll,   BorderLayout.CENTER);
        root.add(bottomBar,BorderLayout.SOUTH);
        setContentPane(root);

        // ── Listeners ────────────────────────────────────────────
        btnSearch.addActionListener(e -> {
            String q = txtSearch.getText().trim();
            if (!q.isEmpty()) loadFines(fineDAO.searchFinesByPlate(q));
        });
        txtSearch.addActionListener(e -> btnSearch.doClick());
        btnAll.addActionListener(e -> loadFines(fineDAO.getAllFines()));
        btnBack.addActionListener(e -> goBack());

        btnPay.addActionListener(e -> {
            Fine f = getSelectedFine();
            if (f == null) return;
            if ("PAID".equals(f.getStatus())) { warn("Already paid."); return; }
            if (confirm("Mark fine #" + f.getId() + " as PAID?")) {
                fineDAO.payFine(f.getId());
                loadFines(fineDAO.getAllFines());
            }
        });

        btnDispute.addActionListener(e -> {
            Fine f = getSelectedFine();
            if (f == null) return;
            if ("PAID".equals(f.getStatus())) { warn("Cannot dispute a paid fine."); return; }
            if (confirm("Mark fine #" + f.getId() + " as DISPUTED?")) {
                fineDAO.disputeFine(f.getId());
                loadFines(fineDAO.getAllFines());
            }
        });

        btnView.addActionListener(e -> {
            Fine f = getSelectedFine();
            if (f == null) return;
            String path = f.getCapturedImagePath();
            if (path == null || path.isEmpty()) { warn("No snapshot for this fine."); return; }
            showSnapshot(path);
        });
    }

    private void loadFines(List<Fine> fines) {
        tableModel.setRowCount(0);
        for (Fine f : fines) {
            tableModel.addRow(new Object[]{
                f.getId(),
                f.getPlateNumber(),
                f.getOwnerName() != null ? f.getOwnerName() : "—",
                f.getViolationName(),
                String.format("%.2f", f.getFineAmount()),
                f.getLocation(),
                f.getOfficerName(),
                f.getStatus(),
                f.getIssuedAt() != null ? f.getIssuedAt().toString().substring(0, 16) : "—"
            });
        }
        // Color status column
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(new Color(226, 232, 240));
        table.setBackground(new Color(15, 23, 42));
        table.setGridColor(new Color(30, 41, 59));
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(51, 65, 85));
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 41, 59));
        header.setForeground(new Color(148, 163, 184));
        header.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        // Column widths
        int[] widths = {40, 110, 110, 150, 90, 120, 110, 80, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private Fine getSelectedFine() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Select a fine first."); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return fineDAO.getAllFines().stream().filter(f -> f.getId() == id).findFirst().orElse(null);
    }

    private void showSnapshot(String path) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() == -1) { warn("Image file not found:\n" + path); return; }
        Image scaled = icon.getImage().getScaledInstance(480, 320, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        JOptionPane.showMessageDialog(this, imgLabel, "Snapshot: " + path, JOptionPane.PLAIN_MESSAGE);
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBack() {
        dispose();
        parent.dispose();
        new AdminDashboard().setVisible(true);
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    // ── Custom renderer for Status column ────────────────────────
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            String status = v == null ? "" : v.toString();
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            switch (status) {
                case "PAID"     -> { setBackground(new Color(6, 78, 59));   setForeground(new Color(52, 211, 153)); }
                case "PENDING"  -> { setBackground(new Color(120, 53, 15)); setForeground(new Color(251, 191, 36)); }
                case "DISPUTED" -> { setBackground(new Color(69, 10, 10));  setForeground(new Color(252, 165, 165)); }
                default         -> { setBackground(new Color(15, 23, 42));  setForeground(Color.WHITE); }
            }
            return this;
        }
    }
}
