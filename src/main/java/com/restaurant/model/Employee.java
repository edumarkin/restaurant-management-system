package com.restaurant.model;

import java.time.LocalDate;

public class Employee {

    public enum Role { MANAGER, RECEPTIONIST, WAITER, CHEF, CASHIER }

    private int id;
    private Integer branchId;        // can be null
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String status;
    private LocalDate dateJoined;

    public Employee() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    @Override public String toString() { return fullName + " (" + role + ")"; }
}
