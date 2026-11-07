-- ===============================================
-- PETCLINIC DATABASE - DATA INSERTION SCRIPT
-- ===============================================
-- Author: PetClinic Team
-- Description: Sample data for Pet Clinic Management System
-- Version: 2.0
-- ===============================================

USE PETCLINIC_DB;

-- ===============================================
-- 1. INSERT VETS (Veterinarios)
-- ===============================================
INSERT INTO vets (id, first_name, last_name, email, phone, active) VALUES
(1, 'James', 'Carter', 'james.carter@petclinic.com', '6085551234', true),
(2, 'Helen', 'Leary', 'helen.leary@petclinic.com', '6085552345', true),
(3, 'Linda', 'Douglas', 'linda.douglas@petclinic.com', '6085553456', true),
(4, 'Rafael', 'Ortega', 'rafael.ortega@petclinic.com', '6085554567', true),
(5, 'Henry', 'Stevens', 'henry.stevens@petclinic.com', '6085555678', true),
(6, 'Sharon', 'Jenkins', 'sharon.jenkins@petclinic.com', '6085556789', false);

-- ===============================================
-- 2. INSERT SPECIALTIES (Especialidades)
-- ===============================================
INSERT INTO specialties (id, name, office, h_open, h_close) VALUES
(1, 'radiology', 'Farewell', 8, 18),
(2, 'surgery', 'Maryland', 8, 12),
(3, 'dentistry', 'Terranova', 9, 19);

-- ===============================================
-- 3. INSERT VET_SPECIALTIES (Relación Vet-Specialty)
-- ===============================================
INSERT INTO vet_specialties (vet_id, specialty_id, certification_date, years_experience, is_primary, notes) VALUES
(2, 1, '2015-06-15', 8, true, 'Expert in X-ray imaging'),
(3, 2, '2012-03-20', 11, true, 'Specialized in orthopedic surgery'),
(3, 3, '2018-11-10', 5, false, 'Additional dental training'),
(4, 2, '2014-09-05', 9, true, 'Soft tissue surgery specialist'),
(5, 1, '2016-02-28', 7, false, 'CT and MRI certified');

-- ===============================================
-- 4. INSERT TYPES (Tipos de Mascotas)
-- ===============================================
INSERT INTO types (id, name, description, active, size_category, average_lifespan, care_level) VALUES
(1, 'cat', 'Domestic feline', true, 'small', 15, 'medium'),
(2, 'dog', 'Domestic canine', true, 'medium', 12, 'medium'),
(3, 'lizard', 'Reptile', true, 'small', 10, 'high'),
(4, 'bird', 'Avian', true, 'small', 8, 'medium'),
(5, 'hamster', 'Small rodent', true, 'small', 3, 'low'),
(6, 'snake', 'Serpent', false, 'medium', 20, 'high'),
(7, 'rabbit', 'Domestic rabbit', true, 'small', 9, 'medium'),
(8, 'turtle', 'Turtle or tortoise', true, 'small', 30, 'medium');

-- ===============================================
-- 5. INSERT OWNERS (Dueños)
-- ===============================================
INSERT INTO owners (id, first_name, last_name, address, city, telephone) VALUES
(1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023'),
(2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749'),
(3, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763'),
(4, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198'),
(5, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765'),
(6, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654'),
(7, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387'),
(8, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683'),
(9, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435'),
(10, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

-- ===============================================
-- 6. INSERT PETS (Mascotas)
-- ===============================================
INSERT INTO pets (id, name, birth_date, type_id, owner_id, age) VALUES
(1, 'Leo', '2000-09-07', 1, 1, 1),
(2, 'Basil', '2002-08-06', 6, 2, 2),
(3, 'Rosy', '2001-04-17', 2, 3, 3),
(4, 'Jewel', '2000-03-07', 2, 3, 3),
(5, 'Iggy', '2000-11-30', 3, 4, 8),
(6, 'George', '2000-01-20', 4, 5, 8),
(7, 'Samantha', '1995-09-04', 1, 6, 10),
(8, 'Max', '1995-09-04', 1, 6, 1),
(9, 'Lucky', '1999-08-06', 5, 7, 2),
(10, 'Mulligan', '1997-02-24', 2, 8, 2),
(11, 'Freddy', '2000-03-09', 5, 9, 4),
(12, 'Lucky', '2000-06-24', 2, 10, 3),
(13, 'Sly', '2002-06-08', 1, 10, 1);

-- ===============================================
-- 7. INSERT VISITS (Visitas)
-- ===============================================
INSERT INTO visits (id, pet_id, vet_id, visit_date, description, cost) VALUES
(1, 7, 2, '2010-03-04', 'rabies shot', 45.00),
(2, 8, 2, '2011-03-04', 'rabies shot', 45.00),
(3, 8, 3, '2009-06-04', 'neutered', 250.00),
(4, 7, 3, '2008-09-04', 'spayed', 275.00),
(5, 1, 1, '2024-01-15', 'general checkup', 65.00),
(6, 3, 4, '2024-02-20', 'dental cleaning', 150.00);

-- ===============================================
-- END OF DATA INSERTION
-- ===============================================