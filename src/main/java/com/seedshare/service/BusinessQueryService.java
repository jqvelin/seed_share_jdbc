package com.seedshare.service;

import com.seedshare.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BusinessQueryService {

    public void topVarieties() throws SQLException {
        System.out.println("=== Топ сортов по рейтингу ===");
        String sql = """
                SELECT p.name AS plant, v.name AS variety, v.rating
                FROM seed_share.variety v
                JOIN seed_share.plant p ON v.plant_id = p.id
                WHERE v.rating IS NOT NULL
                ORDER BY v.rating DESC, v.name
                LIMIT 5
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-4s %-18s %-24s %-8s%n", "#", "Растение", "Сорт", "Рейтинг");
            int rank = 1;
            while (rs.next()) {
                System.out.printf("%-4s %-18s %-24s %-8s%n",
                        "#" + rank,
                        truncate(rs.getString("plant"), 17),
                        truncate(rs.getString("variety"), 23),
                        rs.getBigDecimal("rating"));
                rank++;
            }
        }
        System.out.println();
    }

    public void gardenerReliability() throws SQLException {
        System.out.println("=== Надёжность садоводов ===");
        String sql = """
                SELECT
                    g.username,
                    COUNT(DISTINCT e.id) AS exchanges,
                    ROUND(AVG(f.gardener_rating), 2) AS avg_rating
                FROM seed_share.gardener g
                JOIN seed_share.seed s ON s.gardener_id = g.id
                JOIN seed_share.exchange_item ei ON ei.seed_id = s.id
                JOIN seed_share.exchange e ON e.id = ei.exchange_id
                JOIN seed_share.feedback f ON f.exchange_id = e.id
                WHERE f.gardener_rating IS NOT NULL
                GROUP BY g.id, g.username
                ORDER BY avg_rating DESC, exchanges DESC, g.username
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-10s%n", "Садовод", "Обменов", "Средняя");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-10s%n",
                        truncate(rs.getString("username"), 23),
                        rs.getInt("exchanges"),
                        rs.getBigDecimal("avg_rating"));
            }
        }
        System.out.println();
    }

    public void exchangesByDelivery() throws SQLException {
        System.out.println("=== Обмены по способу доставки ===");
        String sql = """
                SELECT delivery_method::text AS delivery_method, COUNT(*) AS total
                FROM seed_share.exchange
                GROUP BY delivery_method
                ORDER BY total DESC, delivery_method
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-16s %-8s%n", "Доставка", "Обменов");
            while (rs.next()) {
                System.out.printf("%-16s %-8d%n",
                        rs.getString("delivery_method"),
                        rs.getInt("total"));
            }
        }
        System.out.println();
    }

    public void treatmentsForPlant(int plantId) throws SQLException {
        System.out.println("=== Рекомендации по лечению для растения #" + plantId + " ===");
        String sql = """
                SELECT
                    p.name AS plant,
                    i.name AS issue,
                    CASE WHEN i.is_pest THEN 'Вредитель' ELSE 'Болезнь' END AS type,
                    pi.treatment
                FROM seed_share.plant_issue pi
                JOIN seed_share.plant p ON p.id = pi.plant_id
                JOIN seed_share.issue i ON i.id = pi.issue_id
                WHERE p.id = ?
                ORDER BY i.name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, plantId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        System.out.println("Растение: " + rs.getString("plant"));
                        System.out.printf("%-24s %-12s %-50s%n", "Проблема", "Тип", "Лечение");
                        found = true;
                    }
                    System.out.printf("%-24s %-12s %-50s%n",
                            truncate(rs.getString("issue"), 23),
                            rs.getString("type"),
                            truncate(rs.getString("treatment"), 49));
                }
                if (!found) {
                    System.out.println("Нет данных");
                }
            }
        }
        System.out.println();
    }

    public void availableSeeds() throws SQLException {
        System.out.println("=== Семена в наличии ===");
        String sql = """
                SELECT
                    s.id,
                    p.name AS plant,
                    v.name AS variety,
                    g.username,
                    s.harvest_year,
                    s.packets_count
                FROM seed_share.seed s
                JOIN seed_share.variety v ON v.id = s.variety_id
                JOIN seed_share.plant p ON p.id = v.plant_id
                JOIN seed_share.gardener g ON g.id = s.gardener_id
                WHERE s.packets_count > 0
                ORDER BY s.packets_count DESC, p.name, v.name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-5s %-14s %-24s %-24s %-8s %-8s%n", "ID", "Растение", "Сорт", "Садовод", "Год", "Пакеты");
            while (rs.next()) {
                System.out.printf("%-5d %-14s %-24s %-24s %-8d %-8d%n",
                        rs.getInt("id"),
                        truncate(rs.getString("plant"), 13),
                        truncate(rs.getString("variety"), 23),
                        truncate(rs.getString("username"), 23),
                        rs.getInt("harvest_year"),
                        rs.getInt("packets_count"));
            }
        }
        System.out.println();
    }

    public void gardenerExchangeHistory(int gardenerId) throws SQLException {
        System.out.println("=== История обменов садовода #" + gardenerId + " ===");
        String sql = """
                SELECT
                    g.username,
                    e.id AS exchange_id,
                    e.delivery_method::text AS delivery_method,
                    e.transfer_completed,
                    p.name AS plant,
                    v.name AS variety,
                    ei.packets_count
                FROM seed_share.gardener g
                JOIN seed_share.seed s ON s.gardener_id = g.id
                JOIN seed_share.exchange_item ei ON ei.seed_id = s.id
                JOIN seed_share.exchange e ON e.id = ei.exchange_id
                JOIN seed_share.variety v ON v.id = s.variety_id
                JOIN seed_share.plant p ON p.id = v.plant_id
                WHERE g.id = ?
                ORDER BY e.id, v.name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gardenerId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        System.out.println("Садовод: " + rs.getString("username"));
                        System.out.printf("%-8s %-14s %-12s %-14s %-24s %-8s%n",
                                "Обмен", "Доставка", "Статус", "Растение", "Сорт", "Пакеты");
                        found = true;
                    }
                    System.out.printf("%-8d %-14s %-12s %-14s %-24s %-8d%n",
                            rs.getInt("exchange_id"),
                            rs.getString("delivery_method"),
                            rs.getBoolean("transfer_completed") ? "завершён" : "в пути",
                            truncate(rs.getString("plant"), 13),
                            truncate(rs.getString("variety"), 23),
                            rs.getInt("packets_count"));
                }
                if (!found) {
                    System.out.println("Нет данных");
                }
            }
        }
        System.out.println();
    }

    public void pendingExchanges() throws SQLException {
        System.out.println("=== Незавершённые обмены ===");
        String sql = """
                SELECT
                    e.id,
                    e.delivery_method::text AS delivery_method,
                    COUNT(ei.seed_id) AS items
                FROM seed_share.exchange e
                LEFT JOIN seed_share.exchange_item ei ON ei.exchange_id = e.id
                WHERE e.transfer_completed = false
                GROUP BY e.id, e.delivery_method
                ORDER BY e.id
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-8s %-16s %-8s%n", "Обмен", "Доставка", "Позиций");
            while (rs.next()) {
                System.out.printf("%-8d %-16s %-8d%n",
                        rs.getInt("id"),
                        rs.getString("delivery_method"),
                        rs.getInt("items"));
            }
        }
        System.out.println();
    }

    public void topActiveGardeners() throws SQLException {
        System.out.println("=== Самые активные садоводы ===");
        String sql = """
                SELECT
                    g.username,
                    COUNT(DISTINCT e.id) AS exchanges,
                    SUM(ei.packets_count) AS packets_sent
                FROM seed_share.gardener g
                JOIN seed_share.seed s ON s.gardener_id = g.id
                JOIN seed_share.exchange_item ei ON ei.seed_id = s.id
                JOIN seed_share.exchange e ON e.id = ei.exchange_id
                GROUP BY g.id, g.username
                HAVING COUNT(DISTINCT e.id) >= 2
                ORDER BY exchanges DESC, packets_sent DESC, g.username
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-12s%n", "Садовод", "Обменов", "Пакетиков");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-12d%n",
                        truncate(rs.getString("username"), 23),
                        rs.getInt("exchanges"),
                        rs.getInt("packets_sent"));
            }
        }
        System.out.println();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
