package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    public int createOrder(int tableId, int waiterId) {
        String sql = "INSERT INTO `order` (table_id, waiter_id, created_at, status) VALUES (?, ?, ?, 'RECEIVED')";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, waiterId);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    try (PreparedStatement upd = c.prepareStatement("UPDATE restaurant_table SET status='OCCUPIED' WHERE table_id=?")) {
                        upd.setInt(1, tableId);
                        upd.executeUpdate();
                    }
                    return id;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
    public boolean addItem(int orderId, int itemId, int quantity, Integer seat) {
        String sql = "INSERT INTO order_item (order_id, item_id, quantity, seat_number) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity);
            if (seat == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, seat);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean removeItem(int orderItemId) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM order_item WHERE order_item_id = ?")) {
            ps.setInt(1, orderItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean updateStatus(int orderId, Order.Status status) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE `order` SET status = ? WHERE order_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Order> findAll() {
        return findByStatus(null);
    }
    public List<Order> findActive() {
        List<Order> out = new ArrayList<>();
        String sql = "SELECT o.*, t.table_number, e.full_name AS waiter_name, " +
                     "  COALESCE((SELECT SUM(oi.quantity * mi.price) FROM order_item oi JOIN menu_item mi ON mi.item_id = oi.item_id WHERE oi.order_id = o.order_id),0) AS total " +
                     "FROM `order` o " +
                     "JOIN restaurant_table t ON t.table_id = o.table_id " +
                     "LEFT JOIN employee e ON e.employee_id = o.waiter_id " +
                     "WHERE o.status IN ('RECEIVED','PREPARING') " +
                     "ORDER BY o.created_at DESC";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapOrder(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<Order> findByStatus(Order.Status status) {
        List<Order> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, t.table_number, e.full_name AS waiter_name, " +
            "  COALESCE((SELECT SUM(oi.quantity * mi.price) FROM order_item oi JOIN menu_item mi ON mi.item_id = oi.item_id WHERE oi.order_id = o.order_id),0) AS total " +
            "FROM `order` o " +
            "JOIN restaurant_table t ON t.table_id = o.table_id " +
            "LEFT JOIN employee e ON e.employee_id = o.waiter_id ");
        if (status != null) sql.append("WHERE o.status = ? ");
        sql.append("ORDER BY o.created_at DESC");

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            if (status != null) ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<OrderItem> findItemsForOrder(int orderId) {
        List<OrderItem> out = new ArrayList<>();
        String sql = "SELECT oi.*, mi.title AS item_title, mi.price " +
                     "FROM order_item oi JOIN menu_item mi ON mi.item_id = oi.item_id " +
                     "WHERE oi.order_id = ? ORDER BY oi.order_item_id";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("order_item_id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setItemId(rs.getInt("item_id"));
                    oi.setQuantity(rs.getInt("quantity"));
                    int seat = rs.getInt("seat_number");
                    oi.setSeatNumber(rs.wasNull() ? null : seat);
                    oi.setItemTitle(rs.getString("item_title"));
                    oi.setPrice(rs.getDouble("price"));
                    out.add(oi);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public double totalForOrder(int orderId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity * mi.price), 0) AS total " +
                     "FROM order_item oi JOIN menu_item mi ON mi.item_id = oi.item_id " +
                     "WHERE oi.order_id = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("order_id"));
        o.setTableId(rs.getInt("table_id"));
        int w = rs.getInt("waiter_id");
        o.setWaiterId(rs.wasNull() ? null : w);
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        o.setStatus(Order.Status.valueOf(rs.getString("status")));
        o.setTableNumber(rs.getInt("table_number"));
        o.setWaiterName(rs.getString("waiter_name"));
        o.setTotal(rs.getDouble("total"));
        return o;
    }
}
