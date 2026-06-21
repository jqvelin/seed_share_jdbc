package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.Seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeedDao {

    private static final String BASE_SELECT =
            "SELECT id, variety_id, gardener_id, harvest_year, lineage, packets_count FROM seed_share.seed";

    public List<Seed> findAll() throws SQLException {
        List<Seed> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id")) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Seed> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Seed> findByGardener(int gardenerId) throws SQLException {
        String sql = BASE_SELECT + " WHERE gardener_id = ? ORDER BY id";
        List<Seed> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gardenerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public List<Seed> findAvailable() throws SQLException {
        String sql = BASE_SELECT + " WHERE packets_count > 0 ORDER BY packets_count DESC, id";
        List<Seed> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(Seed seed) throws SQLException {
        String sql = "INSERT INTO seed_share.seed (variety_id, gardener_id, harvest_year, lineage, packets_count) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, seed.getVarietyId());
            ps.setInt(2, seed.getGardenerId());
            if (seed.getHarvestYear() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, seed.getHarvestYear());
            }
            ps.setString(4, seed.getLineage());
            ps.setInt(5, seed.getPacketsCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    seed.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Seed seed) throws SQLException {
        String sql = "UPDATE seed_share.seed SET variety_id = ?, gardener_id = ?, harvest_year = ?, lineage = ?, packets_count = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seed.getVarietyId());
            ps.setInt(2, seed.getGardenerId());
            if (seed.getHarvestYear() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, seed.getHarvestYear());
            }
            ps.setString(4, seed.getLineage());
            ps.setInt(5, seed.getPacketsCount());
            ps.setInt(6, seed.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seed_share.seed WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean changePacketsCount(int seedId, int delta) throws SQLException {
        String sql = """
                UPDATE seed_share.seed
                SET packets_count = packets_count + ?
                WHERE id = ? AND packets_count + ? >= 0
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, seedId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    private Seed mapRow(ResultSet rs) throws SQLException {
        int harvestYear = rs.getInt("harvest_year");
        Integer year = rs.wasNull() ? null : harvestYear;
        return new Seed(
                rs.getInt("id"),
                rs.getInt("variety_id"),
                rs.getInt("gardener_id"),
                year,
                rs.getString("lineage"),
                rs.getInt("packets_count")
        );
    }
}
