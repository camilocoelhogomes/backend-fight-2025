CREATE UNLOGGED TABLE payments (
    id SERIAL PRIMARY KEY,
    correlationid UUID NOT NULL,
    amount DECIMAL NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    payment_service VARCHAR(1) NOT NULL
);

CREATE UNLOGGED TABLE payment_dlq (
    id SERIAL PRIMARY KEY,
    correlationid UUID NOT NULL,
    amount DECIMAL NOT NULL,
    partition_key INTEGER NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX payments_requested_at ON payments (requested_at);
CREATE INDEX payments_payment_service ON payments (payment_service);
CREATE INDEX payment_dlq_processed ON payment_dlq (processed);
CREATE INDEX payment_dlq_partition_key ON payment_dlq (partition_key);