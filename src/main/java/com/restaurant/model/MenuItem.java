package com.restaurant.model;

public class MenuItem {
    private int id;
    private int sectionId;
    private String title;
    private String description;
    private double price;
    private String sectionTitle; // joined for display

    public MenuItem() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    @Override public String toString() { return title + " - $" + String.format("%.2f", price); }
}
