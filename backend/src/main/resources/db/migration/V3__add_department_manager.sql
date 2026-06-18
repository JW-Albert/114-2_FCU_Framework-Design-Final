ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(100);

INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'MANAGER') ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
  SELECT r.id, p.id FROM roles r, permissions p
  WHERE r.name = 'MANAGER' AND p.name IN ('APPROVE_BORROWING', 'SUBMIT_REQUEST')
  ON CONFLICT DO NOTHING;
