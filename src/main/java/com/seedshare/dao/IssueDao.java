package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.Issue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IssueDao {

    public List<Issue> findAll() throws SQLException {
        String sql = "SELECT id, is_pest, name, description FROM seed_share.issue ORDER BY id";
        List<Issue> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Issue> findById(int id) throws SQLException {
        String sql = "SELECT id, is_pest, name, description FROM seed_share.issue WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Issue> findByType(boolean isPest) throws SQLException {
        String sql = "SELECT id, is_pest, name, description FROM seed_share.issue WHERE is_pest = ? ORDER BY name";
        List<Issue> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isPest);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public int insert(Issue issue) throws SQLException {
        String sql = "INSERT INTO seed_share.issue (is_pest, name, description) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, issue.isPest());
            ps.setString(2, issue.getName());
            ps.setString(3, issue.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    issue.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Issue issue) throws SQLException {
        String sql = "UPDATE seed_share.issue SET is_pest = ?, name = ?, description = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, issue.isPest());
            ps.setString(2, issue.getName());
            ps.setString(3, issue.getDescription());
            ps.setInt(4, issue.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seed_share.issue WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Issue mapRow(ResultSet rs) throws SQLException {
        return new Issue(
                rs.getInt("id"),
                rs.getBoolean("is_pest"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}
