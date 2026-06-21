package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.Gardener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GardenerDao {

    public List<Gardener> findAll() throws SQLException {
        String sql = "SELECT id, username, rating FROM seed_share.gardener ORDER BY id";
        List<Gardener> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Gardener> findById(int id) throws SQLException {
        String sql = "SELECT id, username, rating FROM seed_share.gardener WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Gardener> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, rating FROM seed_share.gardener WHERE username = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Gardener gardener) throws SQLException {
        String sql = "INSERT INTO seed_share.gardener (username, rating) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gardener.getUsername());
            ps.setBigDecimal(2, gardener.getRating());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    gardener.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Gardener gardener) throws SQLException {
        String sql = "UPDATE seed_share.gardener SET username = ?, rating = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gardener.getUsername());
            ps.setBigDecimal(2, gardener.getRating());
            ps.setInt(3, gardener.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seed_share.gardener WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Gardener mapRow(ResultSet rs) throws SQLException {
        return new Gardener(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getBigDecimal("rating")
        );
    }
}
