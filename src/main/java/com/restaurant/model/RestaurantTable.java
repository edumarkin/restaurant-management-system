package com.restaurant.model;

public class RestaurantTable {

    public enum Status { FREE, RESERVED, OCCUPIED, OTHER }

    private int id;
    private int branchId;
    private int tableNumber;
    private int maxCapacity;
    private String locationIdentifier;
    private Status status;

    public RestaurantTable() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getLocationIdentifier() { return locationIdentifier; }
    public void setLocationIdentifier(String locationIdentifier) { this.locationIdentifier = locationIdentifier; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Table " + tableNumber + " (" + maxCapacity + " seats, " + locationIdentifier + ")";
    }
}
