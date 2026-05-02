package com.restaurant.model;

public class MenuSection {
    private int id;
    private int menuId;
    private String title;
    private String description;

    public MenuSection() { }
    public MenuSection(int id, int menuId, String title, String description) {
        this.id = id; this.menuId = menuId; this.title = title; this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override public String toString() { return title; }
}
