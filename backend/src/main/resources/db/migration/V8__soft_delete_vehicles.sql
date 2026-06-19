-- 車輛軟刪除：新增 deleted_at 欄位（null 表示未刪除）
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS vehicles_deleted_at ON vehicles(deleted_at);
