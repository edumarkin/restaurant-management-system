package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Employee;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    public Employee login(String username, String password) {
        String sql = "SELECT * FROM employee WHERE username = ? AND password = ? AND status = 'ACTIVE'";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Employee> findAll() {
        List<Employee> out = new ArrayList<>();
        String sql = "SELECT * FROM employee ORDER BY employee_id";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public boolean create(Employee e) {
        String sql = "INSERT INTO employee (branch_id, username, password, full_name, email, phone, role, status, date_joined) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (e.getBranchId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, e.getBranchId());
            ps.setString(2, e.getUsername());
            ps.setString(3, e.getPassword());
            ps.setString(4, e.getFullName());
            ps.setString(5, e.getEmail());
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getRole().name());
            ps.setString(8, e.getStatus() == null ? "ACTIVE" : e.getStatus());
            ps.setDate(9, Date.valueOf(e.getDateJoined() == null ? LocalDate.now() : e.getDateJoined()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean update(Employee e) {
        String sql = "UPDATE employee SET branch_id=?, username=?, password=?, full_name=?, email=?, phone=?, role=?, status=? WHERE employee_id=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (e.getBranchId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, e.getBranchId());
            ps.setString(2, e.getUsername());
            ps.setString(3, e.getPassword());
            ps.setString(4, e.getFullName());
            ps.setString(5, e.getEmail());
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getRole().name());
            ps.setString(8, e.getStatus());
            ps.setInt(9, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public boolean delete(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM employee WHERE employee_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("employee_id"));
        int b = rs.getInt("branch_id");
        e.setBranchId(rs.wasNull() ? null : b);
        e.setUsername(rs.getString("username"));
        e.setPassword(rs.getString("password"));
        e.setFullName(rs.getString("full_name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setRole(Employee.Role.valueOf(rs.getString("role")));
        e.setStatus(rs.getString("status"));
        Date d = rs.getDate("date_joined");
        if (d != null) e.setDateJoined(d.toLocalDate());
        return e;
    }
}
