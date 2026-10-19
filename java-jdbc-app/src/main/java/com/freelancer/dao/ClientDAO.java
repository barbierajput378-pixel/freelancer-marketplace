package com.freelancer.dao;

import com.freelancer.db.DBConnection;
import com.freelancer.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the Clients table. Every statement uses a
 * {@link PreparedStatement} with {@code ?} placeholders (no string
 * concatenation) and try-with-resources to close JDBC objects.
 */
public class ClientDAO {

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT client_id, name, email, company_name FROM Clients ORDER BY client_id";
        List<Client> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Client client) throws SQLException {
        String sql = "INSERT INTO Clients (client_id, name, email, company_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, client.getClientId());
            ps.setString(2, client.getName());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getCompanyName());
            ps.executeUpdate();
        }
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE Clients SET name = ?, email = ?, company_name = ? WHERE client_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getCompanyName());
            ps.setInt(4, client.getClientId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No client found with id " + client.getClientId());
            }
        }
    }

    public void delete(int clientId) throws SQLException {
        String sql = "DELETE FROM Clients WHERE client_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No client found with id " + clientId);
            }
        }
    }

    /** Next free primary key (MAX + 1), used to pre-fill the entry form. */
    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(client_id), 0) + 1 AS next_id FROM Clients";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("client_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("company_name"));
    }
}
