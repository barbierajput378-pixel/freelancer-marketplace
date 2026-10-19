package com.freelancer.dao;

import com.freelancer.db.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Read-only reporting queries for the Dashboard tab. Demonstrates plain
 * {@link PreparedStatement} queries, a {@link CallableStatement} that invokes a
 * stored procedure, and SELECTs that call stored functions.
 */
public class ReportDAO {

    /** (a) Open jobs with a budget of at least 70,000. */
    public QueryResult openHighBudgetJobs() throws SQLException {
        String sql = "SELECT job_id, title, budget_max FROM Jobs "
                + "WHERE status = ? AND budget_max >= ? ORDER BY budget_max DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Open");
            ps.setBigDecimal(2, new java.math.BigDecimal("70000"));
            try (ResultSet rs = ps.executeQuery()) {
                return QueryResult.from(rs);
            }
        }
    }

    /** (b) Freelancers who have submitted at least one offer. */
    public QueryResult freelancersWithOffers() throws SQLException {
        String sql = "SELECT freelancer_id, name, email FROM Freelancers "
                + "WHERE freelancer_id IN (SELECT freelancer_id FROM Offers) ORDER BY freelancer_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return QueryResult.from(rs);
        }
    }

    /** (c) Completed-contracts summary per freelancer. */
    public QueryResult completedContractsSummary() throws SQLException {
        String sql = "SELECT freelancer_id, COUNT(*) AS completed_contracts, "
                + "SUM(final_amount) AS total_earned "
                + "FROM Contracts WHERE status = ? "
                + "GROUP BY freelancer_id ORDER BY total_earned DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Completed");
            try (ResultSet rs = ps.executeQuery()) {
                return QueryResult.from(rs);
            }
        }
    }

    /** (d) Stored procedure GetJobsByStatus(p_status) via CallableStatement. */
    public QueryResult jobsByStatus(String status) throws SQLException {
        String sql = "{CALL GetJobsByStatus(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, status);
            try (ResultSet rs = cs.executeQuery()) {
                return QueryResult.from(rs);
            }
        }
    }

    /** (e1) Stored function CountOffers(job_id) via SELECT. */
    public int countOffers(int jobId) throws SQLException {
        String sql = "SELECT CountOffers(?) AS offer_count";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("offer_count");
                }
                return 0;
            }
        }
    }

    /** (e2) Stored function AverageContractAmount() via SELECT. */
    public java.math.BigDecimal averageContractAmount() throws SQLException {
        String sql = "SELECT AverageContractAmount() AS avg_amount";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("avg_amount");
            }
            return null;
        }
    }

    /** Distinct job ids (helper to populate the CountOffers dropdown). */
    public List<Integer> allJobIds() throws SQLException {
        String sql = "SELECT job_id FROM Jobs ORDER BY job_id";
        List<Integer> ids = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("job_id"));
            }
        }
        return ids;
    }
}
