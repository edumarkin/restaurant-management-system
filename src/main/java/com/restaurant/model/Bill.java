package com.restaurant.model;

import java.time.LocalDateTime;

public class Bill {
    private int id;
    private int orderId;
    private double amount;
    private double tax;
    private double tip;
    private boolean paid;
    private LocalDateTime createdAt;

    private int tableNumber;

    public Bill() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getTip() { return tip; }
    public void setTip(double tip) { this.tip = tip; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public double getTotal() { return amount + tax + tip; }
}
