-- V1__initial_schema.sql
-- Corporate Vehicle Management System

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE vehicles (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plate      VARCHAR(20) NOT NULL UNIQUE,
    model      VARCHAR(100) NOT NULL,
    year       SMALLINT NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                   CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE borrowing_requests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id),
    vehicle_id   UUID NOT NULL REFERENCES vehicles(id),
    period_start TIMESTAMPTZ NOT NULL,
    period_end   TIMESTAMPTZ NOT NULL,
    purpose      TEXT,
    state        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (state IN ('PENDING', 'APPROVED', 'REJECTED', 'IN_USE', 'RETURNED')),
    review_note  TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT valid_period CHECK (period_end > period_start)
);

CREATE INDEX idx_borrowing_vehicle_period ON borrowing_requests (vehicle_id, period_start, period_end);
CREATE INDEX idx_borrowing_user            ON borrowing_requests (user_id);
CREATE INDEX idx_borrowing_state           ON borrowing_requests (state);

CREATE TABLE maintenance_records (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id    UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    date          DATE NOT NULL,
    items         TEXT[] NOT NULL DEFAULT '{}',
    cost          NUMERIC(10, 2),
    next_due_date DATE,
    next_due_km   INTEGER,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_maintenance_vehicle ON maintenance_records (vehicle_id);
