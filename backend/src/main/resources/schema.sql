-- Property Rates
CREATE TABLE IF NOT EXISTS property_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    landmark VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    rate_2018 DOUBLE NOT NULL,
    rate_2024 DOUBLE NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL
);

-- Bank Offers
CREATE TABLE IF NOT EXISTS bank_offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank VARCHAR(100) NOT NULL,
    product VARCHAR(100) NOT NULL,
    customer VARCHAR(50) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    rate_min DOUBLE NOT NULL,
    rate_max DOUBLE NOT NULL,
    proc_pct DOUBLE NOT NULL,
    proc_min DOUBLE NOT NULL,
    proc_max DOUBLE NOT NULL,
    max_tenure INT NOT NULL,
    link VARCHAR(500) NOT NULL,
    last_updated VARCHAR(20)
);

-- Stamp Duty Rules
CREATE TABLE IF NOT EXISTS stamp_duty_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state VARCHAR(100) NOT NULL UNIQUE,
    stamp_base DOUBLE NOT NULL,
    stamp_female DOUBLE,
    stamp_joint DOUBLE,
    registration_pct DOUBLE NOT NULL
);

-- Contact Requests
CREATE TABLE IF NOT EXISTS contact_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    purpose VARCHAR(100),
    preferred_date VARCHAR(20),
    preferred_time VARCHAR(20),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
