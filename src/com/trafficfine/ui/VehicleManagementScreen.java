package com.trafficfine.ui;

import com.trafficfine.dao.UserDAO;
import com.trafficfine.dao.VehicleDAO;
import com.trafficfine.model.User;
import com.trafficfine.model.Vehicle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class VehicleManagementScreen extends JFrame {

    private final JFrame parent;
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final UserDAO userDAO = new UserDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public VehicleManagementScreen(JFrame parent) {
        this.parent = parent;
        setTitle("Vehicle Management");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);
        initUI();
        loadVehicles();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { goBack(); }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title + back
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 23, 42));
        JLabel title = new JLabel("Registered Vehicles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(226, 232, 240));
        JButton btnBack = btn("← Back", new Color(30, 41, 59));
        btnBack.addActionListener(e -> goBack());
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnBack, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Plate Number", "Owner", "Type", "Brand", "Color"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(226, 232, 240));
        table.setBackground(new Color(15, 23, 42));
        table.setGridColor(new Color(30, 41, 59));
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(51, 65, 85));
        table.setFillsViewportHeight(true);
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setBackground(new Color(30, 41, 59));
        h.setForeground(new Color(148, 163, 184));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        scroll.getViewport().setBackground(new Color(15, 23, 42));

        // Bottom
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomBar.setBackground(new Color(15, 23, 42));
        JButton btnAdd = btn("+ Register Vehicle", new Color(59, 130, 246));
        JButton btnDel = btn("🗑 Remove",           new Color(239, 68, 68));
        bottomBar.add(btnAdd);
        bottomBar.add(btnDel);

        root.add(topBar,    BorderLayout.NORTH);
        root.add(scroll,    BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(root);

        btnAdd.addActionListener(e -> showAddDialog());
        btnDel.addActionListener(e -> deleteSelected());
    }

    private void loadVehicles() {
        tableModel.setRowCount(0);
        for (Vehicle v : vehicleDAO.getAllVehicles()) {
            tableModel.addRow(new Object[]{
                v.getId(), v.getPlateNumber(), v.getOwnerName() != null ? v.getOwnerName() : "—",
                v.getVehicleType(), v.getBrand(), v.getColor()
            });
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Register Vehicle", true);
        dlg.setSize(420, 400);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(30, 41, 59));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtPlate = field("Plate Number (e.g. MH12AB1234)");
        JTextField txtBrand = field("Brand (e.g. Honda City)");
        JTextField txtColor = field("Color");
        String[] types = {"CAR", "BIKE", "TRUCK", "BUS"};
        JComboBox<String> cboType = new JComboBox<>(types);
        cboType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboType.setBackground(new Color(15, 23, 42));
        cboType.setForeground(new Color(226, 232, 240));

        List<User> owners = userDAO.getAllOwners();
        JComboBox<User> cboOwner = new JComboBox<>(owners.toArray(new User[0]));
        cboOwner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboOwner.setBackground(new Color(15, 23, 42));
        cboOwner.setForeground(new Color(226, 232, 240));

        panel.add(lbl("Plate Number")); panel.add(txtPlate);
        panel.add(lbl("Brand"));        panel.add(txtBrand);
        panel.add(lbl("Color"));        panel.add(txtColor);
        panel.add(lbl("Type"));         panel.add(cboType);
        panel.add(lbl("Owner"));        panel.add(cboOwner);

        JButton save = btn("Register", new Color(59, 130, 246));
        save.addActionListener(e -> {
            String plate = txtPlate.getText().trim().toUpperCase();
            String brand = txtBrand.getText().trim();
            String color = txtColor.getText().trim();
            if (plate.isEmpty() || brand.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Fill all required fields.");
                return;
            }
            Vehicle v = new Vehicle();
            v.setPlateNumber(plate);
            v.setBrand(brand);
            v.setColor(color);
            v.setVehicleType((String) cboType.getSelectedItem());
            User owner = (User) cboOwner.getSelectedItem();
            if (owner != null) v.setOwnerId(owner.getId());
            if (vehicleDAO.addVehicle(v)) {
                loadVehicles();
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed. Plate may already exist.");
            }
        });
        panel.add(save);
        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a vehicle first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String plate = (String) tableModel.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "Remove vehicle " + plate + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            vehicleDAO.deleteVehicle(id);
            loadVehicles();
        }
    }

    private void goBack() {
        dispose();
        parent.dispose();
        new AdminDashboard().setVisible(true);
    }

    private JTextField field(String tip) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(new Color(226, 232, 240));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        f.setToolTipText(tip);
        return f;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    private JButton btn(String text, Color bg) {
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
}
