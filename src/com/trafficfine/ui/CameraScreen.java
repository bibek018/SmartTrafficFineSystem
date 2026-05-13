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
    private JButton btnReadPlate;       // ← NEW: triggers OCR
    private JLabel statusLabel;
    private JLabel ocrStatusLabel;      // ← NEW: shows OCR result / progress

    // Violation form
    private JTextField txtPlate;
    private JComboBox<ViolationType> cboViolation;
    private JTextField txtLocation;
    private JLabel lblFineAmount;
    private JButton btnIssueFine;
    private JLabel lblCapturedPath;
    private JPanel plateConfirmPanel;   // ← NEW: confirm/edit OCR result
    private JLabel lblOcrResult;        // ← NEW: big OCR result label
    private JButton btnConfirmPlate;    // ← NEW: accept OCR reading
    private JButton btnEditPlate;       // ← NEW: reject, type manually

    private String capturedImagePath = null;
    private boolean cameraRunning    = false;

    private final FineDAO fineDAO       = new FineDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public CameraScreen(JFrame parent) {
        this.parent = parent;
        setTitle("Camera — Issue Traffic Fine");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1060, 680);
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

        // ── LEFT: Camera Feed ─────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(new Color(15, 23, 42));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(15, 23, 42));
        JLabel camTitle = new JLabel("Live Camera Feed");
        camTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        camTitle.setForeground(new Color(226, 232, 240));
        ocrStatusLabel = new JLabel("OCR ready");
        ocrStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        ocrStatusLabel.setForeground(new Color(100, 116, 139));
        ocrStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        titleRow.add(camTitle, BorderLayout.WEST);
        titleRow.add(ocrStatusLabel, BorderLayout.EAST);
        leftPanel.add(titleRow, BorderLayout.NORTH);

        // Camera display
        cameraLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        cameraLabel.setPreferredSize(new Dimension(560, 390));
        cameraLabel.setBackground(new Color(30, 41, 59));
        cameraLabel.setForeground(new Color(100, 116, 139));
        cameraLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cameraLabel.setOpaque(true);
        cameraLabel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        leftPanel.add(cameraLabel, BorderLayout.CENTER);

        // Camera controls row
        statusLabel = new JLabel("● Stopped", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(239, 68, 68));

        JPanel camButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        camButtons.setBackground(new Color(15, 23, 42));

        btnStartStop  = styledBtn("▶ Start Camera",  new Color(16, 185, 129));
        btnCapture    = styledBtn("📸 Snapshot",      new Color(59, 130, 246));
        btnReadPlate  = styledBtn("🔍 Read Plate (OCR)", new Color(139, 92, 246));
        btnCapture.setEnabled(false);
        btnReadPlate.setEnabled(false);

        camButtons.add(btnStartStop);
        camButtons.add(btnCapture);
        camButtons.add(btnReadPlate);
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

        // ── RIGHT: Violation Form ─────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(30, 41, 59));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(370, 0));

        JLabel formTitle = new JLabel("Issue Violation");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(226, 232, 240));
        rightPanel.add(formTitle);
        rightPanel.add(Box.createVerticalStrut(4));

        // OCR hint
        JLabel ocrHint = new JLabel("Tip: Point camera at plate → click Read Plate (OCR)");
        ocrHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        ocrHint.setForeground(new Color(139, 92, 246));
        rightPanel.add(ocrHint);
        rightPanel.add(Box.createVerticalStrut(16));

        // ── OCR Result Confirmation Panel ─────────────────────────
        // This panel appears after OCR runs, shows detected text,
        // lets officer confirm or edit before proceeding
        plateConfirmPanel = new JPanel();
        plateConfirmPanel.setBackground(new Color(20, 30, 50));
        plateConfirmPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(139, 92, 246), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        plateConfirmPanel.setLayout(new BoxLayout(plateConfirmPanel, BoxLayout.Y_AXIS));
        plateConfirmPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        plateConfirmPanel.setAlignmentX(LEFT_ALIGNMENT);
        plateConfirmPanel.setVisible(false);   // Hidden until OCR runs

        JLabel ocrLabel = new JLabel("OCR DETECTED PLATE:");
        ocrLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        ocrLabel.setForeground(new Color(139, 92, 246));
        ocrLabel.setAlignmentX(LEFT_ALIGNMENT);

        lblOcrResult = new JLabel("—");
        lblOcrResult.setFont(new Font("Courier New", Font.BOLD, 26));
        lblOcrResult.setForeground(new Color(52, 211, 153));
        lblOcrResult.setAlignmentX(LEFT_ALIGNMENT);

        JPanel ocrBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ocrBtns.setBackground(new Color(20, 30, 50));
        btnConfirmPlate = styledBtn("✓ Use This", new Color(16, 185, 129));
        btnConfirmPlate.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnEditPlate    = styledBtn("✎ Edit",     new Color(71, 85, 105));
        btnEditPlate.setBorder(new EmptyBorder(5, 10, 5, 10));
        ocrBtns.add(btnConfirmPlate);
        ocrBtns.add(btnEditPlate);

        plateConfirmPanel.add(ocrLabel);
        plateConfirmPanel.add(Box.createVerticalStrut(4));
        plateConfirmPanel.add(lblOcrResult);
        plateConfirmPanel.add(Box.createVerticalStrut(6));
        plateConfirmPanel.add(ocrBtns);

        rightPanel.add(plateConfirmPanel);
        rightPanel.add(Box.createVerticalStrut(12));

        // ── Plate field ──────────────────────────────────────────
        rightPanel.add(formLabel("License Plate Number"));
        txtPlate = formField("Auto-filled by OCR or type manually");
        rightPanel.add(txtPlate);
        rightPanel.add(Box.createVerticalStrut(4));

        JButton btnLookup = styledBtn("🔍 Lookup Vehicle", new Color(51, 65, 85));
        btnLookup.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(btnLookup);
        rightPanel.add(Box.createVerticalStrut(14));

        // ── Violation type ───────────────────────────────────────
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

        // ── Fine amount display ──────────────────────────────────
        rightPanel.add(formLabel("Fine Amount"));
        lblFineAmount = new JLabel("₹0.00");
        lblFineAmount.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblFineAmount.setForeground(new Color(245, 158, 11));
        lblFineAmount.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(lblFineAmount);
        rightPanel.add(Box.createVerticalStrut(12));

        // ── Location ─────────────────────────────────────────────
        rightPanel.add(formLabel("Location / Checkpoint"));
        txtLocation = formField("e.g. MG Road Signal 3");
        rightPanel.add(txtLocation);
        rightPanel.add(Box.createVerticalStrut(12));

        // ── Snapshot status ──────────────────────────────────────
        lblCapturedPath = new JLabel("No snapshot taken");
        lblCapturedPath.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblCapturedPath.setForeground(new Color(100, 116, 139));
        lblCapturedPath.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(lblCapturedPath);
        rightPanel.add(Box.createVerticalGlue());

        // ── Issue button ─────────────────────────────────────────
        btnIssueFine = styledBtn("⚡ Issue Fine", new Color(239, 68, 68));
        btnIssueFine.setAlignmentX(LEFT_ALIGNMENT);
        btnIssueFine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        rightPanel.add(btnIssueFine);

        root.add(leftPanel,  BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        setContentPane(root);

        // ── Event Listeners ───────────────────────────────────────
        btnStartStop.addActionListener(e -> toggleCamera());
        btnCapture.addActionListener(e -> takeSnapshot());
        btnReadPlate.addActionListener(e -> runOCR());
        btnConfirmPlate.addActionListener(e -> confirmOCRPlate());
        btnEditPlate.addActionListener(e -> editOCRPlate());
        btnLookup.addActionListener(e -> lookupVehicle());
        cboViolation.addActionListener(e -> updateFineAmount());
        btnIssueFine.addActionListener(e -> issueFine());

        updateFineAmount();
    }

    // ─────────────────────────────────────────────────────────────
    // Camera controls
    // ─────────────────────────────────────────────────────────────
    private void toggleCamera() {
        if (!cameraRunning) {
            boolean started = camera.startCamera();
            if (!started) {
                JOptionPane.showMessageDialog(this,
                        "Could not open webcam.\nCheck camera connection.",
                        "Camera Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cameraRunning = true;
            btnStartStop.setText("⏹ Stop Camera");
            btnStartStop.setBackground(new Color(239, 68, 68));
            btnCapture.setEnabled(true);
            btnReadPlate.setEnabled(true);
            statusLabel.setText("● Live");
            statusLabel.setForeground(new Color(16, 185, 129));

            liveTimer = new Timer(40, e -> {     // ~25 fps
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
        btnReadPlate.setEnabled(false);
        statusLabel.setText("● Stopped");
        statusLabel.setForeground(new Color(239, 68, 68));
        cameraLabel.setIcon(null);
        cameraLabel.setText("Camera stopped");
    }

    private void takeSnapshot() {
        String plate = txtPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) plate = "UNKNOWN";
        capturedImagePath = camera.saveSnapshot(plate);
        if (capturedImagePath != null) {
            lblCapturedPath.setText("✓ " + capturedImagePath);
            lblCapturedPath.setForeground(new Color(16, 185, 129));
        } else {
            lblCapturedPath.setText("✗ Snapshot failed");
            lblCapturedPath.setForeground(new Color(239, 68, 68));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // runOCR() — the core OCR flow:
    //   1. Pause the live feed briefly so we get a clean still frame
    //   2. Run Tesseract in a background SwingWorker (never block EDT)
    //   3. Show result in the confirmation panel
    //   4. Officer clicks "✓ Use This" or "✎ Edit"
    // ─────────────────────────────────────────────────────────────
    private void runOCR() {
        if (!cameraRunning) {
            showError("Start the camera first.");
            return;
        }

        // Pause live timer so we read a stable frame (not mid-motion blur)
        liveTimer.stop();

        // Update UI to show scanning state
        btnReadPlate.setEnabled(false);
        btnReadPlate.setText("⏳ Scanning...");
        ocrStatusLabel.setText("Running OCR…");
        ocrStatusLabel.setForeground(new Color(139, 92, 246));
        plateConfirmPanel.setVisible(false);

        // Run OCR off the EDT so the UI stays responsive
        SwingWorker<String, Void> ocrWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                // Take a snapshot for evidence at the same time
                String currentPlate = txtPlate.getText().trim().toUpperCase();
                if (currentPlate.isEmpty()) currentPlate = "SCANNING";
                capturedImagePath = camera.saveSnapshot(currentPlate);

                // Run Tesseract OCR on the captured frame
                return camera.readPlateOCR();
            }

            @Override
            protected void done() {
                // Restart live feed
                liveTimer.start();
                btnReadPlate.setEnabled(true);
                btnReadPlate.setText("🔍 Read Plate (OCR)");

                try {
                    String result = get();

                    if (result != null && !result.isEmpty()) {
                        // ── OCR Success ──────────────────────────
                        lblOcrResult.setText(result);
                        lblOcrResult.setForeground(new Color(52, 211, 153));
                        plateConfirmPanel.setVisible(true);

                        ocrStatusLabel.setText("✓ Plate detected: " + result);
                        ocrStatusLabel.setForeground(new Color(52, 211, 153));

                        // Update snapshot filename with real plate
                        if (capturedImagePath != null) {
                            lblCapturedPath.setText("✓ Snapshot saved");
                            lblCapturedPath.setForeground(new Color(16, 185, 129));
                        }
                    } else {
                        // ── OCR Failed — no plate read ───────────
                        lblOcrResult.setText("Could not read");
                        lblOcrResult.setForeground(new Color(239, 68, 68));
                        plateConfirmPanel.setVisible(true);

                        ocrStatusLabel.setText("✗ No plate detected — try again or type manually");
                        ocrStatusLabel.setForeground(new Color(245, 158, 11));
                    }
                } catch (Exception ex) {
                    ocrStatusLabel.setText("✗ OCR error: " + ex.getMessage());
                    ocrStatusLabel.setForeground(new Color(239, 68, 68));
                }
                revalidate();
                repaint();
            }
        };
        ocrWorker.execute();
    }

    // Officer accepts the OCR reading → fill the plate field
    private void confirmOCRPlate() {
        String detected = lblOcrResult.getText().trim();
        if (!detected.isEmpty() && !detected.equals("Could not read")) {
            txtPlate.setText(detected);
            txtPlate.setForeground(new Color(52, 211, 153));   // green = auto-filled
            plateConfirmPanel.setVisible(false);
            ocrStatusLabel.setText("✓ Plate confirmed: " + detected);
            ocrStatusLabel.setForeground(new Color(52, 211, 153));
            // Auto-lookup the vehicle immediately
            lookupVehicleQuiet(detected);
        }
    }

    // Officer rejects OCR — hide panel, focus plate field for manual entry
    private void editOCRPlate() {
        plateConfirmPanel.setVisible(false);
        txtPlate.setText("");
        txtPlate.setForeground(new Color(226, 232, 240));
        txtPlate.requestFocus();
        ocrStatusLabel.setText("Type plate manually");
        ocrStatusLabel.setForeground(new Color(148, 163, 184));
        revalidate();
    }

    // ─────────────────────────────────────────────────────────────
    // Vehicle lookup
    // ─────────────────────────────────────────────────────────────
    private void lookupVehicle() {
        String plate = txtPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) return;
        Vehicle v = vehicleDAO.getVehicleByPlate(plate);
        if (v != null) {
            JOptionPane.showMessageDialog(this,
                    "✓ Vehicle Found!\n\n" +
                            "Plate:  " + v.getPlateNumber() + "\n" +
                            "Type:   " + v.getVehicleType() + "\n" +
                            "Brand:  " + v.getBrand() + "\n" +
                            "Color:  " + v.getColor() + "\n" +
                            "Owner:  " + (v.getOwnerName() != null ? v.getOwnerName() : "Unknown"),
                    "Vehicle Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Vehicle " + plate + " not found in system.\nRegister it via the Vehicles menu first.",
                    "Not Registered", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Silent lookup after OCR confirm — just updates the status label, no popup
    private void lookupVehicleQuiet(String plate) {
        SwingWorker<Vehicle, Void> w = new SwingWorker<>() {
            protected Vehicle doInBackground() {
                return vehicleDAO.getVehicleByPlate(plate);
            }
            protected void done() {
                try {
                    Vehicle v = get();
                    if (v != null) {
                        ocrStatusLabel.setText("✓ " + plate + "  →  " + v.getOwnerName()
                                + "  (" + v.getBrand() + ", " + v.getColor() + ")");
                        ocrStatusLabel.setForeground(new Color(52, 211, 153));
                    } else {
                        ocrStatusLabel.setText("⚠ " + plate + " not registered in system");
                        ocrStatusLabel.setForeground(new Color(245, 158, 11));
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    // ─────────────────────────────────────────────────────────────
    // Issue fine
    // ─────────────────────────────────────────────────────────────
    private void updateFineAmount() {
        ViolationType vt = (ViolationType) cboViolation.getSelectedItem();
        if (vt != null) lblFineAmount.setText("₹" + String.format("%.2f", vt.getFineAmount()));
    }

    private void issueFine() {
        String plate    = txtPlate.getText().trim().toUpperCase();
        String location = txtLocation.getText().trim();
        ViolationType vt = (ViolationType) cboViolation.getSelectedItem();

        if (plate.isEmpty())    { showError("Enter or scan a license plate number."); return; }
        if (location.isEmpty()) { showError("Enter checkpoint/location."); return; }

        Vehicle vehicle = vehicleDAO.getVehicleByPlate(plate);
        if (vehicle == null) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Vehicle " + plate + " is not registered.\nPlease register the vehicle first via the Vehicles menu.",
                    "Vehicle Not Registered", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Auto-snapshot if camera running and none taken yet
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
                            "Vehicle:   " + plate + "\n" +
                            "Owner:     " + vehicle.getOwnerName() + "\n" +
                            "Violation: " + vt.getName() + "\n" +
                            "Amount:    ₹" + String.format("%.2f", vt.getFineAmount()) + "\n" +
                            "Location:  " + location + "\n" +
                            (capturedImagePath != null ? "Evidence:  " + capturedImagePath : ""),
                    "Fine Issued", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            showError("Failed to issue fine. Check database connection.");
        }
    }

    private void clearForm() {
        txtPlate.setText("");
        txtPlate.setForeground(new Color(226, 232, 240));
        txtLocation.setText("");
        capturedImagePath = null;
        lblCapturedPath.setText("No snapshot taken");
        lblCapturedPath.setForeground(new Color(100, 116, 139));
        plateConfirmPanel.setVisible(false);
        ocrStatusLabel.setText("OCR ready");
        ocrStatusLabel.setForeground(new Color(100, 116, 139));
        cboViolation.setSelectedIndex(0);
        updateFineAmount();
    }

    private void goBack() {
        stopCamera();
        dispose();
        parent.dispose();
        new AdminDashboard().setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────
    // UI Helpers
    // ─────────────────────────────────────────────────────────────
    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(148, 163, 184));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField formField(String tip) {
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
        f.setToolTipText(tip);
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