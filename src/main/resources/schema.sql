-- ===============================================
-- PETCLINIC DATABASE - H2 SCHEMA CREATION SCRIPT
-- ===============================================
-- Author: PetClinic Team
-- Description: H2 Database schema for Pet Clinic Management System
-- Version: 2.0
-- ===============================================

-- ===============================================
-- DROP TABLES (Orden inverso por dependencias)
-- ===============================================
DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS vet_specialties;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS types;
DROP TABLE IF EXISTS specialties;
DROP TABLE IF EXISTS vets;

-- ===============================================
-- TABLE: vets (Veterinarios)
-- Description: Stores veterinarian information
-- ===============================================
CREATE TABLE IF NOT EXISTS vets (
  id              INT AUTO_INCREMENT,
  first_name      VARCHAR(30),
  last_name       VARCHAR(30),
  email           VARCHAR(100),
  phone           VARCHAR(20),
  active          BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_vets_last_name ON vets(last_name);

-- ===============================================
-- TABLE: specialties (Especialidades)
-- Description: Stores veterinary specialties
-- ===============================================
CREATE TABLE IF NOT EXISTS specialties (
  id            INT AUTO_INCREMENT,
  name          VARCHAR(80),
  office        VARCHAR(40),
  h_open        INT,
  h_close       INT,
  PRIMARY KEY (id)
);

CREATE INDEX idx_specialties_name ON specialties(name);

-- ===============================================
-- TABLE: vet_specialties (Relación Veterinario-Especialidad)
-- Description: Many-to-many relationship between vets and specialties
-- ===============================================
CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id              INT NOT NULL,
  specialty_id        INT NOT NULL,
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
);

-- ===============================================
-- TABLE: types (Tipos de Mascotas)
-- Description: Stores pet type categories
-- ===============================================
CREATE TABLE IF NOT EXISTS types (
  id                  INT AUTO_INCREMENT,
  name                VARCHAR(80) NOT NULL,
  description         VARCHAR(100),
  active              BOOLEAN DEFAULT TRUE,
  size_category       VARCHAR(20),
  average_lifespan    INT,
  care_level          VARCHAR(20),
  PRIMARY KEY (id)
);

CREATE INDEX idx_types_name ON types(name);

-- ===============================================
-- TABLE: owners (Dueños de Mascotas)
-- Description: Stores pet owner information
-- ===============================================
CREATE TABLE IF NOT EXISTS owners (
  id            INT AUTO_INCREMENT,
  first_name    VARCHAR(30),
  last_name     VARCHAR(30),
  address       VARCHAR(255),
  city          VARCHAR(80),
  telephone     VARCHAR(20),
  PRIMARY KEY (id)
);

CREATE INDEX idx_owners_last_name ON owners(last_name);

-- ===============================================
-- TABLE: pets (Mascotas)
-- Description: Stores pet information
-- ===============================================
CREATE TABLE IF NOT EXISTS pets (
  id            INT AUTO_INCREMENT,
  name          VARCHAR(30) NOT NULL,
  birth_date    DATE,
  type_id       INT NOT NULL,
  owner_id      INT NOT NULL,
  age           INT,
  PRIMARY KEY (id),
  FOREIGN KEY (owner_id) REFERENCES owners(id) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  FOREIGN KEY (type_id) REFERENCES types(id) 
    ON DELETE RESTRICT 
    ON UPDATE CASCADE
);

CREATE INDEX idx_pets_name ON pets(name);

-- ===============================================
-- TABLE: visits (Visitas Veterinarias)
-- Description: Stores pet visit records
-- ===============================================
CREATE TABLE IF NOT EXISTS visits (
  id              INT AUTO_INCREMENT,
  pet_id          INT NOT NULL,
  vet_id          INT,
  visit_date      DATE NOT NULL,
  description     VARCHAR(255),
  cost            DECIMAL(10,2),
  PRIMARY KEY (id),
  FOREIGN KEY (pet_id) REFERENCES pets(id) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  FOREIGN KEY (vet_id) REFERENCES vets(id) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
);

CREATE INDEX idx_visits_pet_id ON visits(pet_id);
CREATE INDEX idx_visits_vet_id ON visits(vet_id);
CREATE INDEX idx_visits_date ON visits(visit_date);

-- ===============================================
-- END OF SCHEMA CREATION
-- ===============================================