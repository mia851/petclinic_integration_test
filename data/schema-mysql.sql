-- ===============================================
-- PETCLINIC DATABASE - SCHEMA CREATION SCRIPT
-- ===============================================
-- Author: PetClinic Team
-- Description: Database schema for Pet Clinic Management System
-- Version: 2.0
-- ===============================================

-- ===============================================
-- DATABASE SETUP
-- ===============================================

DROP DATABASE IF EXISTS PETCLINIC_DB;

CREATE DATABASE IF NOT EXISTS PETCLINIC_DB;

ALTER DATABASE PETCLINIC_DB
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

USE PETCLINIC_DB;

-- ===============================================
-- TABLE: vets (Veterinarios)
-- Description: Stores veterinarian information
-- ===============================================
CREATE TABLE IF NOT EXISTS vets (
  id              INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  first_name      VARCHAR(30),
  last_name       VARCHAR(30),
  email           VARCHAR(100),
  phone           VARCHAR(20),
  active          BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id),
  INDEX idx_vets_last_name (last_name)
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: specialties (Especialidades)
-- Description: Stores veterinary specialties
-- ===============================================
CREATE TABLE IF NOT EXISTS specialties (
  id            INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(80),
  office        VARCHAR(40),
  h_open        INT(4),
  h_close       INT(4),
  PRIMARY KEY (id),
  INDEX idx_specialties_name (name)
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: vet_specialties (Relación Veterinario-Especialidad)
-- Description: Many-to-many relationship between vets and specialties
-- ===============================================
CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id              INT(4) UNSIGNED NOT NULL,
  specialty_id        INT(4) UNSIGNED NOT NULL,
  certification_date  DATE,
  years_experience    INT DEFAULT 0,
  is_primary          BOOLEAN DEFAULT FALSE,
  notes               VARCHAR(255),
  PRIMARY KEY (vet_id, specialty_id),
  FOREIGN KEY (vet_id) REFERENCES vets(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  FOREIGN KEY (specialty_id) REFERENCES specialties(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: types (Tipos de Mascotas)
-- Description: Stores pet type categories
-- ===============================================
CREATE TABLE IF NOT EXISTS types (
  id                  INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  name                VARCHAR(80) NOT NULL,
  description         VARCHAR(100),
  active              BOOLEAN DEFAULT TRUE,
  size_category       VARCHAR(20),
  average_lifespan    INT,
  care_level          VARCHAR(20),
  PRIMARY KEY (id),
  INDEX idx_types_name (name)
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: owners (Dueños de Mascotas)
-- Description: Stores pet owner information
-- ===============================================
CREATE TABLE IF NOT EXISTS owners (
  id            INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  first_name    VARCHAR(30),
  last_name     VARCHAR(30),
  address       VARCHAR(255),
  city          VARCHAR(80),
  telephone     VARCHAR(20),
  PRIMARY KEY (id),
  INDEX idx_owners_last_name (last_name)
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: pets (Mascotas)
-- Description: Stores pet information
-- ===============================================
CREATE TABLE IF NOT EXISTS pets (
  id            INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(30) NOT NULL,
  birth_date    DATE,
  type_id       INT(4) UNSIGNED NOT NULL,
  owner_id      INT(4) UNSIGNED NOT NULL,
  age           INT(4),
  PRIMARY KEY (id),
  INDEX idx_pets_name (name),
  FOREIGN KEY (owner_id) REFERENCES owners(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  FOREIGN KEY (type_id) REFERENCES types(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ===============================================
-- TABLE: visits (Visitas Veterinarias)
-- Description: Stores pet visit records
-- ===============================================
CREATE TABLE IF NOT EXISTS visits (
  id              INT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  pet_id          INT(4) UNSIGNED NOT NULL,
  vet_id          INT(4) UNSIGNED,
  visit_date      DATE NOT NULL,
  description     VARCHAR(255),
  cost            DECIMAL(10,2),
  PRIMARY KEY (id),
  INDEX idx_visits_pet_id (pet_id),
  INDEX idx_visits_vet_id (vet_id),
  INDEX idx_visits_date (visit_date),
  FOREIGN KEY (pet_id) REFERENCES pets(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  FOREIGN KEY (vet_id) REFERENCES vets(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ===============================================
-- END OF SCHEMA CREATION
-- ===============================================