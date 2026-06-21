package com.seedshare.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private SchemaInitializer() {
    }

    public static void initialize() throws SQLException {
        log.info("Инициализация схемы БД...");
        if (isSchemaCreationRequired()) {
            executeSqlFile("schema.sql");
        } else {
            log.info("Схема уже существует, создание пропущено");
        }
        if (isSeedRequired()) {
            executeSqlFile("dml.sql");
            log.info("Тестовые данные загружены");
        } else {
            log.info("Тестовые данные уже есть, загрузка пропущена");
        }
        log.info("БД готова к работе");
    }

    public static void resetDatabase() throws SQLException {
        log.info("Сброс БД к начальному состоянию...");
        executeStatement("DROP SCHEMA IF EXISTS seed_share CASCADE");
        executeSqlFile("schema.sql");
        executeSqlFile("dml.sql");
        log.info("БД сброшена и заполнена исходными данными");
    }

    private static void executeSqlFile(String fileName) throws SQLException {
        String sql = readSqlFile(fileName);
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("Выполнен SQL-файл: {}", fileName);
        }
    }

    private static String readSqlFile(String fileName) {
        try (InputStream is = SchemaInitializer.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException("SQL-файл не найден: " + fileName);
            }
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения SQL-файла: " + fileName, e);
        }
    }

    private static void executeStatement(String sql) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static boolean isSchemaCreationRequired() throws SQLException {
        String sql = "SELECT to_regclass('seed_share.gardener')";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getString(1) == null;
        }
    }

    private static boolean isSeedRequired() throws SQLException {
        String sql = "SELECT COUNT(*) FROM seed_share.gardener";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }
}
