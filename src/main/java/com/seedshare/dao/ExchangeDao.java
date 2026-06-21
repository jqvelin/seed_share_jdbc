package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.Exchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeDao {

    public List<Exchange> findAll() throws SQLException {
        String sql = "SELECT id, transfer_completed, delivery_method::text AS delivery_method FROM seed_share.exchange ORDER BY id";
        List<Exchange> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Exchange> findById(int id) throws SQLException {
        String sql = "SELECT id, transfer_completed, delivery_method::text AS delivery_method FROM seed_share.exchange WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Exchange> findPending() throws SQLException {
        String sql = "SELECT id, transfer_completed, delivery_method::text AS delivery_method FROM seed_share.exchange WHERE transfer_completed = false ORDER BY id";
        List<Exchange> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(Exchange exchange) throws SQLException {
        String sql = "INSERT INTO seed_share.exchange (transfer_completed, delivery_method) VALUES (?, ?::seed_share.delivery_method_enum)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, exchange.isTransferCompleted());
            ps.setString(2, exchange.getDeliveryMethod());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    exchange.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Exchange exchange) throws SQLException {
        String sql = "UPDATE seed_share.exchange SET transfer_completed = ?, delivery_method = ?::seed_share.delivery_method_enum WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, exchange.isTransferCompleted());
            ps.setString(2, exchange.getDeliveryMethod());
            ps.setInt(3, exchange.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seed_share.exchange WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Exchange mapRow(ResultSet rs) throws SQLException {
        return new Exchange(
                rs.getInt("id"),
                rs.getBoolean("transfer_completed"),
                rs.getString("delivery_method")
        );
    }
}
