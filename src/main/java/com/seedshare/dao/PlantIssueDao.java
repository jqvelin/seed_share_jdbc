package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.PlantIssue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class PlantIssueDao {

    public List<PlantIssue> findAll() throws SQLException {
        String sql = "SELECT plant_id, issue_id, treatment FROM seed_share.plant_issue ORDER BY plant_id, issue_id";
        List<PlantIssue> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<PlantIssue> findByKey(int plantId, int issueId) throws SQLException {
        String sql = "SELECT plant_id, issue_id, treatment FROM seed_share.plant_issue WHERE plant_id = ? AND issue_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setInt(2, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<PlantIssue> findByPlant(int plantId) throws SQLException {
        String sql = "SELECT plant_id, issue_id, treatment FROM seed_share.plant_issue WHERE plant_id = ? ORDER BY issue_id";
        List<PlantIssue> result = new ArrayList<>();
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

    public boolean insert(PlantIssue plantIssue) throws SQLException {
        String sql = """
                INSERT INTO seed_share.plant_issue (plant_id, issue_id, treatment)
                VALUES (?, ?, ?)
                ON CONFLICT (plant_id, issue_id) DO NOTHING
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantIssue.getPlantId());
            ps.setInt(2, plantIssue.getIssueId());
            ps.setString(3, plantIssue.getTreatment());
            return ps.executeUpdate() > 0;
        }
    }

    public int batchInsert(List<PlantIssue> plantIssues) throws SQLException {
        String sql = """
                INSERT INTO seed_share.plant_issue (plant_id, issue_id, treatment)
                VALUES (?, ?, ?)
                ON CONFLICT (plant_id, issue_id) DO NOTHING
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (PlantIssue plantIssue : plantIssues) {
                    ps.setInt(1, plantIssue.getPlantId());
                    ps.setInt(2, plantIssue.getIssueId());
                    ps.setString(3, plantIssue.getTreatment());
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                conn.commit();
                int inserted = 0;
                for (int count : counts) {
                    if (count > 0) {
                        inserted++;
                    }
                }
                return inserted;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean update(PlantIssue plantIssue) throws SQLException {
        String sql = "UPDATE seed_share.plant_issue SET treatment = ? WHERE plant_id = ? AND issue_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plantIssue.getTreatment());
            ps.setInt(2, plantIssue.getPlantId());
            ps.setInt(3, plantIssue.getIssueId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int plantId, int issueId) throws SQLException {
        String sql = "DELETE FROM seed_share.plant_issue WHERE plant_id = ? AND issue_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            ps.setInt(2, issueId);
            return ps.executeUpdate() > 0;
        }
    }

    private PlantIssue mapRow(ResultSet rs) throws SQLException {
        return new PlantIssue(
                rs.getInt("plant_id"),
                rs.getInt("issue_id"),
                rs.getString("treatment")
        );
    }
}
