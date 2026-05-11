package com.trafficfine.model;

public class Vehicle {
    private int id;
    private String plateNumber;
    private int ownerId;
    private String ownerName; // joined field
    private String vehicleType;
    private String brand;
    private String color;

    public Vehicle() {}

    public Vehicle(int id, String plateNumber, int ownerId, String vehicleType, String brand, String color) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.ownerId = ownerId;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.color = color;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() { return plateNumber + " - " + brand + " (" + vehicleType + ")"; }
}
