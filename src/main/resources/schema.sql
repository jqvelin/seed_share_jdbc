CREATE SCHEMA IF NOT EXISTS seed_share;

CREATE TYPE seed_share.delivery_method_enum AS ENUM (
    'in_person',
    'mail',
    'courier',
    'pickup_point',
    'other'
);

CREATE TABLE seed_share.plant (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE seed_share.variety (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plant_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    growing_conditions VARCHAR(100),
    rating DECIMAL(3,2),
    UNIQUE (plant_id, name),
    FOREIGN KEY (plant_id) REFERENCES seed_share.plant(id) ON DELETE CASCADE,
    CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5))
);

CREATE TABLE seed_share.issue (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_pest BOOLEAN NOT NULL,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE seed_share.plant_issue (
    plant_id INT NOT NULL,
    issue_id INT NOT NULL,
    treatment TEXT,
    PRIMARY KEY (plant_id, issue_id),
    FOREIGN KEY (plant_id) REFERENCES seed_share.plant(id) ON DELETE CASCADE,
    FOREIGN KEY (issue_id) REFERENCES seed_share.issue(id) ON DELETE RESTRICT
);

CREATE TABLE seed_share.gardener (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    rating NUMERIC(3,2),
    CHECK (username <> ''),
    CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5))
);

CREATE TABLE seed_share.seed (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    variety_id INT NOT NULL,
    gardener_id INT NOT NULL,
    harvest_year INT,
    lineage TEXT,
    packets_count INT NOT NULL DEFAULT 1,
    UNIQUE (variety_id, gardener_id),
    FOREIGN KEY (variety_id) REFERENCES seed_share.variety(id) ON DELETE CASCADE,
    FOREIGN KEY (gardener_id) REFERENCES seed_share.gardener(id) ON DELETE CASCADE,
    CHECK (packets_count > 0),
    CHECK (
        harvest_year IS NULL OR
        (harvest_year >= 1900 AND harvest_year <= EXTRACT(YEAR FROM CURRENT_DATE))
    )
);

CREATE TABLE seed_share.exchange (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transfer_completed BOOLEAN NOT NULL DEFAULT FALSE,
    delivery_method seed_share.delivery_method_enum
);

CREATE TABLE seed_share.exchange_item (
    exchange_id INT NOT NULL,
    seed_id INT NOT NULL,
    packets_count INT NOT NULL,
    PRIMARY KEY (exchange_id, seed_id),
    FOREIGN KEY (exchange_id) REFERENCES seed_share.exchange(id) ON DELETE CASCADE,
    FOREIGN KEY (seed_id) REFERENCES seed_share.seed(id) ON DELETE RESTRICT,
    CHECK (packets_count > 0)
);

CREATE TABLE seed_share.feedback (
    exchange_id INT NOT NULL,
    gardener_id INT NOT NULL,
    comment TEXT,
    photo_url TEXT,
    gardener_rating NUMERIC(3,2),
    plant_rating NUMERIC(3,2),
    PRIMARY KEY (exchange_id, gardener_id),
    FOREIGN KEY (exchange_id) REFERENCES seed_share.exchange(id) ON DELETE RESTRICT,
    FOREIGN KEY (gardener_id) REFERENCES seed_share.gardener(id) ON DELETE RESTRICT,
    CHECK (gardener_rating IS NULL OR (gardener_rating >= 0 AND gardener_rating <= 5)),
    CHECK (plant_rating IS NULL OR (plant_rating >= 0 AND plant_rating <= 5)),
    CHECK (gardener_rating IS NOT NULL OR plant_rating IS NOT NULL),
    CHECK (comment IS NULL OR length(comment) <= 1000)
);
