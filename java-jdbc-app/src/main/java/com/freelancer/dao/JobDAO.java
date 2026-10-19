package com.freelancer.dao;

import com.freelancer.db.DBConnection;
import com.freelancer.model.Job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the Jobs table.
 *
 * <p>Note: the {@code before_insert_jobs} trigger uppercases the title on
 * insert, so a freshly inserted title will come back uppercased on refresh.
 */
public class JobDAO {

    public List<Job> findAll() throws SQLException {
        String sql = "SELECT job_id, client_id, title, budget_max, status FROM Jobs ORDER BY job_id";
        List<Job> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Job job) throws SQLException {
        String sql = "INSERT INTO Jobs (job_id, client_id, title, budget_max, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, job.getJobId());
            ps.setInt(2, job.getClientId());
            ps.setString(3, job.getTitle());
            ps.setBigDecimal(4, job.getBudgetMax());
            ps.setString(5, job.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Job job) throws SQLException {
        String sql = "UPDATE Jobs SET client_id = ?, title = ?, budget_max = ?, status = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, job.getClientId());
            ps.setString(2, job.getTitle());
            ps.setBigDecimal(3, job.getBudgetMax());
            ps.setString(4, job.getStatus());
            ps.setInt(5, job.getJobId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No job found with id " + job.getJobId());
            }
        }
    }

    public void delete(int jobId) throws SQLException {
        String sql = "DELETE FROM Jobs WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No job found with id " + jobId);
            }
        }
    }

    /** Next free primary key (MAX + 1), used to pre-fill the entry form. */
    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(job_id), 0) + 1 AS next_id FROM Jobs";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        return new Job(
                rs.getInt("job_id"),
                rs.getInt("client_id"),
                rs.getString("title"),
                rs.getBigDecimal("budget_max"),
                rs.getString("status"));
    }
}
