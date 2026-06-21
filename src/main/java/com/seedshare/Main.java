package com.seedshare;

import com.seedshare.db.ConnectionManager;
import com.seedshare.db.SchemaInitializer;
import com.seedshare.service.BusinessQueryService;
import com.seedshare.service.CrudDemoService;
import com.seedshare.service.PlantCrudDemoService;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final CrudDemoService crudDemo = new CrudDemoService();
    private static final BusinessQueryService businessQuery = new BusinessQueryService();

    public static void main(String[] args) {
        System.out.println("=== JDBC Seed Share Demo (Java 21 · PostgreSQL · HikariCP) ===\n");

        try {
            SchemaInitializer.initialize();
            System.out.println("БД готова.\n");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации: " + e.getMessage());
            ConnectionManager.close();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("""
                    [1] CRUD  [2] Запросы  [3] Всё  [4] CRUD растений  [5] Сброс БД  [0] Выход
                    > """);

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> runCrudMenu(scanner);
                    case "2" -> runBusinessMenu(scanner);
                    case "3" -> runAllDemo();
                    case "4" -> runPlantCrudDemo();
                    case "5" -> resetDatabase(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Неверный выбор.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка SQL: " + e.getMessage());
            }
        }

        System.out.println("До свидания!");
        ConnectionManager.close();
    }

    private static void runCrudMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Create  [2] Read  [3] Update  [4] Delete
                    [5] Batch   [6] Транзакция  [7] Всё  [0] Назад
                    > """);

            switch (scanner.nextLine().trim()) {
                case "1" -> crudDemo.demoCreate();
                case "2" -> crudDemo.demoRead();
                case "3" -> crudDemo.demoUpdate();
                case "4" -> crudDemo.demoDelete();
                case "5" -> crudDemo.demoBatchInsert();
                case "6" -> crudDemo.demoTransaction();
                case "7" -> runAllCrud();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void runBusinessMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Топ сортов  [2] Надёжность   [3] Доставка
                    [4] Лечение     [5] Наличие      [6] История
                    [7] Незавершённые [8] Активные   [9] Всё  [0] Назад
                    > """);

            switch (scanner.nextLine().trim()) {
                case "1" -> businessQuery.topVarieties();
                case "2" -> businessQuery.gardenerReliability();
                case "3" -> businessQuery.exchangesByDelivery();
                case "4" -> {
                    System.out.print("ID растения [1]: ");
                    String value = scanner.nextLine().trim();
                    businessQuery.treatmentsForPlant(value.isEmpty() ? 1 : Integer.parseInt(value));
                }
                case "5" -> businessQuery.availableSeeds();
                case "6" -> {
                    System.out.print("ID садовода [1]: ");
                    String value = scanner.nextLine().trim();
                    businessQuery.gardenerExchangeHistory(value.isEmpty() ? 1 : Integer.parseInt(value));
                }
                case "7" -> businessQuery.pendingExchanges();
                case "8" -> businessQuery.topActiveGardeners();
                case "9" -> runAllBusinessQueries();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void runAllCrud() throws SQLException {
        crudDemo.demoCreate();
        crudDemo.demoRead();
        crudDemo.demoUpdate();
        crudDemo.demoDelete();
        crudDemo.demoBatchInsert();
        crudDemo.demoTransaction();
    }

    private static void runAllBusinessQueries() throws SQLException {
        businessQuery.topVarieties();
        businessQuery.gardenerReliability();
        businessQuery.exchangesByDelivery();
        businessQuery.treatmentsForPlant(1);
        businessQuery.availableSeeds();
        businessQuery.gardenerExchangeHistory(1);
        businessQuery.pendingExchanges();
        businessQuery.topActiveGardeners();
    }

    private static void runAllDemo() throws SQLException {
        System.out.println("\n--- CRUD ---");
        runAllCrud();
        System.out.println("\n--- Бизнес-запросы ---");
        runAllBusinessQueries();
        System.out.println("\nГотово.");
    }

    private static void runPlantCrudDemo() {
        PlantCrudDemoService.run();
    }

    private static void resetDatabase(Scanner scanner) throws SQLException {
        System.out.print("Сбросить БД к исходному состоянию? [y/N]: ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (!answer.equals("y") && !answer.equals("yes") && !answer.equals("д") && !answer.equals("да")) {
            System.out.println("Сброс отменён.");
            return;
        }
        SchemaInitializer.resetDatabase();
        System.out.println("БД сброшена к исходному состоянию.\n");
    }
}
