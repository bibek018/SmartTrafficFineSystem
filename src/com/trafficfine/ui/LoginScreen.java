package com.trafficfine.ui;

import com.trafficfine.dao.UserDAO;
import com.trafficfine.model.User;
import com.trafficfine.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginScreen extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private final UserDAO userDAO = new UserDAO();

    public LoginScreen() {
        setTitle("Smart Traffic Fine System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));

        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(new Color(15, 23, 42));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(40, 0, 20, 0));

        JLabel icon = new JLabel("🚦", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("SMART TRAFFIC FINE SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(226, 232, 240));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Secure Officer & Owner Portal");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ── Card Panel ──────────────────────────────────────────
        JPanel card = new JPanel();
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            new EmptyBorder(30, 40, 30, 40)
        ));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel loginLabel = new JLabel("Sign In");
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginLabel.setForeground(new Color(226, 232, 240));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(loginLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 2;
        card.add(Box.createVerticalStrut(6), gbc);

        // Username
        JLabel lblUser = makeLabel("Username");
        gbc.gridy = 2; gbc.gridwidth = 2;
        card.add(lblUser, gbc);

        txtUsername = makeTextField();
        gbc.gridy = 3;
        card.add(txtUsername, gbc);

        // Password
        JLabel lblPass = makeLabel("Password");
        gbc.gridy = 4;
        card.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        styleTextField(txtPassword);
        gbc.gridy = 5;
        card.add(txtPassword, gbc);

        // Login button
        btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(59, 130, 246));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 44));
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(new Color(37, 99, 235)); }
            public void mouseExited(MouseEvent e)  { btnLogin.setBackground(new Color(59, 130, 246)); }
        });
        gbc.gridy = 6; gbc.insets = new Insets(16, 0, 6, 0);
        card.add(btnLogin, gbc);

        // Hint
        JLabel hint = new JLabel("Admin: admin/admin123  |  Owner: owner1/owner123");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hint.setForeground(new Color(71, 85, 105));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7; gbc.insets = new Insets(8, 0, 0, 0);
        card.add(hint, gbc);

        // ── Center the card ─────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(15, 23, 42));
        center.setBorder(new EmptyBorder(0, 40, 40, 40));
        center.add(card);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);

        // ── Actions ─────────────────────────────────────────────
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
    }

    private void doLogin() {
        String uname = txtUsername.getText().trim();
        String pass  = new String(txtPassword.getPassword());

        if (uname.isEmpty() || pass.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        btnLogin.setText("Signing in...");
        btnLogin.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            protected User doInBackground() {
                return userDAO.authenticate(uname, pass);
            }
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.setCurrentUser(user);
                        dispose();
                        if ("ADMIN".equals(user.getRole())) {
                            new AdminDashboard().setVisible(true);
                        } else {
                            new OwnerDashboard().setVisible(true);
                        }
                    } else {
                        showError("Invalid username or password.");
                        btnLogin.setText("Sign In");
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    showError("Login failed: " + ex.getMessage());
                    btnLogin.setText("Sign In");
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    private JTextField makeTextField() {
        JTextField f = new JTextField();
        styleTextField(f);
        return f;
    }

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(new Color(226, 232, 240));
        f.setCaretColor(new Color(226, 232, 240));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setPreferredSize(new Dimension(0, 42));
    }
}
