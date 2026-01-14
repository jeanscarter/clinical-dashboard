-- V1: Initial Schema
-- Creates the core tables for the Clinical Management System

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cedula TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    apellido TEXT,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    sexo TEXT,
    fecha_nacimiento TEXT,
    fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Clinical histories table
CREATE TABLE IF NOT EXISTS clinical_histories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    fecha_consulta TEXT NOT NULL,
    motivo_consulta TEXT NOT NULL,
    antecedentes TEXT,
    examen_fisico TEXT,
    diagnostico TEXT,
    conducta TEXT,
    observaciones TEXT,
    medico TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clinical_history_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    ruta_archivo TEXT NOT NULL,
    tipo TEXT,
    tamano_bytes INTEGER,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clinical_history_id) REFERENCES clinical_histories(id) ON DELETE CASCADE
);
