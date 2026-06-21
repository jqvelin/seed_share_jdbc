package com.seedshare.dao;

import com.seedshare.db.ConnectionManager;
import com.seedshare.model.ExchangeItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeItemDao {

    public List<ExchangeItem> findAll() throws SQLException {
        String sql = "SELECT exchange_id, seed_id, packets_count FROM seed_share.exchange_item ORDER BY exchange_id, seed_id";
        List<ExchangeItem> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<ExchangeItem> findByKey(int exchangeId, int seedId) throws SQLException {
        String sql = "SELECT exchange_id, seed_id, packets_count FROM seed_share.exchange_item WHERE exchange_id = ? AND seed_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeId);
            ps.setInt(2, seedId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<ExchangeItem> findByExchange(int exchangeId) throws SQLException {
        String sql = "SELECT exchange_id, seed_id, packets_count FROM seed_share.exchange_item WHERE exchange_id = ? ORDER BY seed_id";
        List<ExchangeItem> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public boolean insert(ExchangeItem exchangeItem) throws SQLException {
        String sql = "INSERT INTO seed_share.exchange_item (exchange_id, seed_id, packets_count) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeItem.getExchangeId());
            ps.setInt(2, exchangeItem.getSeedId());
            ps.setInt(3, exchangeItem.getPacketsCount());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(ExchangeItem exchangeItem) throws SQLException {
        String sql = "UPDATE seed_share.exchange_item SET packets_count = ? WHERE exchange_id = ? AND seed_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeItem.getPacketsCount());
            ps.setInt(2, exchangeItem.getExchangeId());
            ps.setInt(3, exchangeItem.getSeedId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int exchangeId, int seedId) throws SQLException {
        String sql = "DELETE FROM seed_share.exchange_item WHERE exchange_id = ? AND seed_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exchangeId);
            ps.setInt(2, seedId);
            return ps.executeUpdate() > 0;
        }
    }

    public int registerExchange(String deliveryMethod, int firstSeedId, int firstPackets, int secondSeedId, int secondPackets) throws SQLException {
        String exchangeSql = """
                INSERT INTO seed_share.exchange (transfer_completed, delivery_method)
                VALUES (false, ?::seed_share.delivery_method_enum)
                """;
        String itemSql = "INSERT INTO seed_share.exchange_item (exchange_id, seed_id, packets_count) VALUES (?, ?, ?)";
        String selectPacketsSql = "SELECT packets_count FROM seed_share.seed WHERE id = ?";
        String updatePacketsSql = "UPDATE seed_share.seed SET packets_count = packets_count - ? WHERE id = ?";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int firstAvailable = getPackets(conn, selectPacketsSql, firstSeedId);
                int secondAvailable = getPackets(conn, selectPacketsSql, secondSeedId);

                if (firstAvailable < firstPackets) {
                    throw new SQLException("Недостаточно пакетиков у семян id=" + firstSeedId);
                }
                if (secondAvailable < secondPackets) {
                    throw new SQLException("Недостаточно пакетиков у семян id=" + secondSeedId);
                }

                int exchangeId;
                try (PreparedStatement ps = conn.prepareStatement(exchangeSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, deliveryMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Не удалось получить id обмена");
                        }
                        exchangeId = keys.getInt(1);
                    }
                }

                try (PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
                    itemPs.setInt(1, exchangeId);
                    itemPs.setInt(2, firstSeedId);
                    itemPs.setInt(3, firstPackets);
                    itemPs.executeUpdate();

                    itemPs.setInt(1, exchangeId);
                    itemPs.setInt(2, secondSeedId);
                    itemPs.setInt(3, secondPackets);
                    itemPs.executeUpdate();
                }

                try (PreparedStatement updatePs = conn.prepareStatement(updatePacketsSql)) {
                    updatePs.setInt(1, firstPackets);
                    updatePs.setInt(2, firstSeedId);
                    updatePs.executeUpdate();

                    updatePs.setInt(1, secondPackets);
                    updatePs.setInt(2, secondSeedId);
                    updatePs.executeUpdate();
                }

                conn.commit();
                return exchangeId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteExchangeWithRestock(int exchangeId) throws SQLException {
        String loadSql = "SELECT seed_id, packets_count FROM seed_share.exchange_item WHERE exchange_id = ?";
        String restoreSql = "UPDATE seed_share.seed SET packets_count = packets_count + ? WHERE id = ?";
        String deleteSql = "DELETE FROM seed_share.exchange WHERE id = ?";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<ExchangeItem> items = new ArrayList<>();
                try (PreparedStatement loadPs = conn.prepareStatement(loadSql)) {
                    loadPs.setInt(1, exchangeId);
                    try (ResultSet rs = loadPs.executeQuery()) {
                        while (rs.next()) {
                            items.add(new ExchangeItem(exchangeId, rs.getInt("seed_id"), rs.getInt("packets_count")));
                        }
                    }
                }

                try (PreparedStatement restorePs = conn.prepareStatement(restoreSql)) {
                    for (ExchangeItem item : items) {
                        restorePs.setInt(1, item.getPacketsCount());
                        restorePs.setInt(2, item.getSeedId());
                        restorePs.addBatch();
                    }
                    restorePs.executeBatch();
                }

                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                    deletePs.setInt(1, exchangeId);
                    deletePs.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int getPackets(Connection conn, String sql, int seedId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Семена id=" + seedId + " не найдены");
                }
                return rs.getInt("packets_count");
            }
        }
    }

    private ExchangeItem mapRow(ResultSet rs) throws SQLException {
        return new ExchangeItem(
                rs.getInt("exchange_id"),
                rs.getInt("seed_id"),
                rs.getInt("packets_count")
        );
    }
}
