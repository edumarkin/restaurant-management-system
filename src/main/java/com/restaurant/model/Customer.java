package com.restaurant.model;

import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime lastVisited;

    public Customer() { }
    public Customer(int id, String fullName, String phone, String email) {
        this.id = id; this.fullName = fullName; this.phone = phone; this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getLastVisited() { return lastVisited; }
    public void setLastVisited(LocalDateTime lastVisited) { this.lastVisited = lastVisited; }

    @Override public String toString() { return fullName + " (" + phone + ")"; }
}
