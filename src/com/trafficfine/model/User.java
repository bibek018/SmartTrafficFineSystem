package com.trafficfine.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role; // ADMIN or OWNER
    private String fullName;
    private String phone;

    public User() {}

    public User(int id, String username, String role, String fullName, String phone) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
