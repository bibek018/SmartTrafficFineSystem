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
            public void windowClosing(java.awt.event.WindowEvent e) {
                goBack();
            }
        });
    }

    private void initUI() {

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= TOP BAR =================

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 23, 42));

        JLabel title = new JLabel("Registered Vehicles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(241, 245, 249));

        JButton btnBack = btn("← Back", new Color(30, 41, 59));
        btnBack.addActionListener(e -> goBack());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnBack, BorderLayout.EAST);

        // ================= TABLE =================

        String[] cols = {
                "ID",
                "Plate Number",
                "Owner",
                "Type",
                "Brand",
                "Color"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(new Color(226, 232, 240));
        table.setBackground(new Color(15, 23, 42));

        table.setGridColor(new Color(30, 41, 59));

        table.setRowHeight(34);

        table.setSelectionBackground(new Color(51, 65, 85));

        table.setFillsViewportHeight(true);

        JTableHeader h = table.getTableHeader();

        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(30, 41, 59));
        h.setForeground(new Color(148, 163, 184));

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85))
        );

        scroll.getViewport().setBackground(new Color(15, 23, 42));

        // ================= BOTTOM BAR =================

        JPanel bottomBar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0)
        );

        bottomBar.setBackground(new Color(15, 23, 42));

        JButton btnAdd = btn(
                "+ Register Vehicle",
                new Color(59, 130, 246)
        );

        JButton btnDel = btn(
                "🗑 Remove",
                new Color(239, 68, 68)
        );

        bottomBar.add(btnAdd);
        bottomBar.add(btnDel);

        // ================= ROOT =================

        root.add(topBar, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);

        // ================= EVENTS =================

        btnAdd.addActionListener(e -> showAddDialog());

        btnDel.addActionListener(e -> deleteSelected());
    }

    private void loadVehicles() {

        tableModel.setRowCount(0);

        for (Vehicle v : vehicleDAO.getAllVehicles()) {

            tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getPlateNumber(),
                    v.getOwnerName() != null
                            ? v.getOwnerName()
                            : "—",
                    v.getVehicleType(),
                    v.getBrand(),
                    v.getColor()
            });
        }
    }

    private void showAddDialog() {

        JDialog dlg = new JDialog(this, "Register Vehicle", true);

        dlg.setSize(480, 520);

        dlg.setLocationRelativeTo(this);

        dlg.getContentPane().setBackground(new Color(30, 41, 59));

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBackground(new Color(30, 41, 59));

        panel.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ================= FIELDS =================

        JTextField txtPlate = field(
                "Plate Number (e.g. MH12AB1234)"
        );

        JTextField txtBrand = field(
                "Brand (e.g. Honda City)"
        );

        JTextField txtColor = field(
                "Color"
        );

        String[] types = {
                "CAR",
                "BIKE",
                "TRUCK",
                "BUS"
        };

        JComboBox<String> cboType =
                new JComboBox<>(types);

        styleCombo(cboType);

        List<User> owners = userDAO.getAllOwners();

        JComboBox<User> cboOwner =
                new JComboBox<>(owners.toArray(new User[0]));

        styleCombo(cboOwner);

        // ================= FORM =================

        panel.add(lbl("Plate Number"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(txtPlate);

        panel.add(Box.createVerticalStrut(16));

        panel.add(lbl("Brand"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(txtBrand);

        panel.add(Box.createVerticalStrut(16));

        panel.add(lbl("Color"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(txtColor);

        panel.add(Box.createVerticalStrut(16));

        panel.add(lbl("Type"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(cboType);

        panel.add(Box.createVerticalStrut(16));

        panel.add(lbl("Owner"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(cboOwner);

        panel.add(Box.createVerticalStrut(24));

        JButton save = btn(
                "Register Vehicle",
                new Color(59, 130, 246)
        );

        save.setAlignmentX(Component.CENTER_ALIGNMENT);

        save.addActionListener(e -> {

            String plate =
                    txtPlate.getText()
                            .trim()
                            .toUpperCase();

            String brand =
                    txtBrand.getText().trim();

            String color =
                    txtColor.getText().trim();

            if (plate.isEmpty() || brand.isEmpty()) {

                JOptionPane.showMessageDialog(
                        dlg,
                        "Fill all required fields."
                );

                return;
            }

            Vehicle v = new Vehicle();

            v.setPlateNumber(plate);

            v.setBrand(brand);

            v.setColor(color);

            v.setVehicleType(
                    (String) cboType.getSelectedItem()
            );

            User owner =
                    (User) cboOwner.getSelectedItem();

            if (owner != null) {
                v.setOwnerId(owner.getId());
            }

            if (vehicleDAO.addVehicle(v)) {

                loadVehicles();

                dlg.dispose();

            } else {

                JOptionPane.showMessageDialog(
                        dlg,
                        "Failed. Plate may already exist."
                );
            }
        });

        panel.add(save);

        dlg.setContentPane(panel);

        dlg.setVisible(true);
    }

    private void deleteSelected() {

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a vehicle first."
            );

            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);

        String plate =
                (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove vehicle " + plate + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            vehicleDAO.deleteVehicle(id);

            loadVehicles();
        }
    }

    private void goBack() {

        dispose();

        parent.dispose();

        new AdminDashboard().setVisible(true);
    }

    // ================= COMPONENTS =================

    private JTextField field(String tip) {

        JTextField f = new JTextField();

        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        f.setPreferredSize(new Dimension(0, 42));

        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        f.setBackground(new Color(15, 23, 42));

        f.setForeground(new Color(226, 232, 240));

        f.setCaretColor(Color.WHITE);

        f.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(51, 65, 85)
                        ),
                        new EmptyBorder(8, 12, 8, 12)
                )
        );

        f.setToolTipText(tip);

        return f;
    }

    private <T> void styleCombo(JComboBox<T> combo) {

        combo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        combo.setPreferredSize(
                new Dimension(0, 42)
        );

        combo.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        combo.setBackground(
                new Color(15, 23, 42)
        );

        combo.setForeground(
                new Color(226, 232, 240)
        );

        combo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(51, 65, 85)
                )
        );
    }

    private JLabel lbl(String text) {

        JLabel l = new JLabel(text);

        l.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        l.setForeground(
                new Color(148, 163, 184)
        );

        return l;
    }

    private JButton btn(String text, Color bg) {

        JButton b = new JButton(text);

        b.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        b.setBackground(bg);

        b.setForeground(Color.WHITE);

        b.setBorderPainted(false);

        b.setFocusPainted(false);

        b.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        b.setBorder(
                new EmptyBorder(10, 18, 10, 18)
        );

        return b;
    }
}