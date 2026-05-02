package com.restaurant.model;

import java.time.LocalDateTime;

public class Order {

    public enum Status { RECEIVED, PREPARING, COMPLETE, CANCELED, NONE }

    private int id;
    private int tableId;
    private Integer waiterId;
    private LocalDateTime createdAt;
    private Status status;

    // joined for display
    private int tableNumber;
    private String waiterName;
    private double total;

    public Order() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public Integer getWaiterId() { return waiterId; }
    public void setWaiterId(Integer waiterId) { this.waiterId = waiterId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
