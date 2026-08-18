-- Runs automatically the first time the Postgres container initializes
-- its data volume. Creates one schema per service. Hibernate (ddl-auto:
-- update) then builds the tables inside each on service startup.

CREATE SCHEMA IF NOT EXISTS authentication;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS cart;
CREATE SCHEMA IF NOT EXISTS orders;

-- Note: this runs ONLY on a fresh volume. If you already have a pgdata
-- volume, drop it first (docker compose down -v) to re-trigger this.
