package com.trafficfine.dao;

import com.trafficfine.model.Fine;
import com.trafficfine.model.ViolationType;
import com.trafficfine.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    // ── Violation Types ──────────────────────────────────────────
    public List<ViolationType> getAllViolationTypes() {
        List<ViolationType> list = new ArrayList<>();
        String sql = "SELECT * FROM violation_types";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ViolationType(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("fine_amount"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Fines ─────────────────────────────────────────────────────
    public boolean issueFine(Fine f) {
        String sql = "INSERT INTO fines (vehicle_id, violation_id, officer_id, captured_image_path, plate_detected, fine_amount, location) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, f.getVehicleId());
            ps.setInt(2, f.getViolationId());
            ps.setInt(3, f.getOfficerId());
            ps.setString(4, f.getCapturedImagePath());
            ps.setString(5, f.getPlateDetected());
            ps.setDouble(6, f.getFineAmount());
            ps.setString(7, f.getLocation());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean payFine(int fineId) {
        String sql = "UPDATE fines SET status = 'PAID', paid_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean disputeFine(int fineId) {
        String sql = "UPDATE fines SET status = 'DISPUTED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Fine> getAllFines() {
        return queryFines("SELECT f.*, v.plate_number, vt.name AS violation_name, u.full_name AS officer_name, ow.full_name AS owner_name " +
                "FROM fines f " +
                "JOIN vehicles v ON f.vehicle_id = v.id " +
                "JOIN violation_types vt ON f.violation_id = vt.id " +
                "JOIN users u ON f.officer_id = u.id " +
                "LEFT JOIN users ow ON v.owner_id = ow.id " +
                "ORDER BY f.issued_at DESC", null);
    }

    public List<Fine> getFinesByOwner(int ownerId) {
        return queryFines("SELECT f.*, v.plate_number, vt.name AS violation_name, u.full_name AS officer_name, ow.full_name AS owner_name " +
                "FROM fines f " +
                "JOIN vehicles v ON f.vehicle_id = v.id " +
                "JOIN violation_types vt ON f.violation_id = vt.id " +
                "JOIN users u ON f.officer_id = u.id " +
                "LEFT JOIN users ow ON v.owner_id = ow.id " +
                "WHERE v.owner_id = ? ORDER BY f.issued_at DESC", ownerId);
    }

    public List<Fine> searchFinesByPlate(String plate) {
        return queryFines("SELECT f.*, v.plate_number, vt.name AS violation_name, u.full_name AS officer_name, ow.full_name AS owner_name " +
                "FROM fines f " +
                "JOIN vehicles v ON f.vehicle_id = v.id " +
                "JOIN violation_types vt ON f.violation_id = vt.id " +
                "JOIN users u ON f.officer_id = u.id " +
                "LEFT JOIN users ow ON v.owner_id = ow.id " +
                "WHERE v.plate_number LIKE ? ORDER BY f.issued_at DESC", "%" + plate.toUpperCase() + "%");
    }

    // Stats for dashboard
    public int countByStatus(String status) {
        String sql = status == null
            ? "SELECT COUNT(*) FROM fines"
            : "SELECT COUNT(*) FROM fines WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double totalCollected() {
        String sql = "SELECT COALESCE(SUM(fine_amount),0) FROM fines WHERE status = 'PAID'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Private helpers ───────────────────────────────────────────
    private List<Fine> queryFines(String sql, Object param) {
        List<Fine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                if (param instanceof Integer) ps.setInt(1, (Integer) param);
                else ps.setString(1, param.toString());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFine(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Fine mapFine(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.setId(rs.getInt("id"));
        f.setVehicleId(rs.getInt("vehicle_id"));
        f.setViolationId(rs.getInt("violation_id"));
        f.setOfficerId(rs.getInt("officer_id"));
        f.setCapturedImagePath(rs.getString("captured_image_path"));
        f.setPlateDetected(rs.getString("plate_detected"));
        f.setFineAmount(rs.getDouble("fine_amount"));
        f.setStatus(rs.getString("status"));
        f.setLocation(rs.getString("location"));
        f.setIssuedAt(rs.getTimestamp("issued_at"));
        f.setPaidAt(rs.getTimestamp("paid_at"));
        f.setPlateNumber(rs.getString("plate_number"));
        f.setViolationName(rs.getString("violation_name"));
        f.setOfficerName(rs.getString("officer_name"));
        f.setOwnerName(rs.getString("owner_name"));
        return f;
    }
}
