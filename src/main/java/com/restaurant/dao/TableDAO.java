package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.RestaurantTable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public List<RestaurantTable> findAll() {
        List<RestaurantTable> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM restaurant_table ORDER BY table_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<RestaurantTable> searchAvailable(int peopleCount, LocalDateTime when) {
        List<RestaurantTable> out = new ArrayList<>();
        String sql =
            "SELECT t.* FROM restaurant_table t " +
            "WHERE t.max_capacity >= ? AND t.status = 'FREE' " +
            "AND t.table_id NOT IN ( " +
            "    SELECT r.table_id FROM reservation r " +
            "    WHERE r.status IN ('CONFIRMED','PENDING','CHECKED_IN') " +
            "    AND ABS(TIMESTAMPDIFF(MINUTE, r.reservation_time, ?)) < 120 " +
            ") " +
            "ORDER BY t.max_capacity, t.table_number";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, peopleCount);
            ps.setTimestamp(2, Timestamp.valueOf(when));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public boolean updateStatus(int tableId, RestaurantTable.Status status) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE restaurant_table SET status = ? WHERE table_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, tableId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean create(RestaurantTable t) {
        String sql = "INSERT INTO restaurant_table (branch_id, table_number, max_capacity, location_identifier, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getBranchId());
            ps.setInt(2, t.getTableNumber());
            ps.setInt(3, t.getMaxCapacity());
            ps.setString(4, t.getLocationIdentifier());
            ps.setString(5, t.getStatus() == null ? "FREE" : t.getStatus().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean update(RestaurantTable t) {
        String sql = "UPDATE restaurant_table SET branch_id=?, table_number=?, max_capacity=?, location_identifier=?, status=? WHERE table_id=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getBranchId());
            ps.setInt(2, t.getTableNumber());
            ps.setInt(3, t.getMaxCapacity());
            ps.setString(4, t.getLocationIdentifier());
            ps.setString(5, t.getStatus().name());
            ps.setInt(6, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean delete(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM restaurant_table WHERE table_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    private RestaurantTable map(ResultSet rs) throws SQLException {
        RestaurantTable t = new RestaurantTable();
        t.setId(rs.getInt("table_id"));
        t.setBranchId(rs.getInt("branch_id"));
        t.setTableNumber(rs.getInt("table_number"));
        t.setMaxCapacity(rs.getInt("max_capacity"));
        t.setLocationIdentifier(rs.getString("location_identifier"));
        t.setStatus(RestaurantTable.Status.valueOf(rs.getString("status")));
        return t;
    }
}
