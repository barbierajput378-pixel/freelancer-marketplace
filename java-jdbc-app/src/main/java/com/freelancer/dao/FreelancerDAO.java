package com.freelancer.dao;

import com.freelancer.db.DBConnection;
import com.freelancer.model.Freelancer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the Freelancers table. */
public class FreelancerDAO {

    public List<Freelancer> findAll() throws SQLException {
        String sql = "SELECT freelancer_id, name, email, avg_rating FROM Freelancers ORDER BY freelancer_id";
        List<Freelancer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Freelancer freelancer) throws SQLException {
        String sql = "INSERT INTO Freelancers (freelancer_id, name, email, avg_rating) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, freelancer.getFreelancerId());
            ps.setString(2, freelancer.getName());
            ps.setString(3, freelancer.getEmail());
            ps.setBigDecimal(4, freelancer.getAvgRating());
            ps.executeUpdate();
        }
    }

    public void update(Freelancer freelancer) throws SQLException {
        String sql = "UPDATE Freelancers SET name = ?, email = ?, avg_rating = ? WHERE freelancer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, freelancer.getName());
            ps.setString(2, freelancer.getEmail());
            ps.setBigDecimal(3, freelancer.getAvgRating());
            ps.setInt(4, freelancer.getFreelancerId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No freelancer found with id " + freelancer.getFreelancerId());
            }
        }
    }

    public void delete(int freelancerId) throws SQLException {
        String sql = "DELETE FROM Freelancers WHERE freelancer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, freelancerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No freelancer found with id " + freelancerId);
            }
        }
    }

    /** Next free primary key (MAX + 1), used to pre-fill the entry form. */
    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(freelancer_id), 0) + 1 AS next_id FROM Freelancers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    private Freelancer mapRow(ResultSet rs) throws SQLException {
        return new Freelancer(
                rs.getInt("freelancer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getBigDecimal("avg_rating"));
    }
}
