# JDBC Seed Share Demo

Консольное приложение на Java 21, JDBC, PostgreSQL и HikariCP для управления базой данных проекта «Обмен семенами».

## Требования

- Java 21 JDK
- Maven 3.9+
- PostgreSQL с базой `seed_share`

Параметры подключения по умолчанию:

```properties
db.url=jdbc:postgresql://localhost:5432/seed_share
db.username=postgres
db.password=postgres
```

## Как запустить

1. Убедитесь, что PostgreSQL запущен и база `seed_share` доступна.
2. Проверьте настройки в `src/main/resources/application.properties`.
3. Выполните команду:

```bash
mvn clean compile exec:java
```

При старте приложение создаёт схему из `schema.sql`. Если таблица `seed_share.gardener` пустая, оно дополнительно загружает тестовые данные из `dml.sql`.

## Структура проекта

```text
JDBC/
├── pom.xml
├── DDL.sql
├── DML.sql
├── README.md
└── src/main/
    ├── java/com/seedshare/
    │   ├── Main.java
    │   ├── db/
    │   ├── dao/
    │   ├── model/
    │   └── service/
    └── resources/
        ├── application.properties
        ├── dml.sql
        ├── logback.xml
        └── schema.sql
```

## Схема базы данных

Приложение работает со схемой `seed_share`.

| Таблица | Назначение |
|---------|------------|
| `plant` | Растения |
| `variety` | Сорта растений |
| `issue` | Вредители и болезни |
| `plant_issue` | Связь растений и проблем |
| `gardener` | Садоводы |
| `seed` | Запасы семян |
| `exchange` | Факты обменов |
| `exchange_item` | Позиции обменов |
| `feedback` | Отзывы о полученных семенах |

Также используется ENUM `delivery_method_enum`.

## Что показывает приложение

### CRUD-операции

- создание садовода, растения, сорта, записи о семенах и обмена;
- чтение списков и поиск по `id`, `username` и составному ключу;
- обновление рейтинга садовода и названия сорта;
- удаление временных записей;
- batch insert для связей `plant_issue`;
- транзакция при регистрации обмена.

### Бизнес-запросы

1. Топ сортов по рейтингу.
2. Надёжность садоводов по отзывам.
3. Статистика обменов по способу доставки.
4. Рекомендации по лечению для выбранного растения.
5. Семена, доступные для обмена.
6. История обменов конкретного садовода.
7. Незавершённые обмены.
8. Самые активные садоводы.

## JDBC-техники

| Техника | Где используется |
|---------|------------------|
| `PreparedStatement` | Все DAO и параметризованные бизнес-запросы |
| `Statement.RETURN_GENERATED_KEYS` | Создание садоводов, растений, сортов, семян и обменов |
| Batch insert | `PlantIssueDao.batchInsert()` |
| Транзакции | `ExchangeItemDao.registerExchange()` и `deleteExchangeWithRestock()` |
| Составной ключ | `PlantIssueDao`, `ExchangeItemDao` |
| PostgreSQL ENUM | `ExchangeDao`, `BusinessQueryService` |
| HikariCP | `ConnectionManager` |
| Text blocks | `BusinessQueryService`, `ExchangeItemDao`, `PlantIssueDao` |

## Меню приложения

- `CRUD` — демонстрация базовых операций.
- `Запросы` — запуск бизнес-запросов.
- `Всё` — полный прогон всех демо-сценариев.
- `CRUD растений` — отдельное мини-демо для таблицы `plant`.
- `Сброс БД` — полное восстановление исходного состояния из `schema.sql` и `dml.sql`.
