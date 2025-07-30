CREATE UNLOGGED TABLE payments (
    correlationid UUID PRIMARY KEY,
    amount DECIMAL NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    payment_service VARCHAR(1) NOT NULL
);

CREATE INDEX payments_requested_at ON payments (requested_at);
CREATE INDEX payments_payment_service ON payments (payment_service);