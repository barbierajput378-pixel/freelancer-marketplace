package com.freelancer.dao;

import com.freelancer.db.DBConnection;
import com.freelancer.model.Offer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the Offers table. */
public class OfferDAO {

    public List<Offer> findAll() throws SQLException {
        String sql = "SELECT offer_id, job_id, freelancer_id, offer_amount, proposed_duration, status "
                + "FROM Offers ORDER BY offer_id";
        List<Offer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Offer offer) throws SQLException {
        String sql = "INSERT INTO Offers (offer_id, job_id, freelancer_id, offer_amount, proposed_duration, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offer.getOfferId());
            ps.setInt(2, offer.getJobId());
            ps.setInt(3, offer.getFreelancerId());
            ps.setBigDecimal(4, offer.getOfferAmount());
            ps.setInt(5, offer.getProposedDuration());
            ps.setString(6, offer.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Offer offer) throws SQLException {
        String sql = "UPDATE Offers SET job_id = ?, freelancer_id = ?, offer_amount = ?, "
                + "proposed_duration = ?, status = ? WHERE offer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offer.getJobId());
            ps.setInt(2, offer.getFreelancerId());
            ps.setBigDecimal(3, offer.getOfferAmount());
            ps.setInt(4, offer.getProposedDuration());
            ps.setString(5, offer.getStatus());
            ps.setInt(6, offer.getOfferId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No offer found with id " + offer.getOfferId());
            }
        }
    }

    public void delete(int offerId) throws SQLException {
        String sql = "DELETE FROM Offers WHERE offer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No offer found with id " + offerId);
            }
        }
    }

    /** Next free primary key (MAX + 1), used to pre-fill the entry form. */
    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(offer_id), 0) + 1 AS next_id FROM Offers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    /**
     * Runs the AwardOffer stored procedure via a {@link CallableStatement}.
     * The procedure (in a DB transaction) selects this offer, rejects the other
     * pending offers on the job, creates the contract and awards the job. Any
     * rule it SIGNALs (offer not Pending, job not Open) surfaces as a SQLException.
     */
    public void awardOffer(int offerId) throws SQLException {
        String sql = "{CALL AwardOffer(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, offerId);
            cs.execute();
        }
    }

    private Offer mapRow(ResultSet rs) throws SQLException {
        return new Offer(
                rs.getInt("offer_id"),
                rs.getInt("job_id"),
                rs.getInt("freelancer_id"),
                rs.getBigDecimal("offer_amount"),
                rs.getInt("proposed_duration"),
                rs.getString("status"));
    }
}
