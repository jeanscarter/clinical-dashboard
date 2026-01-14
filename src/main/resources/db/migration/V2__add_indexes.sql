-- V2: Add Indexes
-- Performance optimization indexes for better query performance

-- Index on patients cedula for quick lookup
CREATE INDEX IF NOT EXISTS idx_patients_cedula ON patients(cedula);

-- Index on patients nombre for search
CREATE INDEX IF NOT EXISTS idx_patients_nombre ON patients(nombre);

-- Index on patients apellido for search
CREATE INDEX IF NOT EXISTS idx_patients_apellido ON patients(apellido);

-- Index on clinical_histories patient_id for filtering by patient
CREATE INDEX IF NOT EXISTS idx_clinical_histories_patient_id ON clinical_histories(patient_id);

-- Index on clinical_histories fecha for date-based queries
CREATE INDEX IF NOT EXISTS idx_clinical_histories_fecha ON clinical_histories(fecha_consulta);

-- Index on clinical_histories medico for filtering by doctor
CREATE INDEX IF NOT EXISTS idx_clinical_histories_medico ON clinical_histories(medico);

-- Index on attachments clinical_history_id for loading attachments
CREATE INDEX IF NOT EXISTS idx_attachments_history_id ON attachments(clinical_history_id);

-- Composite index for patient search
CREATE INDEX IF NOT EXISTS idx_patients_search ON patients(apellido, nombre);

-- Composite index for history lookups with date ordering
CREATE INDEX IF NOT EXISTS idx_clinical_histories_patient_date ON clinical_histories(patient_id, fecha_consulta DESC);
