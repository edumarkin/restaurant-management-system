package com.restaurant.dao;

import com.restaurant.db.Database;
import com.restaurant.model.Branch;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {
    public List<Branch> findAll() {
        List<Branch> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM branch ORDER BY branch_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Branch(rs.getInt("branch_id"), rs.getString("name"), rs.getString("location")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
}
