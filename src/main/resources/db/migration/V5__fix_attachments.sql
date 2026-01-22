-- V5: Fix attachments table schema
-- Renames fecha_creacion to fecha_carga and adds descripcion column
-- This resolves the discrepancy between V1 schema and SQLiteAttachmentRepository

-- SQLite doesn't support RENAME COLUMN in older versions
-- Using table recreation approach for compatibility

CREATE TABLE IF NOT EXISTS attachments_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clinical_history_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    ruta_archivo TEXT NOT NULL,
    tipo TEXT,
    tamano_bytes INTEGER,
    fecha_carga TEXT DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT,
    FOREIGN KEY (clinical_history_id) REFERENCES clinical_histories(id) ON DELETE CASCADE
);

-- Only migrate data if old table exists with fecha_creacion column
INSERT OR IGNORE INTO attachments_new (id, clinical_history_id, nombre, ruta_archivo, tipo, tamano_bytes, fecha_carga)
SELECT id, clinical_history_id, nombre, ruta_archivo, tipo, tamano_bytes, fecha_creacion
FROM attachments
WHERE EXISTS (SELECT 1 FROM pragma_table_info('attachments') WHERE name = 'fecha_creacion');

-- Handle case where fecha_carga already exists (from repository's initializeTable)
INSERT OR IGNORE INTO attachments_new (id, clinical_history_id, nombre, ruta_archivo, tipo, tamano_bytes, fecha_carga, descripcion)
SELECT id, clinical_history_id, nombre, ruta_archivo, tipo, tamano_bytes, fecha_carga, descripcion
FROM attachments
WHERE EXISTS (SELECT 1 FROM pragma_table_info('attachments') WHERE name = 'fecha_carga');

DROP TABLE IF EXISTS attachments;

ALTER TABLE attachments_new RENAME TO attachments;

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_attachments_clinical_history ON attachments(clinical_history_id);
