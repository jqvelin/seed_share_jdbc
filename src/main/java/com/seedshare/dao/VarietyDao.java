package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.Variety;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VarietyDao {

    private static final String BASE_SELECT =
            "SELECT id, plant_id, name, growing_conditions, rating FROM seed_share.variety";

    public List<Variety> findAll() throws SQLException {
        List<Variety> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id")) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Variety> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Variety> findByPlant(int plantId) throws SQLException {
        String sql = BASE_SELECT + " WHERE plant_id = ? ORDER BY name";
        List<Variety> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public int insert(Variety variety) throws SQLException {
        String sql = "INSERT INTO seed_share.variety (plant_id, name, growing_conditions, rating) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, variety.getPlantId());
            ps.setString(2, variety.getName());
            ps.setString(3, variety.getGrowingConditions());
            ps.setBigDecimal(4, variety.getRating());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    variety.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Variety variety) throws SQLException {
        String sql = "UPDATE seed_share.variety SET plant_id = ?, name = ?, growing_conditions = ?, rating = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variety.getPlantId());
            ps.setString(2, variety.getName());
            ps.setString(3, variety.getGrowingConditions());
            ps.setBigDecimal(4, variety.getRating());
            ps.setInt(5, variety.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seed_share.variety WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Variety mapRow(ResultSet rs) throws SQLException {
        return new Variety(
                rs.getInt("id"),
                rs.getInt("plant_id"),
                rs.getString("name"),
                rs.getString("growing_conditions"),
                rs.getBigDecimal("rating")
        );
    }
}
