package com.seedshare.service;

import com.seedshare.dao.ExchangeDao;
import com.seedshare.dao.ExchangeItemDao;
import com.seedshare.dao.GardenerDao;
import com.seedshare.dao.PlantDao;
import com.seedshare.dao.PlantIssueDao;
import com.seedshare.dao.SeedDao;
import com.seedshare.dao.VarietyDao;
import com.seedshare.model.Exchange;
import com.seedshare.model.ExchangeItem;
import com.seedshare.model.Gardener;
import com.seedshare.model.Plant;
import com.seedshare.model.PlantIssue;
import com.seedshare.model.Seed;
import com.seedshare.model.Variety;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CrudDemoService {

    private final GardenerDao gardenerDao = new GardenerDao();
    private final PlantDao plantDao = new PlantDao();
    private final VarietyDao varietyDao = new VarietyDao();
    private final SeedDao seedDao = new SeedDao();
    private final ExchangeDao exchangeDao = new ExchangeDao();
    private final ExchangeItemDao exchangeItemDao = new ExchangeItemDao();
    private final PlantIssueDao plantIssueDao = new PlantIssueDao();

    public void demoCreate() throws SQLException {
        System.out.println("=== CREATE — Создание записей ===");

        long suffix = System.currentTimeMillis() % 100000;

        Gardener gardener = new Gardener("Тестовый_Садовод_" + suffix, BigDecimal.valueOf(4.40));
        int gardenerId = gardenerDao.insert(gardener);
        System.out.printf("Создан садовод: id=%d, %s%n", gardenerId, gardener.getUsername());

        Plant plant = new Plant("Тестовое растение " + suffix);
        int plantId = plantDao.insert(plant);
        System.out.printf("Создано растение: id=%d, %s%n", plantId, plant.getName());

        Variety variety = new Variety(plantId, "Тестовый сорт " + suffix, "Солнце, умеренный полив", BigDecimal.valueOf(4.30));
        int varietyId = varietyDao.insert(variety);
        System.out.printf("Создан сорт: id=%d, %s%n", varietyId, variety.getName());

        boolean linked = plantIssueDao.insert(new PlantIssue(plantId, 1, "Промывание листьев и мыльный раствор"));
        System.out.printf("Связь с проблемой создана: %b%n", linked);

        Seed seed = new Seed(varietyId, gardenerId, 2024, "Собран на учебной грядке", 3);
        int seedId = seedDao.insert(seed);
        System.out.printf("Добавлены семена: id=%d, пакетиков=%d%n", seedId, seed.getPacketsCount());

        Exchange exchange = new Exchange(false, "mail");
        int exchangeId = exchangeDao.insert(exchange);
        exchangeItemDao.insert(new ExchangeItem(exchangeId, seedId, 1));
        System.out.printf("Создан обмен: id=%d, доставка=%s%n", exchangeId, exchange.getDeliveryMethod());

        System.out.println();
    }

    public void demoCreateInteractive(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    === CREATE — Что добавить? ===
                    [1] Садовод
                    [2] Растение
                    [3] Сорт
                    [4] Семена
                    [5] Обмен
                    [6] Связь растение-проблема
                    [7] Позиция обмена
                    [0] Назад
                    > """);

            switch (scanner.nextLine().trim()) {
                case "1" -> createGardener(scanner);
                case "2" -> createPlant(scanner);
                case "3" -> createVariety(scanner);
                case "4" -> createSeed(scanner);
                case "5" -> createExchange(scanner);
                case "6" -> createPlantIssue(scanner);
                case "7" -> createExchangeItem(scanner);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    public void demoRead() throws SQLException {
        System.out.println("=== READ — Чтение данных ===");

        System.out.println("Все садоводы:");
        System.out.printf("%-5s %-24s %-8s%n", "ID", "Username", "Рейтинг");
        for (Gardener gardener : gardenerDao.findAll()) {
            System.out.printf("%-5d %-24s %-8s%n",
                    gardener.getId(),
                    truncate(gardener.getUsername(), 23),
                    gardener.getRating());
        }

        System.out.println("\nВсе растения:");
        System.out.printf("%-5s %-24s%n", "ID", "Название");
        for (Plant plant : plantDao.findAll()) {
            System.out.printf("%-5d %-24s%n", plant.getId(), truncate(plant.getName(), 23));
        }

        System.out.println("\nВсе сорта:");
        System.out.printf("%-5s %-8s %-24s %-8s%n", "ID", "Plant", "Название", "Рейтинг");
        for (Variety variety : varietyDao.findAll()) {
            System.out.printf("%-5d %-8d %-24s %-8s%n",
                    variety.getId(),
                    variety.getPlantId(),
                    truncate(variety.getName(), 23),
                    variety.getRating());
        }

        System.out.println("\nПоиск садовода по id=1:");
        gardenerDao.findById(1).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Не найден")
        );

        System.out.println("\nПоиск садовода по username=Елена_Садовод:");
        gardenerDao.findByUsername("Елена_Садовод").ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Не найден")
        );

        System.out.println("\nПоиск связи растение-проблема: plant=1, issue=1:");
        plantIssueDao.findByKey(1, 1).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Не найдено")
        );

        System.out.println();
    }

    public void demoUpdate() throws SQLException {
        System.out.println("=== UPDATE — Обновление данных ===");

        gardenerDao.findById(1).ifPresent(gardener -> {
            BigDecimal oldRating = gardener.getRating();
            gardener.setRating(BigDecimal.valueOf(4.95));
            try {
                boolean ok = gardenerDao.update(gardener);
                System.out.printf("Обновлён рейтинг садовода id=1: %s -> %s (успех=%b)%n",
                        oldRating, gardener.getRating(), ok);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления: " + e.getMessage());
            }
        });

        varietyDao.findById(1).ifPresent(variety -> {
            String oldName = variety.getName();
            String newName = oldName.endsWith(" учебный") ? oldName : oldName + " учебный";
            variety.setName(newName);
            try {
                boolean ok = varietyDao.update(variety);
                System.out.printf("Обновлён сорт id=1: '%s' -> '%s' (успех=%b)%n",
                        oldName, variety.getName(), ok);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления: " + e.getMessage());
            }
        });

        System.out.println();
    }

    public void demoDelete() throws SQLException {
        System.out.println("=== DELETE — Удаление данных ===");

        long suffix = System.currentTimeMillis() % 100000;
        Gardener temp = new Gardener("Удалить_" + suffix, BigDecimal.valueOf(3.50));
        int tempId = gardenerDao.insert(temp);
        System.out.printf("Создан временный садовод id=%d%n", tempId);

        boolean deleted = gardenerDao.delete(tempId);
        System.out.printf("Удалён садовод id=%d (успех=%b)%n", tempId, deleted);

        boolean notFound = gardenerDao.delete(99999);
        System.out.printf("Удаление несуществующего id=99999 (успех=%b)%n", notFound);

        System.out.println();
    }

    public void demoBatchInsert() throws SQLException {
        System.out.println("=== BATCH INSERT — Массовая вставка ===");

        long suffix = System.currentTimeMillis() % 100000;
        Plant plant = new Plant("Batch растение " + suffix);
        int plantId = plantDao.insert(plant);
        System.out.printf("Создано растение: id=%d, %s%n", plantId, plant.getName());

        List<PlantIssue> items = new ArrayList<>();
        items.add(new PlantIssue(plantId, 1, "Мыльный раствор"));
        items.add(new PlantIssue(plantId, 6, "Бордоская жидкость"));
        items.add(new PlantIssue(plantId, 7, "Опрыскивание серой"));

        long start = System.nanoTime();
        int inserted = plantIssueDao.batchInsert(items);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Вставлено %d связей за %d мс%n", inserted, elapsed);

        plantDao.delete(plantId);
        System.out.printf("Растение id=%d удалено, связи удалены каскадно%n", plantId);

        System.out.println();
    }

    public void demoTransaction() throws SQLException {
        System.out.println("=== TRANSACTION — Регистрация обмена ===");
        System.out.println("Попытка создать обмен: seed=1 (1 пакетик) и seed=4 (1 пакетик)");

        Integer exchangeId = null;
        try {
            exchangeId = exchangeItemDao.registerExchange("courier", 1, 1, 4, 1);
            System.out.printf("Обмен создан! id=%d%n", exchangeId);
            System.out.println("Позиции обмена:");
            for (ExchangeItem item : exchangeItemDao.findByExchange(exchangeId)) {
                System.out.printf("seed=%d, packets=%d%n", item.getSeedId(), item.getPacketsCount());
            }

            System.out.println("Повторная попытка с заведомо большим количеством пакетиков...");
            try {
                exchangeItemDao.registerExchange("courier", 1, 999, 4, 999);
            } catch (SQLException e) {
                System.out.printf("Ожидаемая ошибка: %s%n", e.getMessage());
            }
        } finally {
            if (exchangeId != null) {
                exchangeItemDao.deleteExchangeWithRestock(exchangeId);
                System.out.printf("Тестовый обмен id=%d удалён, пакетики восстановлены%n", exchangeId);
            }
        }

        System.out.println();
    }

    public static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private void createGardener(Scanner scanner) throws SQLException {
        System.out.println("=== Новый садовод ===");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        BigDecimal rating = readBigDecimal(scanner, "Рейтинг");
        Gardener gardener = new Gardener(username, rating);
        int id = gardenerDao.insert(gardener);
        System.out.printf("Создан садовод: id=%d, %s%n%n", id, gardener.getUsername());
    }

    private void createPlant(Scanner scanner) throws SQLException {
        System.out.println("=== Новое растение ===");
        System.out.print("Название растения: ");
        String name = scanner.nextLine().trim();
        Plant plant = new Plant(name);
        int id = plantDao.insert(plant);
        System.out.printf("Создано растение: id=%d, %s%n%n", id, plant.getName());
    }

    private void createVariety(Scanner scanner) throws SQLException {
        System.out.println("=== Новый сорт ===");
        int plantId = readInt(scanner, "ID растения");
        System.out.print("Название сорта: ");
        String name = scanner.nextLine().trim();
        System.out.print("Условия выращивания: ");
        String conditions = scanner.nextLine().trim();
        BigDecimal rating = readBigDecimal(scanner, "Рейтинг");
        Variety variety = new Variety(plantId, name, conditions, rating);
        int id = varietyDao.insert(variety);
        System.out.printf("Создан сорт: id=%d, %s%n%n", id, variety.getName());
    }

    private void createSeed(Scanner scanner) throws SQLException {
        System.out.println("=== Новая запись о семенах ===");
        int varietyId = readInt(scanner, "ID сорта");
        int gardenerId = readInt(scanner, "ID садовода");
        Integer harvestYear = readOptionalInt(scanner, "Год сбора");
        System.out.print("Происхождение: ");
        String lineage = scanner.nextLine().trim();
        int packetsCount = readInt(scanner, "Количество пакетиков");
        Seed seed = new Seed(varietyId, gardenerId, harvestYear, lineage, packetsCount);
        int id = seedDao.insert(seed);
        System.out.printf("Добавлены семена: id=%d, пакетиков=%d%n%n", id, seed.getPacketsCount());
    }

    private void createExchange(Scanner scanner) throws SQLException {
        System.out.println("=== Новый обмен ===");
        boolean completed = readBoolean(scanner, "Обмен завершён");
        System.out.print("Способ доставки (in_person/mail/courier/pickup_point/other): ");
        String deliveryMethod = scanner.nextLine().trim();
        Exchange exchange = new Exchange(completed, deliveryMethod);
        int id = exchangeDao.insert(exchange);
        System.out.printf("Создан обмен: id=%d, доставка=%s%n%n", id, exchange.getDeliveryMethod());
    }

    private void createPlantIssue(Scanner scanner) throws SQLException {
        System.out.println("=== Новая связь растение-проблема ===");
        int plantId = readInt(scanner, "ID растения");
        int issueId = readInt(scanner, "ID проблемы");
        System.out.print("Лечение: ");
        String treatment = scanner.nextLine().trim();
        boolean created = plantIssueDao.insert(new PlantIssue(plantId, issueId, treatment));
        System.out.printf("Связь создана: %b%n%n", created);
    }

    private void createExchangeItem(Scanner scanner) throws SQLException {
        System.out.println("=== Новая позиция обмена ===");
        int exchangeId = readInt(scanner, "ID обмена");
        int seedId = readInt(scanner, "ID семян");
        int packetsCount = readInt(scanner, "Количество пакетиков");
        boolean created = exchangeItemDao.insert(new ExchangeItem(exchangeId, seedId, packetsCount));
        System.out.printf("Позиция обмена создана: %b%n%n", created);
    }

    private int readInt(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + ": ");
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private Integer readOptionalInt(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " (Enter если пусто): ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число или оставьте поле пустым.");
            }
        }
    }

    private BigDecimal readBigDecimal(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + ": ");
            String value = scanner.nextLine().trim().replace(',', '.');
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.out.println("Введите число.");
            }
        }
    }

    private boolean readBoolean(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " [y/N]: ");
            String value = scanner.nextLine().trim().toLowerCase();
            if (value.isEmpty() || value.equals("n") || value.equals("no") || value.equals("н") || value.equals("нет")) {
                return false;
            }
            if (value.equals("y") || value.equals("yes") || value.equals("д") || value.equals("да")) {
                return true;
            }
            System.out.println("Введите y/yes/да или n/no/нет.");
        }
    }
}
