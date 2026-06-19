-- 站內通知（收件夾）資料表
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_borrowing_id UUID REFERENCES borrowing_requests(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 收件夾查詢（依收件人、時間排序）與未讀計數的索引
CREATE INDEX IF NOT EXISTS notifications_recipient_created
    ON notifications(recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS notifications_recipient_unread
    ON notifications(recipient_id, is_read);
