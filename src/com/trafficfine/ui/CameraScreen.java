package com.trafficfine.ui;

import com.trafficfine.camera.CameraCapture;
import com.trafficfine.dao.FineDAO;
import com.trafficfine.dao.VehicleDAO;
import com.trafficfine.model.Fine;
import com.trafficfine.model.Vehicle;
import com.trafficfine.model.ViolationType;
import com.trafficfine.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class CameraScreen extends JFrame {

    private final JFrame parent;
    private CameraCapture camera;
    private Timer liveTimer;

    // Camera panel
    private JLabel cameraLabel;
    private JButton btnCapture;
    private JButton btnStartStop;
    private JLabel statusLabel;

    // Violation form
    private JTextField txtPlate;
    private JComboBox<ViolationType> cboViolation;
    private JTextField txtLocation;
    private JLabel lblFineAmount;
    private JButton btnIssueFine;
    private JLabel lblCapturedPath;

    private String capturedImagePath = null;
    private boolean cameraRunning = false;

    private final FineDAO fineDAO = new FineDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public CameraScreen(JFrame parent) {
        this.parent = parent;
        setTitle("Camera — Issue Traffic Fine");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 660);
        setLocationRelativeTo(null);
        camera = new CameraCapture();
        initUI();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { goBack(); }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(12, 0));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Left: Camera Feed ────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(new Color(15, 23, 42));

        JLabel camTitle = new JLabel("Live Camera Feed");
        camTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        camTitle.setForeground(new Color(226, 232, 240));
        leftPanel.add(camTitle, BorderLayout.NORTH);

        cameraLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        cameraLabel.setPreferredSize(new Dimension(540, 380));
        cameraLabel.setBackground(new Color(30, 41, 59));
        cameraLabel.setForeground(new Color(100, 116, 139));
        cameraLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cameraLabel.setOpaque(true);
        cameraLabel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        leftPanel.add(cameraLabel, BorderLayout.CENTER);

        statusLabel = new JLabel("● Camera stopped", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(239, 68, 68));

        JPanel camButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        camButtons.setBackground(new Color(15, 23, 42));

        btnStartStop = styledBtn("▶ Start Camera", new Color(16, 185, 129));
        btnCapture   = styledBtn("📸 Snapshot", new Color(59, 130, 246));
        btnCapture.setEnabled(false);

        camButtons.add(btnStartStop);
        camButtons.add(btnCapture);
        camButtons.add(statusLabel);

        JPanel bottomLeft = new JPanel(new BorderLayout());
        bottomLeft.setBackground(new Color(15, 23, 42));
        bottomLeft.add(camButtons, BorderLayout.CENTER);

        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backBtn.setForeground(new Color(148, 163, 184));
        backBtn.setBackground(new Color(30, 41, 59));
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> goBack());
        bottomLeft.add(backBtn, BorderLayout.EAST);

        leftPanel.add(bottomLeft, BorderLayout.SOUTH);

        // ── Right: Violation Form ────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(30, 41, 59));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(360, 0));

        JLabel formTitle = new JLabel("Issue Violation");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(226, 232, 240));
        rightPanel.add(formTitle);
        rightPanel.add(Box.createVerticalStrut(20));

        // Plate
        rightPanel.add(formLabel("License Plate Number"));
        txtPlate = formField("e.g. MH12AB1234");
        rightPanel.add(txtPlate);
        rightPanel.add(Box.createVerticalStrut(4));

        JButton btnLookup = styledBtn("🔍 Lookup Vehicle", new Color(51, 65, 85));
        btnLookup.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(btnLookup);
        rightPanel.add(Box.createVerticalStrut(12));

        // Violation type
        rightPanel.add(formLabel("Violation Type"));
        List<ViolationType> violations = fineDAO.getAllViolationTypes();
        cboViolation = new JComboBox<>(violations.toArray(new ViolationType[0]));
        cboViolation.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboViolation.setBackground(new Color(15, 23, 42));
        cboViolation.setForeground(new Color(226, 232, 240));
        cboViolation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cboViolation.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(cboViolation);
        rightPanel.add(Box.createVerticalStrut(12));

        // Fine amount display
        rightPanel.add(formLabel("Fine Amount"));
        lblFineAmount = new JLabel("₹0.00");
        lblFineAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFineAmount.setForeground(new Color(245, 158, 11));
        rightPanel.add(lblFineAmount);
        rightPanel.add(Box.createVerticalStrut(12));

        // Location
        rightPanel.add(formLabel("Location / Checkpoint"));
        txtLocation = formField("e.g. MG Road Junction");
        rightPanel.add(txtLocation);
        rightPanel.add(Box.createVerticalStrut(12));

        // Snapshot status
        lblCapturedPath = new JLabel("No snapshot taken");
        lblCapturedPath.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCapturedPath.setForeground(new Color(100, 116, 139));
        rightPanel.add(lblCapturedPath);
        rightPanel.add(Box.createVerticalStrut(20));

        // Issue button
        btnIssueFine = styledBtn("⚡ Issue Fine", new Color(239, 68, 68));
        btnIssueFine.setAlignmentX(LEFT_ALIGNMENT);
        btnIssueFine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        rightPanel.add(btnIssueFine);

        root.add(leftPanel, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        setContentPane(root);

        // ── Event Listeners ──────────────────────────────────────
        btnStartStop.addActionListener(e -> toggleCamera());
        btnCapture.addActionListener(e -> takeSnapshot());
        btnLookup.addActionListener(e -> lookupVehicle());
        cboViolation.addActionListener(e -> updateFineAmount());
        btnIssueFine.addActionListener(e -> issueFine());

        updateFineAmount();
    }

    private void toggleCamera() {
        if (!cameraRunning) {
            boolean started = camera.startCamera();
            if (!started) {
                JOptionPane.showMessageDialog(this, "Could not open webcam.\nCheck camera connection.", "Camera Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cameraRunning = true;
            btnStartStop.setText("⏹ Stop Camera");
            btnStartStop.setBackground(new Color(239, 68, 68));
            btnCapture.setEnabled(true);
            statusLabel.setText("● Live");
            statusLabel.setForeground(new Color(16, 185, 129));

            liveTimer = new Timer(40, e -> {  // ~25 fps
                BufferedImage frame = camera.captureFrame();
                if (frame != null) {
                    Image scaled = frame.getScaledInstance(
                            cameraLabel.getWidth(), cameraLabel.getHeight(), Image.SCALE_FAST);
                    cameraLabel.setIcon(new ImageIcon(scaled));
                    cameraLabel.setText("");
                }
            });
            liveTimer.start();
        } else {
            stopCamera();
        }
    }

    private void stopCamera() {
        if (liveTimer != null) liveTimer.stop();
        camera.stopCamera();
        cameraRunning = false;
        btnStartStop.setText("▶ Start Camera");
        btnStartStop.setBackground(new Color(16, 185, 129));
        btnCapture.setEnabled(false);
        statusLabel.setText("● Camera stopped");
        statusLabel.setForeground(new Color(239, 68, 68));
        cameraLabel.setIcon(null);
        cameraLabel.setText("Camera stopped");
    }

    private void takeSnapshot() {
        String plate = txtPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) plate = "UNKNOWN";
        capturedImagePath = camera.saveSnapshot(plate);
        if (capturedImagePath != null) {
            lblCapturedPath.setText("✓ Saved: " + capturedImagePath);
            lblCapturedPath.setForeground(new Color(16, 185, 129));
        } else {
            lblCapturedPath.setText("✗ Snapshot failed");
            lblCapturedPath.setForeground(new Color(239, 68, 68));
        }
    }

    private void lookupVehicle() {
        String plate = txtPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) return;
        Vehicle v = vehicleDAO.getVehicleByPlate(plate);
        if (v != null) {
            JOptionPane.showMessageDialog(this,
                    "Vehicle Found!\n" +
                            "Plate: " + v.getPlateNumber() + "\n" +
                            "Type: " + v.getVehicleType() + "\n" +
                            "Brand: " + v.getBrand() + "\n" +
                            "Color: " + v.getColor() + "\n" +
                            "Owner: " + v.getOwnerName(),
                    "Vehicle Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Vehicle not found in system.\nPlease register vehicle first.",
                    "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateFineAmount() {
        ViolationType vt = (ViolationType) cboViolation.getSelectedItem();
        if (vt != null) lblFineAmount.setText("₹" + String.format("%.2f", vt.getFineAmount()));
    }

    private void issueFine() {
        String plate    = txtPlate.getText().trim().toUpperCase();
        String location = txtLocation.getText().trim();
        ViolationType vt = (ViolationType) cboViolation.getSelectedItem();

        if (plate.isEmpty()) { showError("Enter license plate number."); return; }
        if (location.isEmpty()) { showError("Enter location/checkpoint."); return; }

        Vehicle vehicle = vehicleDAO.getVehicleByPlate(plate);
        if (vehicle == null) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Vehicle " + plate + " not registered.\nRegister and issue fine?",
                    "Vehicle Not Found", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
            // Can't issue without vehicle; go to vehicle screen
            JOptionPane.showMessageDialog(this, "Please register vehicle first via Vehicles menu.");
            return;
        }

        // Auto-capture snapshot if camera is running and no snapshot yet
        if (cameraRunning && capturedImagePath == null) {
            capturedImagePath = camera.saveSnapshot(plate);
        }

        Fine fine = new Fine();
        fine.setVehicleId(vehicle.getId());
        fine.setViolationId(vt.getId());
        fine.setOfficerId(SessionManager.getCurrentUser().getId());
        fine.setCapturedImagePath(capturedImagePath);
        fine.setPlateDetected(plate);
        fine.setFineAmount(vt.getFineAmount());
        fine.setLocation(location);

        boolean ok = fineDAO.issueFine(fine);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "✓ Fine Issued Successfully!\n\n" +
                            "Vehicle: " + plate + "\n" +
                            "Violation: " + vt.getName() + "\n" +
                            "Amount: ₹" + vt.getFineAmount() + "\n" +
                            "Location: " + location,
                    "Fine Issued", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            showError("Failed to issue fine. Check database connection.");
        }
    }

    private void clearForm() {
        txtPlate.setText("");
        txtLocation.setText("");
        capturedImagePath = null;
        lblCapturedPath.setText("No snapshot taken");
        lblCapturedPath.setForeground(new Color(100, 116, 139));
        cboViolation.setSelectedIndex(0);
        updateFineAmount();
    }

    private void goBack() {
        stopCamera();
        dispose();
        parent.setVisible(true);
        ((AdminDashboard) parent).setVisible(true);
        // Recreate dashboard to refresh stats
        AdminDashboard fresh = new AdminDashboard();
        parent.dispose();
        fresh.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(148, 163, 184));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField formField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(new Color(226, 232, 240));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setToolTipText(placeholder);
        return f;
    }

    private JButton styledBtn(String text, Color bg) {
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