package com.trafficfine.model;

import java.sql.Timestamp;

public class Fine {
    private int id;
    private int vehicleId;
    private int violationId;
    private int officerId;
    private String capturedImagePath;
    private String plateDetected;
    private double fineAmount;
    private String status; // PENDING, PAID, DISPUTED
    private String location;
    private Timestamp issuedAt;
    private Timestamp paidAt;

    // Joined display fields
    private String plateNumber;
    private String violationName;
    private String officerName;
    private String ownerName;

    public Fine() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public int getOfficerId() { return officerId; }
    public void setOfficerId(int officerId) { this.officerId = officerId; }
    public String getCapturedImagePath() { return capturedImagePath; }
    public void setCapturedImagePath(String capturedImagePath) { this.capturedImagePath = capturedImagePath; }
    public String getPlateDetected() { return plateDetected; }
    public void setPlateDetected(String plateDetected) { this.plateDetected = plateDetected; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Timestamp getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Timestamp issuedAt) { this.issuedAt = issuedAt; }
    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getViolationName() { return violationName; }
    public void setViolationName(String violationName) { this.violationName = violationName; }
    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
