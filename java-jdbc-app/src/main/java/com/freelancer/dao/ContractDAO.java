package com.freelancer.dao;

import com.freelancer.db.DBConnection;
import com.freelancer.model.Contract;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the Contracts table.
 *
 * <p>Contracts.job_id is UNIQUE, so inserting a second contract for the same
 * job raises a SQL duplicate-key error that is surfaced to the user.
 */
public class ContractDAO {

    public List<Contract> findAll() throws SQLException {
        String sql = "SELECT contract_id, job_id, freelancer_id, final_amount, status "
                + "FROM Contracts ORDER BY contract_id";
        List<Contract> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Contract contract) throws SQLException {
        String sql = "INSERT INTO Contracts (contract_id, job_id, freelancer_id, final_amount, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contract.getContractId());
            ps.setInt(2, contract.getJobId());
            ps.setInt(3, contract.getFreelancerId());
            ps.setBigDecimal(4, contract.getFinalAmount());
            ps.setString(5, contract.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Contract contract) throws SQLException {
        String sql = "UPDATE Contracts SET job_id = ?, freelancer_id = ?, final_amount = ?, "
                + "status = ? WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contract.getJobId());
            ps.setInt(2, contract.getFreelancerId());
            ps.setBigDecimal(3, contract.getFinalAmount());
            ps.setString(4, contract.getStatus());
            ps.setInt(5, contract.getContractId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No contract found with id " + contract.getContractId());
            }
        }
    }

    public void delete(int contractId) throws SQLException {
        String sql = "DELETE FROM Contracts WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No contract found with id " + contractId);
            }
        }
    }

    /** Next free primary key (MAX + 1), used to pre-fill the entry form. */
    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(contract_id), 0) + 1 AS next_id FROM Contracts";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    /**
     * Updates only the status of one contract (used by the Mark Completed /
     * Cancel buttons). The {@code after_update_contracts} trigger then updates
     * the related job.
     */
    public void updateStatus(int contractId, String status) throws SQLException {
        String sql = "UPDATE Contracts SET status = ? WHERE contract_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, contractId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No contract found with id " + contractId);
            }
        }
    }

    private Contract mapRow(ResultSet rs) throws SQLException {
        return new Contract(
                rs.getInt("contract_id"),
                rs.getInt("job_id"),
                rs.getInt("freelancer_id"),
                rs.getBigDecimal("final_amount"),
                rs.getString("status"));
    }
}
