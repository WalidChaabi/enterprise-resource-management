-- Baseline Flyway : création du schéma applicatif
CREATE SCHEMA IF NOT EXISTS erm;

-- Définit le search_path pour les objets suivants
SET search_path TO erm, public;

-- Marqueur simple pour valider la baseline
CREATE TABLE IF NOT EXISTS schema_version_marker (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version_marker (id) VALUES (1)
ON CONFLICT (id) DO NOTHING;

