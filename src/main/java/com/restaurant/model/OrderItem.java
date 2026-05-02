package com.restaurant.model;

public class OrderItem {
    private int id;
    private int orderId;
    private int itemId;
    private int quantity;
    private Integer seatNumber;

    private String itemTitle;
    private double price;

    public OrderItem() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getLineTotal() { return price * quantity; }
}
