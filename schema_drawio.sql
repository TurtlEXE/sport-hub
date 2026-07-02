CREATE TABLE Account (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    google_id VARCHAR(255),
    full_name NVARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    avatar_path VARCHAR(255),
    role VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE Guest (
    guest_id INT PRIMARY KEY AUTO_INCREMENT,
    guest_name NVARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE Facility (
    facility_id INT PRIMARY KEY AUTO_INCREMENT,
    owner_account_id INT NOT NULL,
    name NVARCHAR(255) NOT NULL,
    province NVARCHAR(100),
    district NVARCHAR(100),
    ward NVARCHAR(100),
    address NVARCHAR(255) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    description TEXT,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    approval_status VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL UNIQUE,
    facility_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE OwnerProfile (
    owner_profile_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL UNIQUE,
    business_name NVARCHAR(255) NOT NULL,
    tax_code VARCHAR(50),
    bank_name NVARCHAR(255),
    bank_account_no VARCHAR(50),
    bank_account_name NVARCHAR(255),
    approval_status VARCHAR(20),
    approved_by INT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE FacilityImage (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_id INT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE TABLE Sport (
    sport_id INT PRIMARY KEY AUTO_INCREMENT,
    sport_code VARCHAR(30) UNIQUE NOT NULL,
    sport_name NVARCHAR(255) NOT NULL,
    icon_path VARCHAR(255),
    default_min_duration_minutes INT NOT NULL,
    default_slot_step_minutes INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE FacilitySport (
    facility_sport_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_id INT NOT NULL,
    sport_id INT NOT NULL,
    min_duration_minutes INT NOT NULL,
    slot_step_minutes INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE (facility_id, sport_id)
);

CREATE TABLE SportAttribute (
    attribute_id INT PRIMARY KEY AUTO_INCREMENT,
    sport_id INT NOT NULL,
    attribute_code VARCHAR(50) NOT NULL,
    attribute_name NVARCHAR(255) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    options_json TEXT,
    is_required BOOLEAN DEFAULT FALSE
);

CREATE TABLE Court (
    court_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_sport_id INT NOT NULL,
    court_name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE CourtAttributeValue (
    id INT PRIMARY KEY AUTO_INCREMENT,
    court_id INT NOT NULL,
    attribute_id INT NOT NULL,
    value NVARCHAR(255) NOT NULL,
    UNIQUE (court_id, attribute_id)
);

CREATE TABLE FacilityPriceRule (
    price_rule_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_sport_id INT NOT NULL,
    day_type VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price_per_slot DECIMAL(12,2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE CourtScheduleException (
    exception_id INT PRIMARY KEY AUTO_INCREMENT,
    court_id INT NOT NULL,
    facility_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    exception_type VARCHAR(20),
    reason NVARCHAR(300),
    created_by INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE Booking (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_id INT NOT NULL,
    account_id INT,
    guest_id INT,
    staff_id INT,
    booking_status VARCHAR(20),
    hold_expired_at TIMESTAMP,
    checkin_time TIMESTAMP,
    checkout_time TIMESTAMP,
    note NVARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE BookingSlot (
    booking_slot_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    court_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price_snapshot DECIMAL(12,2) NOT NULL,
    slot_status VARCHAR(20),
    checkin_time TIMESTAMP,
    checkout_time TIMESTAMP
);

CREATE TABLE CourtSlotBooking (
    id INT PRIMARY KEY AUTO_INCREMENT,
    court_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    booking_slot_id INT NOT NULL,
    UNIQUE (court_id, booking_date, start_time)
);

CREATE TABLE ProductCategory (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(30) UNIQUE NOT NULL,
    category_name NVARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE Product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    facility_id INT NOT NULL,
    category_id INT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    image_path VARCHAR(255),
    product_type VARCHAR(10) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    rental_unit VARCHAR(20),
    stock_quantity INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE OrderItem (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price_snapshot DECIMAL(12,2) NOT NULL,
    rental_duration INT,
    total_amount DECIMAL(12,2) NOT NULL,
    added_by VARCHAR(10),
    created_at TIMESTAMP
);

CREATE TABLE Voucher (
    voucher_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    issuer_type VARCHAR(10) NOT NULL,
    issuer_account_id INT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(12,2) DEFAULT 0,
    max_discount_amount DECIMAL(12,2),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    usage_limit INT,
    per_user_limit INT DEFAULT 1,
    applicable_to VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE VoucherFacility (
    voucher_id INT NOT NULL,
    facility_id INT NOT NULL,
    PRIMARY KEY (voucher_id, facility_id)
);

CREATE TABLE VoucherAccount (
    voucher_id INT NOT NULL,
    account_id INT NOT NULL,
    PRIMARY KEY (voucher_id, account_id)
);

CREATE TABLE Invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL UNIQUE,
    court_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    product_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    voucher_id INT,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    deposit_percent INT DEFAULT 100,
    payment_status VARCHAR(20),
    refund_due DECIMAL(12,2) NOT NULL DEFAULT 0,
    refund_status VARCHAR(20),
    refund_note NVARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE Payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    vnpay_txn_no VARCHAR(100),
    vnpay_response_code VARCHAR(10),
    transaction_code VARCHAR(100),
    paid_amount DECIMAL(12,2),
    payment_time TIMESTAMP,
    expire_at TIMESTAMP,
    payment_type VARCHAR(20),
    method VARCHAR(20),
    payment_status VARCHAR(20),
    staff_confirm_id INT,
    confirm_time TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE VoucherUsage (
    usage_id INT PRIMARY KEY AUTO_INCREMENT,
    voucher_id INT NOT NULL,
    account_id INT,
    booking_id INT NOT NULL,
    invoice_id INT NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    liability_party VARCHAR(10) NOT NULL,
    used_at TIMESTAMP
);

CREATE TABLE CommissionTier (
    tier_id INT PRIMARY KEY AUTO_INCREMENT,
    min_price_per_minute DECIMAL(12,2) NOT NULL,
    max_price_per_minute DECIMAL(12,2),
    commission_rate DECIMAL(5,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20),
    announced_at TIMESTAMP,
    notice_days INT,
    description NVARCHAR(500),
    created_by INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE PlatformCommission (
    commission_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL UNIQUE,
    owner_account_id INT NOT NULL,
    court_revenue DECIMAL(12,2) NOT NULL,
    commission_tier_id INT,
    commission_rate DECIMAL(5,4) NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    voucher_cost_owner DECIMAL(12,2) DEFAULT 0,
    voucher_cost_platform DECIMAL(12,2) DEFAULT 0,
    owner_payout DECIMAL(12,2) NOT NULL,
    status VARCHAR(20),
    settled_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE CommissionChangeLog (
    change_log_id INT PRIMARY KEY AUTO_INCREMENT,
    old_tier_id INT,
    old_rate DECIMAL(5,4),
    new_tier_id INT NOT NULL,
    new_rate DECIMAL(5,4) NOT NULL,
    min_price_per_minute DECIMAL(12,2) NOT NULL,
    max_price_per_minute DECIMAL(12,2),
    effective_from TIMESTAMP NOT NULL,
    announced_at TIMESTAMP NOT NULL,
    notice_days INT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    reason NVARCHAR(500),
    changed_by INT NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE CommissionPolicy (
    policy_id INT PRIMARY KEY AUTO_INCREMENT,
    min_notice_days INT NOT NULL DEFAULT 14,
    description NVARCHAR(500),
    updated_by INT,
    updated_at TIMESTAMP
);

CREATE TABLE BookingChangeLog (
    change_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    old_court_id INT,
    new_court_id INT,
    old_start_time TIME,
    new_start_time TIME,
    old_end_time TIME,
    new_end_time TIME,
    old_booking_date DATE,
    new_booking_date DATE,
    change_type VARCHAR(20),
    change_time TIMESTAMP,
    note NVARCHAR(255),
    actor_staff_id INT,
    change_action VARCHAR(30),
    before_data TEXT,
    after_data TEXT,
    reason NVARCHAR(500),
    etag_before VARCHAR(64),
    etag_after VARCHAR(64),
    refund_due DECIMAL(12,2)
);

CREATE TABLE Review (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL UNIQUE,
    account_id INT NOT NULL,
    rating INT NOT NULL,
    comment NVARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE BlogPost (
    post_id INT PRIMARY KEY AUTO_INCREMENT,
    author_account_id INT NOT NULL,
    title NVARCHAR(200) NOT NULL,
    summary NVARCHAR(500),
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    published_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE BlogComment (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL,
    author_account_id INT NOT NULL,
    content NVARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    moderated_by_account_id INT,
    moderated_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE BlogReaction (
    reaction_id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL,
    account_id INT NOT NULL,
    emoji_code VARCHAR(30) NOT NULL,
    created_at TIMESTAMP,
    UNIQUE (post_id, account_id, emoji_code)
);

CREATE TABLE Notification (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    title NVARCHAR(255),
    content NVARCHAR(500),
    type VARCHAR(10),
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE TABLE CustomerFavoriteFacility (
    favorite_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    facility_id INT NOT NULL,
    UNIQUE (account_id, facility_id)
);

CREATE TABLE EmailQueue (
    email_id INT PRIMARY KEY AUTO_INCREMENT,
    email_type VARCHAR(30) NOT NULL,
    booking_id INT,
    to_email VARCHAR(255) NOT NULL,
    payload_json TEXT,
    reminder_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    last_error NVARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE TABLE EmailVerification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(10) NOT NULL,
    token VARCHAR(255) NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE PasswordResetToken (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expire_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE Facility ADD FOREIGN KEY (owner_account_id) REFERENCES Account(account_id);
ALTER TABLE Staff ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE Staff ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE OwnerProfile ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE OwnerProfile ADD FOREIGN KEY (approved_by) REFERENCES Account(account_id);
ALTER TABLE FacilityImage ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE FacilitySport ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE FacilitySport ADD FOREIGN KEY (sport_id) REFERENCES Sport(sport_id);
ALTER TABLE SportAttribute ADD FOREIGN KEY (sport_id) REFERENCES Sport(sport_id);
ALTER TABLE Court ADD FOREIGN KEY (facility_sport_id) REFERENCES FacilitySport(facility_sport_id);
ALTER TABLE CourtAttributeValue ADD FOREIGN KEY (court_id) REFERENCES Court(court_id);
ALTER TABLE CourtAttributeValue ADD FOREIGN KEY (attribute_id) REFERENCES SportAttribute(attribute_id);
ALTER TABLE FacilityPriceRule ADD FOREIGN KEY (facility_sport_id) REFERENCES FacilitySport(facility_sport_id);
ALTER TABLE CourtScheduleException ADD FOREIGN KEY (court_id) REFERENCES Court(court_id);
ALTER TABLE CourtScheduleException ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE CourtScheduleException ADD FOREIGN KEY (created_by) REFERENCES Staff(staff_id);
ALTER TABLE Booking ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE Booking ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE Booking ADD FOREIGN KEY (guest_id) REFERENCES Guest(guest_id);
ALTER TABLE Booking ADD FOREIGN KEY (staff_id) REFERENCES Staff(staff_id);
ALTER TABLE BookingSlot ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE BookingSlot ADD FOREIGN KEY (court_id) REFERENCES Court(court_id);
ALTER TABLE CourtSlotBooking ADD FOREIGN KEY (court_id) REFERENCES Court(court_id);
ALTER TABLE CourtSlotBooking ADD FOREIGN KEY (booking_slot_id) REFERENCES BookingSlot(booking_slot_id);
ALTER TABLE Product ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE Product ADD FOREIGN KEY (category_id) REFERENCES ProductCategory(category_id);
ALTER TABLE OrderItem ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE OrderItem ADD FOREIGN KEY (product_id) REFERENCES Product(product_id);
ALTER TABLE Voucher ADD FOREIGN KEY (issuer_account_id) REFERENCES Account(account_id);
ALTER TABLE VoucherFacility ADD FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id);
ALTER TABLE VoucherFacility ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE VoucherAccount ADD FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id);
ALTER TABLE VoucherAccount ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE Invoice ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE Invoice ADD FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id);
ALTER TABLE Payment ADD FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id);
ALTER TABLE Payment ADD FOREIGN KEY (staff_confirm_id) REFERENCES Staff(staff_id);
ALTER TABLE VoucherUsage ADD FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id);
ALTER TABLE VoucherUsage ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE VoucherUsage ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE VoucherUsage ADD FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id);
ALTER TABLE CommissionTier ADD FOREIGN KEY (created_by) REFERENCES Account(account_id);
ALTER TABLE PlatformCommission ADD FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id);
ALTER TABLE PlatformCommission ADD FOREIGN KEY (owner_account_id) REFERENCES Account(account_id);
ALTER TABLE PlatformCommission ADD FOREIGN KEY (commission_tier_id) REFERENCES CommissionTier(tier_id);
ALTER TABLE CommissionChangeLog ADD FOREIGN KEY (old_tier_id) REFERENCES CommissionTier(tier_id);
ALTER TABLE CommissionChangeLog ADD FOREIGN KEY (new_tier_id) REFERENCES CommissionTier(tier_id);
ALTER TABLE CommissionChangeLog ADD FOREIGN KEY (changed_by) REFERENCES Account(account_id);
ALTER TABLE CommissionPolicy ADD FOREIGN KEY (updated_by) REFERENCES Account(account_id);
ALTER TABLE BookingChangeLog ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE BookingChangeLog ADD FOREIGN KEY (old_court_id) REFERENCES Court(court_id);
ALTER TABLE BookingChangeLog ADD FOREIGN KEY (new_court_id) REFERENCES Court(court_id);
ALTER TABLE BookingChangeLog ADD FOREIGN KEY (actor_staff_id) REFERENCES Staff(staff_id);
ALTER TABLE Review ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
ALTER TABLE Review ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE BlogPost ADD FOREIGN KEY (author_account_id) REFERENCES Account(account_id);
ALTER TABLE BlogComment ADD FOREIGN KEY (post_id) REFERENCES BlogPost(post_id);
ALTER TABLE BlogComment ADD FOREIGN KEY (author_account_id) REFERENCES Account(account_id);
ALTER TABLE BlogComment ADD FOREIGN KEY (moderated_by_account_id) REFERENCES Account(account_id);
ALTER TABLE BlogReaction ADD FOREIGN KEY (post_id) REFERENCES BlogPost(post_id);
ALTER TABLE BlogReaction ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE Notification ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE CustomerFavoriteFacility ADD FOREIGN KEY (account_id) REFERENCES Account(account_id);
ALTER TABLE CustomerFavoriteFacility ADD FOREIGN KEY (facility_id) REFERENCES Facility(facility_id);
ALTER TABLE EmailQueue ADD FOREIGN KEY (booking_id) REFERENCES Booking(booking_id);
