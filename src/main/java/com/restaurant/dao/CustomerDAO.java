package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public List<Customer> findAll() {
        List<Customer> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM customer ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    public Customer findByPhone(String phone) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM customer WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    public int create(Customer cust) {
        String sql = "INSERT INTO customer (full_name, phone, email) VALUES (?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cust.getFullName());
            ps.setString(2, cust.getPhone());
            ps.setString(3, cust.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        Timestamp t = rs.getTimestamp("last_visited");
        if (t != null) c.setLastVisited(t.toLocalDateTime());
        return c;
    }
}
