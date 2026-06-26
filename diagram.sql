-- SQL script for draw.io ER Diagram (Multi-Sport Court Booking Platform)

CREATE TABLE accounts (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL, -- e.g., ADMIN, OWNER, STAFF, CUSTOMER
    facility_id BIGINT -- for Staff
);

CREATE TABLE facilities (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    sport_types VARCHAR(255), -- e.g., Badminton, Tennis, Football
    operating_hours VARCHAR(100),
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES accounts(id)
);

-- Adding foreign key to accounts for staff after facilities is created
ALTER TABLE accounts ADD CONSTRAINT fk_account_facility FOREIGN KEY (facility_id) REFERENCES facilities(id);

CREATE TABLE courts (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    quality VARCHAR(100),
    facility_id BIGINT NOT NULL,
    FOREIGN KEY (facility_id) REFERENCES facilities(id)
);

CREATE TABLE time_slots (
    id BIGINT PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price DOUBLE NOT NULL,
    court_id BIGINT NOT NULL,
    FOREIGN KEY (court_id) REFERENCES courts(id)
);

CREATE TABLE bookings (
    id BIGINT PRIMARY KEY,
    booking_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., PENDING, CONFIRMED, CANCELLED
    payment_method VARCHAR(50) NOT NULL, -- e.g., VNPAY, CASH
    customer_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES accounts(id),
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    amount DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., SUCCESS, PENDING, FAILED
    payment_time DATETIME,
    booking_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY,
    rating INT NOT NULL,
    comment VARCHAR(500),
    created_at DATETIME,
    facility_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    FOREIGN KEY (facility_id) REFERENCES facilities(id),
    FOREIGN KEY (customer_id) REFERENCES accounts(id)
);
