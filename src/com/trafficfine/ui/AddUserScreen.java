package com.trafficfine.ui;

import com.trafficfine.dao.UserDAO;
import com.trafficfine.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddUserScreen extends JDialog {

    private final UserDAO userDAO = new UserDAO();

    public AddUserScreen(JFrame parent) {
        super(parent, "Add Owner Account", true);
        setSize(400, 380);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 41, 59));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Register Vehicle Owner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(226, 232, 240));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JTextField txtName     = addField(panel, "Full Name");
        JTextField txtUsername = addField(panel, "Username");
        JTextField txtPassword = addField(panel, "Password");
        JTextField txtPhone    = addField(panel, "Phone Number");

        panel.add(Box.createVerticalStrut(16));

        JButton btnSave = new JButton("Create Account");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(59, 130, 246));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(btnSave);

        setContentPane(panel);

        btnSave.addActionListener(e -> {
            String name  = txtName.getText().trim();
            String uname = txtUsername.getText().trim();
            String pass  = txtPassword.getText().trim();
            String phone = txtPhone.getText().trim();

            if (name.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, username and password are required.");
                return;
            }
            User user = new User();
            user.setFullName(name);
            user.setUsername(uname);
            user.setPassword(pass);
            user.setPhone(phone);
            user.setRole("OWNER");

            if (userDAO.addUser(user)) {
                JOptionPane.showMessageDialog(this, "✓ Owner account created!\nUsername: " + uname);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed. Username may already exist.");
            }
        });
    }

    private JTextField addField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);

        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(new Color(226, 232, 240));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            new EmptyBorder(7, 10, 7, 10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(f);
        panel.add(Box.createVerticalStrut(8));
        return f;
    }
}
