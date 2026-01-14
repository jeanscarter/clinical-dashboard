-- V3: Add Audit Table
-- Audit logging for tracking changes to important records

CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    record_id INTEGER NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    user_id INTEGER,
    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
    old_values TEXT,
    new_values TEXT
);

-- Index for querying audit by table and record
CREATE INDEX IF NOT EXISTS idx_audit_table_record ON audit_log(table_name, record_id);

-- Index for querying audit by timestamp
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_log(timestamp);

-- Index for querying audit by user
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id);
