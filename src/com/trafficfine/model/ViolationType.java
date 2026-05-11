package com.trafficfine.model;

public class ViolationType {
    private int id;
    private String name;
    private double fineAmount;
    private String description;

    public ViolationType() {}

    public ViolationType(int id, String name, double fineAmount, String description) {
        this.id = id;
        this.name = name;
        this.fineAmount = fineAmount;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return name + " (₹" + fineAmount + ")"; }
}
