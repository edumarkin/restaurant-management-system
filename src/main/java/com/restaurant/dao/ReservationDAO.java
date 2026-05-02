package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public List<Reservation> findAll() {
        List<Reservation> out = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS cust_name, t.table_number AS tbl_num " +
                     "FROM reservation r " +
                     "JOIN customer c ON c.customer_id = r.customer_id " +
                     "JOIN restaurant_table t ON t.table_id = r.table_id " +
                     "ORDER BY r.reservation_time DESC";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public List<Reservation> findUpcoming() {
        List<Reservation> out = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS cust_name, t.table_number AS tbl_num " +
                     "FROM reservation r " +
                     "JOIN customer c ON c.customer_id = r.customer_id " +
                     "JOIN restaurant_table t ON t.table_id = r.table_id " +
                     "WHERE r.reservation_time >= NOW() AND r.status IN ('CONFIRMED','PENDING','CHECKED_IN') " +
                     "ORDER BY r.reservation_time";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
    public int create(Reservation r) {
        String sql = "INSERT INTO reservation (customer_id, table_id, reservation_time, people_count, status, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getTableId());
            ps.setTimestamp(3, Timestamp.valueOf(r.getReservationTime()));
            ps.setInt(4, r.getPeopleCount());
            ps.setString(5, r.getStatus() == null ? "CONFIRMED" : r.getStatus().name());
            ps.setString(6, r.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    // mark the table reserved
                    try (PreparedStatement upd = c.prepareStatement("UPDATE restaurant_table SET status='RESERVED' WHERE table_id=?")) {
                        upd.setInt(1, r.getTableId());
                        upd.executeUpdate();
                    }
                    return id;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
    public boolean cancel(int reservationId) {
        try (Connection c = Database.get()) {
            int tableId = -1;
            try (PreparedStatement ps = c.prepareStatement("SELECT table_id FROM reservation WHERE reservation_id = ?")) {
                ps.setInt(1, reservationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) tableId = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE reservation SET status = 'CANCELED' WHERE reservation_id = ?")) {
                ps.setInt(1, reservationId);
                ps.executeUpdate();
            }
            if (tableId > 0) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE restaurant_table SET status = 'FREE' WHERE table_id = ?")) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean checkIn(int reservationId) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE reservation SET status = 'CHECKED_IN' WHERE reservation_id = ?")) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("reservation_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setTableId(rs.getInt("table_id"));
        r.setReservationTime(rs.getTimestamp("reservation_time").toLocalDateTime());
        r.setPeopleCount(rs.getInt("people_count"));
        r.setStatus(Reservation.Status.valueOf(rs.getString("status")));
        r.setNotes(rs.getString("notes"));
        r.setCustomerName(rs.getString("cust_name"));
        r.setTableNumber(rs.getInt("tbl_num"));
        return r;
    }
}
