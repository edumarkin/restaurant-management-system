package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Bill;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    public Bill createOrGet(int orderId) {
        Bill existing = findByOrderId(orderId);
        if (existing != null) return existing;

        OrderDAO odao = new OrderDAO();
        double subtotal = odao.totalForOrder(orderId);
        double tax = round(subtotal * 0.10);   // 10% tax (demo)

        String sql = "INSERT INTO bill (order_id, amount, tax, tip, is_paid, created_at) VALUES (?, ?, ?, 0, FALSE, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setDouble(2, subtotal);
            ps.setDouble(3, tax);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return findById(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Bill findById(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT b.*, t.table_number FROM bill b JOIN `order` o ON o.order_id = b.order_id JOIN restaurant_table t ON t.table_id = o.table_id WHERE b.bill_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Bill findByOrderId(int orderId) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT b.*, t.table_number FROM bill b JOIN `order` o ON o.order_id = b.order_id JOIN restaurant_table t ON t.table_id = o.table_id WHERE b.order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Bill> findUnpaid() {
        List<Bill> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT b.*, t.table_number FROM bill b JOIN `order` o ON o.order_id = b.order_id JOIN restaurant_table t ON t.table_id = o.table_id WHERE b.is_paid = FALSE ORDER BY b.bill_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    public boolean updateTip(int billId, double tip) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE bill SET tip = ? WHERE bill_id = ?")) {
            ps.setDouble(1, tip);
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean pay(int billId, String method) {
        try (Connection c = Database.get()) {
            Bill b = findById(billId);
            if (b == null) return false;

            try (PreparedStatement ps = c.prepareStatement("UPDATE bill SET is_paid = TRUE WHERE bill_id = ?")) {
                ps.setInt(1, billId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO payment (bill_id, amount, method, paid_at) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, billId);
                ps.setDouble(2, b.getTotal());
                ps.setString(3, method);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
            // Complete the order and free the table
            try (PreparedStatement ps = c.prepareStatement("UPDATE `order` SET status = 'COMPLETE' WHERE order_id = ?")) {
                ps.setInt(1, b.getOrderId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                 "UPDATE restaurant_table SET status = 'FREE' WHERE table_id = (SELECT table_id FROM `order` WHERE order_id = ?)")) {
                ps.setInt(1, b.getOrderId());
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Bill map(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("bill_id"));
        b.setOrderId(rs.getInt("order_id"));
        b.setAmount(rs.getDouble("amount"));
        b.setTax(rs.getDouble("tax"));
        b.setTip(rs.getDouble("tip"));
        b.setPaid(rs.getBoolean("is_paid"));
        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        b.setTableNumber(rs.getInt("table_number"));
        return b;
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
