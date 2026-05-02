package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.MenuItem;
import com.restaurant.model.MenuSection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    public List<MenuSection> findSections() {
        List<MenuSection> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM menu_section ORDER BY section_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new MenuSection(
                    rs.getInt("section_id"),
                    rs.getInt("menu_id"),
                    rs.getString("title"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<MenuItem> findAllItems() {
        List<MenuItem> out = new ArrayList<>();
        String sql = "SELECT mi.*, ms.title AS section_title FROM menu_item mi " +
                     "JOIN menu_section ms ON ms.section_id = mi.section_id " +
                     "ORDER BY ms.section_id, mi.item_id";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<MenuItem> findItemsBySection(int sectionId) {
        List<MenuItem> out = new ArrayList<>();
        String sql = "SELECT mi.*, ms.title AS section_title FROM menu_item mi " +
                     "JOIN menu_section ms ON ms.section_id = mi.section_id " +
                     "WHERE mi.section_id = ? ORDER BY mi.item_id";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public boolean createItem(MenuItem mi) {
        String sql = "INSERT INTO menu_item (section_id, title, description, price) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mi.getSectionId());
            ps.setString(2, mi.getTitle());
            ps.setString(3, mi.getDescription());
            ps.setDouble(4, mi.getPrice());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateItem(MenuItem mi) {
        String sql = "UPDATE menu_item SET section_id=?, title=?, description=?, price=? WHERE item_id=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mi.getSectionId());
            ps.setString(2, mi.getTitle());
            ps.setString(3, mi.getDescription());
            ps.setDouble(4, mi.getPrice());
            ps.setInt(5, mi.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteItem(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM menu_item WHERE item_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    private MenuItem map(ResultSet rs) throws SQLException {
        MenuItem mi = new MenuItem();
        mi.setId(rs.getInt("item_id"));
        mi.setSectionId(rs.getInt("section_id"));
        mi.setTitle(rs.getString("title"));
        mi.setDescription(rs.getString("description"));
        mi.setPrice(rs.getDouble("price"));
        mi.setSectionTitle(rs.getString("section_title"));
        return mi;
    }
}
