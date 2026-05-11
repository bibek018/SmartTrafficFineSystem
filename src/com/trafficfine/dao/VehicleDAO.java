package com.trafficfine.dao;

import com.trafficfine.model.Vehicle;
import com.trafficfine.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public boolean addVehicle(Vehicle v) {
        String sql = "INSERT INTO vehicles (plate_number, owner_id, vehicle_type, brand, color) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getPlateNumber());
            ps.setInt(2, v.getOwnerId());
            ps.setString(3, v.getVehicleType());
            ps.setString(4, v.getBrand());
            ps.setString(5, v.getColor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVehicle(int id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name AS owner_name FROM vehicles v LEFT JOIN users u ON v.owner_id = u.id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapVehicle(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Vehicle> getVehiclesByOwner(int ownerId) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name AS owner_name FROM vehicles v LEFT JOIN users u ON v.owner_id = u.id WHERE v.owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapVehicle(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Vehicle getVehicleByPlate(String plate) {
        String sql = "SELECT v.*, u.full_name AS owner_name FROM vehicles v LEFT JOIN users u ON v.owner_id = u.id WHERE v.plate_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapVehicle(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Vehicle mapVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getInt("id"));
        v.setPlateNumber(rs.getString("plate_number"));
        v.setOwnerId(rs.getInt("owner_id"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setBrand(rs.getString("brand"));
        v.setColor(rs.getString("color"));
        try { v.setOwnerName(rs.getString("owner_name")); } catch (SQLException ignored) {}
        return v;
    }
}
