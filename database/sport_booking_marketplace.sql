USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'sport_booking_marketplace')
BEGIN
    ALTER DATABASE sport_booking_marketplace
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE sport_booking_marketplace;
END
GO

CREATE DATABASE sport_booking_marketplace;
GO

USE sport_booking_marketplace;
GO

-- ============================================================================
-- 1. ACCOUNT & AUTHENTICATION
-- ============================================================================

CREATE TABLE Account (
    account_id    INT IDENTITY PRIMARY KEY,
    email         NVARCHAR(255) UNIQUE NULL,
    password_hash NVARCHAR(255),
    google_id     NVARCHAR(255) NULL,
    full_name     NVARCHAR(255) NOT NULL,
    phone         NVARCHAR(20) UNIQUE NULL,
    avatar_path   NVARCHAR(500) NULL,
    role          VARCHAR(10)
        CHECK (role IN ('ADMIN','OWNER','STAFF','CUSTOMER')) NOT NULL,
    is_active     BIT DEFAULT 1,
    created_at    DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE EmailVerification (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    full_name     NVARCHAR(255) NOT NULL,
    phone         NVARCHAR(20) NULL,
    role          VARCHAR(10) NOT NULL,
    token         NVARCHAR(255) NOT NULL,
    expire_at     DATETIME NOT NULL,
    created_at    DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE PasswordResetToken (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL,
    token         NVARCHAR(255) NOT NULL,
    expire_at     DATETIME NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT GETDATE()
);
GO

CREATE UNIQUE INDEX UX_PasswordResetToken_Token
    ON PasswordResetToken(token);
GO

-- ============================================================================
-- 2. OWNER PROFILE (B2B layer — thông tin kinh doanh của chủ sân)
-- ============================================================================

CREATE TABLE OwnerProfile (
    owner_profile_id  INT IDENTITY PRIMARY KEY,
    account_id        INT UNIQUE NOT NULL,
    business_name     NVARCHAR(255) NOT NULL,
    tax_code          NVARCHAR(50) NULL,
    bank_name         NVARCHAR(100) NULL,
    bank_account_no   NVARCHAR(50) NULL,
    bank_account_name NVARCHAR(255) NULL,
    approval_status   VARCHAR(20)
        CHECK (approval_status IN ('PENDING','APPROVED','REJECTED'))
        DEFAULT 'PENDING',
    approved_by       INT NULL,
    approved_at       DATETIME NULL,
    created_at        DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (account_id)  REFERENCES Account(account_id),
    FOREIGN KEY (approved_by) REFERENCES Account(account_id)
);
GO

-- ============================================================================
-- 3. SPORT & SPORT ATTRIBUTES (Multi-sport support)
-- ============================================================================

CREATE TABLE Sport (
    sport_id                     INT IDENTITY PRIMARY KEY,
    sport_code                   VARCHAR(30) UNIQUE NOT NULL,
    sport_name                   NVARCHAR(100) NOT NULL,
    icon_path                    NVARCHAR(500) NULL,
    default_min_duration_minutes INT NOT NULL DEFAULT 30,
    default_slot_step_minutes    INT NOT NULL DEFAULT 30,
    is_active                    BIT DEFAULT 1
);
GO

CREATE TABLE SportAttribute (
    attribute_id   INT IDENTITY PRIMARY KEY,
    sport_id       INT NOT NULL,
    attribute_code VARCHAR(50) NOT NULL,
    attribute_name NVARCHAR(100) NOT NULL,
    data_type      VARCHAR(20)
        CHECK (data_type IN ('TEXT','NUMBER','BOOLEAN','SELECT')) NOT NULL,
    options_json   NVARCHAR(MAX) NULL,    -- cho SELECT: ["Cỏ nhân tạo","Cỏ tự nhiên"]
    is_required    BIT DEFAULT 0,

    FOREIGN KEY (sport_id) REFERENCES Sport(sport_id),
    UNIQUE (sport_id, attribute_code)
);
GO

-- ============================================================================
-- 4. FACILITY (Cơ sở — giờ thuộc về Owner)
-- ============================================================================

CREATE TABLE Facility (
    facility_id      INT IDENTITY PRIMARY KEY,
    owner_account_id INT NOT NULL,
    name             NVARCHAR(255) NOT NULL,
    province         NVARCHAR(100),
    district         NVARCHAR(100),
    ward             NVARCHAR(100),
    address          NVARCHAR(255) NOT NULL,
    latitude         DECIMAL(10, 8) NULL,
    longitude        DECIMAL(11, 8) NULL,
    description      NVARCHAR(MAX),
    open_time        TIME NOT NULL,
    close_time       TIME NOT NULL,
    approval_status  VARCHAR(20)
        CHECK (approval_status IN ('PENDING','APPROVED','REJECTED'))
        DEFAULT 'PENDING',
    is_active        BIT DEFAULT 1,
    created_at       DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (owner_account_id) REFERENCES Account(account_id)
);
GO

CREATE TABLE FacilityImage (
    image_id     INT IDENTITY PRIMARY KEY,
    facility_id  INT NOT NULL,
    image_path   NVARCHAR(500) NOT NULL,
    is_thumbnail BIT DEFAULT 0,
    created_at   DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

CREATE UNIQUE INDEX UX_Facility_Thumbnail
    ON FacilityImage(facility_id)
    WHERE is_thumbnail = 1;
GO

-- ============================================================================
-- 5. FACILITY-SPORT (Liên kết cơ sở ↔ bộ môn, Owner config slot riêng)
-- ============================================================================

CREATE TABLE FacilitySport (
    facility_sport_id    INT IDENTITY PRIMARY KEY,
    facility_id          INT NOT NULL,
    sport_id             INT NOT NULL,
    min_duration_minutes INT NOT NULL,   -- thời lượng tối thiểu (phút)
    slot_step_minutes    INT NOT NULL,   -- bước nhảy slot (phút): 30, 60, 45...
    is_active            BIT DEFAULT 1,

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (sport_id)    REFERENCES Sport(sport_id),
    UNIQUE (facility_id, sport_id)
);
GO

-- ============================================================================
-- 6. COURT (Sân — thuộc FacilitySport)
-- ============================================================================

CREATE TABLE Court (
    court_id          INT IDENTITY PRIMARY KEY,
    facility_sport_id INT NOT NULL,
    court_name        NVARCHAR(100) NOT NULL,
    description       NVARCHAR(500),
    is_active         BIT DEFAULT 1,

    FOREIGN KEY (facility_sport_id) REFERENCES FacilitySport(facility_sport_id)
);
GO

-- Thuộc tính cụ thể của từng sân (sân cỏ nhân tạo, indoor, 5 người...)
CREATE TABLE CourtAttributeValue (
    id           INT IDENTITY PRIMARY KEY,
    court_id     INT NOT NULL,
    attribute_id INT NOT NULL,
    value        NVARCHAR(500) NOT NULL,

    FOREIGN KEY (court_id)     REFERENCES Court(court_id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES SportAttribute(attribute_id),
    UNIQUE (court_id, attribute_id)
);
GO

-- ============================================================================
-- 7. FACILITY PRICE RULE (Giá theo FacilitySport, khung giờ, ngày)
-- ============================================================================

CREATE TABLE FacilityPriceRule (
    price_rule_id     INT IDENTITY PRIMARY KEY,
    facility_sport_id INT NOT NULL,
    day_type          VARCHAR(10)
        CHECK (day_type IN ('WEEKDAY','WEEKEND','HOLIDAY')) NOT NULL,
    start_time        TIME NOT NULL,
    end_time          TIME NOT NULL,
    price_per_slot    DECIMAL(12,2) NOT NULL,   -- giá cho 1 slot (theo slot_step_minutes)
    effective_from    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    effective_to      DATE NULL,                -- NULL = vô thời hạn
    is_active         BIT DEFAULT 1,
    created_at        DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (facility_sport_id) REFERENCES FacilitySport(facility_sport_id),
    CHECK (end_time > start_time)
);
GO

-- ============================================================================
-- 8. PRODUCT & SHOP (Thay thế Inventory/RacketRental cũ)
-- ============================================================================

CREATE TABLE ProductCategory (
    category_id   INT IDENTITY PRIMARY KEY,
    category_code VARCHAR(30) UNIQUE NOT NULL,
    category_name NVARCHAR(100) NOT NULL,
    is_active     BIT DEFAULT 1
);
GO

CREATE TABLE Product (
    product_id   INT IDENTITY PRIMARY KEY,
    facility_id  INT NOT NULL,
    category_id  INT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    description  NVARCHAR(500) NULL,
    image_path   NVARCHAR(500) NULL,

    product_type VARCHAR(10)
        CHECK (product_type IN ('SALE','RENTAL')) NOT NULL,
    -- SALE  : bán đứt (nước, cầu lông, khăn...)
    -- RENTAL: thuê theo thời gian (vợt, giày...)

    price          DECIMAL(12,2) NOT NULL,   -- giá bán / giá thuê per unit
    rental_unit    VARCHAR(20) NULL
        CHECK (rental_unit IN ('PER_HOUR','PER_30MIN','PER_SESSION') OR rental_unit IS NULL),
    stock_quantity INT NULL,                 -- NULL = không giới hạn
    is_active      BIT DEFAULT 1,
    created_at     DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (category_id) REFERENCES ProductCategory(category_id)
);
GO

-- ============================================================================
-- 9. STAFF & GUEST
-- ============================================================================

CREATE TABLE Staff (
    staff_id    INT IDENTITY PRIMARY KEY,
    account_id  INT UNIQUE NOT NULL,
    facility_id INT NOT NULL,
    is_active   BIT DEFAULT 1,

    FOREIGN KEY (account_id)  REFERENCES Account(account_id),
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id)
);
GO

CREATE TABLE Guest (
    guest_id   INT IDENTITY PRIMARY KEY,
    guest_name NVARCHAR(255) NOT NULL,
    phone      NVARCHAR(20) NOT NULL,
    email      NVARCHAR(255) NULL
);
GO

-- ============================================================================
-- 10. BOOKING (Core — giữ logic cũ, bỏ recurring tạm thời)
-- ============================================================================

CREATE TABLE Booking (
    booking_id     INT IDENTITY PRIMARY KEY,
    facility_id    INT NOT NULL,

    account_id     INT NULL,       -- customer online
    guest_id       INT NULL,       -- walk-in / phone
    staff_id       INT NULL,       -- staff tạo hộ

    booking_status VARCHAR(20)
        CHECK (booking_status IN ('PENDING','CONFIRMED','EXPIRED','CANCELLED','COMPLETED'))
        DEFAULT 'PENDING',

    hold_expired_at DATETIME NULL,
    checkin_time    DATETIME NULL,
    checkout_time   DATETIME NULL,
    note            NVARCHAR(500) NULL,
    created_at      DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (account_id)  REFERENCES Account(account_id),
    FOREIGN KEY (guest_id)    REFERENCES Guest(guest_id),
    FOREIGN KEY (staff_id)    REFERENCES Staff(staff_id),

    -- Ràng buộc: phải có ít nhất 1 trong account/guest, staff tạo hộ thì phải có guest hoặc account
    CHECK (
        (staff_id IS NULL AND account_id IS NOT NULL AND guest_id IS NULL)
        OR (staff_id IS NOT NULL AND guest_id IS NOT NULL)
        OR (staff_id IS NOT NULL AND account_id IS NOT NULL)
    )
);
GO

-- ============================================================================
-- 11. BOOKING SLOT (Dùng start_time/end_time thay TimeSlot cố định)
-- ============================================================================

CREATE TABLE BookingSlot (
    booking_slot_id INT IDENTITY PRIMARY KEY,
    booking_id      INT NOT NULL,
    court_id        INT NOT NULL,
    booking_date    DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    price_snapshot  DECIMAL(12,2) NOT NULL,   -- giá tại thời điểm đặt

    slot_status     VARCHAR(20)
        CHECK (slot_status IN ('PENDING','CHECKED_IN','CHECKED_OUT','NO_SHOW','CANCELLED'))
        DEFAULT 'PENDING',
    checkin_time    DATETIME NULL,
    checkout_time   DATETIME NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (court_id)   REFERENCES Court(court_id),
    CHECK (end_time > start_time)
);
GO

-- ============================================================================
-- 12. COURT SLOT BOOKING (Anti-overlap / Lock table)
-- ============================================================================

CREATE TABLE CourtSlotBooking (
    id              INT IDENTITY PRIMARY KEY,
    court_id        INT NOT NULL,
    booking_date    DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    booking_slot_id INT NOT NULL,

    FOREIGN KEY (court_id)        REFERENCES Court(court_id),
    FOREIGN KEY (booking_slot_id) REFERENCES BookingSlot(booking_slot_id),

    -- Một sân, một ngày, một start_time chỉ có 1 booking
    UNIQUE (court_id, booking_date, start_time)
);
GO

-- ============================================================================
-- 13. BOOKING CHANGE LOG
-- ============================================================================

CREATE TABLE BookingChangeLog (
    change_id        INT IDENTITY PRIMARY KEY,
    booking_id       INT NOT NULL,

    old_court_id     INT NULL,
    new_court_id     INT NULL,

    old_start_time   TIME NULL,
    new_start_time   TIME NULL,
    old_end_time     TIME NULL,
    new_end_time     TIME NULL,

    old_booking_date DATE NULL,
    new_booking_date DATE NULL,

    change_type      VARCHAR(20)
        CHECK (change_type IN ('CHANGE_COURT','CHANGE_TIME','CHANGE_DATE','CHANGE_MULTIPLE')),

    change_time      DATETIME DEFAULT GETDATE(),
    note             NVARCHAR(255),

    actor_staff_id   INT NULL,
    change_action    VARCHAR(30) NULL,
    before_data      NVARCHAR(MAX) NULL,
    after_data       NVARCHAR(MAX) NULL,
    reason           NVARCHAR(500) NULL,
    etag_before      VARCHAR(64) NULL,
    etag_after       VARCHAR(64) NULL,
    refund_due       DECIMAL(12,2) NULL,

    FOREIGN KEY (booking_id)    REFERENCES Booking(booking_id),
    FOREIGN KEY (actor_staff_id) REFERENCES Staff(staff_id)
);
GO

-- ============================================================================
-- 14. ORDER ITEM (Sản phẩm/dịch vụ gắn vào Booking — thay RacketRental)
-- ============================================================================

CREATE TABLE OrderItem (
    order_item_id       INT IDENTITY PRIMARY KEY,
    booking_id          INT NOT NULL,
    product_id          INT NOT NULL,
    quantity            INT NOT NULL CHECK (quantity > 0),
    unit_price_snapshot DECIMAL(12,2) NOT NULL,   -- giá tại thời điểm mua
    rental_duration     INT NULL,                 -- phút (chỉ dùng cho product_type = RENTAL)
    total_amount        DECIMAL(12,2) NOT NULL,   -- = unit_price × quantity (× duration factor nếu rental)
    added_by            VARCHAR(10)
        CHECK (added_by IN ('CUSTOMER','STAFF')),
    created_at          DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);
GO

-- ============================================================================
-- 15. VOUCHER (Dual system: PLATFORM vs OWNER)
-- ============================================================================

CREATE TABLE Voucher (
    voucher_id          INT IDENTITY(1,1) PRIMARY KEY,
    code                NVARCHAR(50) COLLATE SQL_Latin1_General_CP1_CS_AS UNIQUE NOT NULL,
    name                NVARCHAR(255) NOT NULL,
    description         NVARCHAR(500) NULL,

    -- Ai tạo? Ai chịu phí?
    issuer_type         VARCHAR(10)
        CHECK (issuer_type IN ('PLATFORM','OWNER')) NOT NULL,
    issuer_account_id   INT NULL,   -- nếu OWNER → FK Account(OWNER); nếu PLATFORM → NULL

    discount_type       VARCHAR(20)
        CHECK (discount_type IN ('PERCENTAGE','FIXED_AMOUNT')) NOT NULL,
    discount_value      DECIMAL(12,2) NOT NULL,
    min_order_amount    DECIMAL(12,2) DEFAULT 0,
    max_discount_amount DECIMAL(12,2) NULL,

    valid_from          DATETIME NOT NULL,
    valid_to            DATETIME NOT NULL,

    usage_limit         INT NULL,           -- tổng lần dùng (NULL = vô hạn)
    per_user_limit      INT DEFAULT 1,

    applicable_to       VARCHAR(20)
        CHECK (applicable_to IN ('COURT_BOOKING','PRODUCT','ALL'))
        DEFAULT 'ALL',

    is_active           BIT DEFAULT 1,
    created_at          DATETIME DEFAULT GETDATE(),
    updated_at          DATETIME NULL,

    FOREIGN KEY (issuer_account_id) REFERENCES Account(account_id)
);
GO

CREATE UNIQUE INDEX UX_Voucher_Code ON Voucher(code);
CREATE INDEX IX_Voucher_ValidPeriod ON Voucher(valid_from, valid_to);
GO

-- Scope: voucher chỉ áp dụng cho facility cụ thể (nếu không có record → toàn bộ)
CREATE TABLE VoucherFacility (
    voucher_id  INT NOT NULL,
    facility_id INT NOT NULL,
    PRIMARY KEY (voucher_id, facility_id),
    FOREIGN KEY (voucher_id)  REFERENCES Voucher(voucher_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

-- Scope: voucher chỉ áp dụng cho account cụ thể (nếu không có record → toàn bộ user)
CREATE TABLE VoucherAccount (
    voucher_id INT NOT NULL,
    account_id INT NOT NULL,
    PRIMARY KEY (voucher_id, account_id),
    FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE
);
GO

-- ============================================================================
-- 16. INVOICE (Hoá đơn — tách court vs product)
-- ============================================================================

CREATE TABLE Invoice (
    invoice_id      INT IDENTITY PRIMARY KEY,
    booking_id      INT UNIQUE NOT NULL,

    court_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,    -- tổng tiền sân
    product_amount  DECIMAL(12,2) NOT NULL DEFAULT 0,    -- tổng tiền sản phẩm/dịch vụ
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,    -- = court_amount + product_amount
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,    -- tiền giảm giá voucher
    total_amount    DECIMAL(12,2) NOT NULL,               -- = subtotal - discount_amount

    voucher_id      INT NULL,

    paid_amount     DECIMAL(12,2) DEFAULT 0,
    deposit_percent INT DEFAULT 100,

    payment_status  VARCHAR(20)
        CHECK (payment_status IN ('UNPAID','PARTIAL','PAID'))
        DEFAULT 'UNPAID',

    refund_due      DECIMAL(12,2) NOT NULL DEFAULT 0,
    refund_status   VARCHAR(20)
        CHECK (refund_status IN ('NONE','PENDING_MANUAL','REFUNDED'))
        DEFAULT 'NONE',
    refund_note     NVARCHAR(500) NULL,

    created_at      DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id),
    CHECK (deposit_percent BETWEEN 0 AND 100)
);
GO

-- ============================================================================
-- 17. VOUCHER USAGE (Tracking — thêm liability_party)
-- ============================================================================

CREATE TABLE VoucherUsage (
    usage_id        INT IDENTITY(1,1) PRIMARY KEY,
    voucher_id      INT NOT NULL,
    account_id      INT NULL,
    booking_id      INT NOT NULL,
    invoice_id      INT NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    liability_party VARCHAR(10)
        CHECK (liability_party IN ('PLATFORM','OWNER')) NOT NULL,
    used_at         DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id),
    FOREIGN KEY (account_id) REFERENCES Account(account_id),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id)
);
GO

-- ============================================================================
-- 18. PAYMENT
-- ============================================================================

CREATE TABLE Payment (
    payment_id         INT IDENTITY PRIMARY KEY,
    invoice_id         INT NOT NULL,

    vnpay_txn_no       NVARCHAR(100) NULL,
    vnpay_response_code VARCHAR(10) NULL,
    expire_at          DATETIME NULL,
    created_at         DATETIME DEFAULT GETDATE(),

    transaction_code   NVARCHAR(100),
    paid_amount        DECIMAL(12,2),
    payment_time       DATETIME,

    payment_type       VARCHAR(20)
        CHECK (payment_type IN ('DEPOSIT','REMAINING','FULL')),

    method             VARCHAR(20)
        CHECK (method IN ('VNPAY','CASH','BANK_TRANSFER')),

    payment_status     VARCHAR(20)
        CHECK (payment_status IN ('SUCCESS','FAILED','PENDING')),

    staff_confirm_id   INT NULL,
    confirm_time       DATETIME NULL,

    FOREIGN KEY (invoice_id)       REFERENCES Invoice(invoice_id),
    FOREIGN KEY (staff_confirm_id) REFERENCES Staff(staff_id)
);
GO

-- ============================================================================
-- 19. COMMISSION POLICY (Cấu hình chính sách hoa hồng toàn sàn)
-- ============================================================================
-- Quy định thời gian thông báo tối thiểu trước khi thay đổi có hiệu lực.
-- Tham khảo: Grab thông báo 30 ngày, Shopee 14 ngày trước khi đổi commission.

CREATE TABLE CommissionPolicy (
    policy_id          INT IDENTITY PRIMARY KEY,
    min_notice_days    INT NOT NULL DEFAULT 14,    -- số ngày thông báo tối thiểu trước khi hiệu lực
    -- Ví dụ: 14 = phải thông báo ít nhất 14 ngày trước effective_from
    description        NVARCHAR(500) NULL,
    updated_by         INT NULL,                   -- Admin cập nhật
    updated_at         DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (updated_by) REFERENCES Account(account_id),
    CHECK (min_notice_days >= 0)
);
GO

-- ============================================================================
-- 20. COMMISSION TIER (Bậc hoa hồng — Versioning SCD Type 2)
-- ============================================================================
-- Hoa hồng chỉ tính trên tiền SÂN, không tính sản phẩm/dịch vụ.
-- Bậc hoa hồng xác định bằng: price_per_minute = price_per_slot / slot_step_minutes
-- Ví dụ: slot 30p giá 50,000đ → 50,000/30 = 1,667đ/phút → rơi vào bậc 1,000–2,000đ/phút
--
-- VERSIONING: Không bao giờ UPDATE/DELETE bản ghi cũ.
-- Khi Admin thay đổi rate:
--   1. Bản ghi hiện tại giữ nguyên (effective_to = ngày hiệu lực mới)
--   2. INSERT bản ghi mới với effective_from = ngày hiệu lực mới
--   3. effective_from phải >= GETDATE() + min_notice_days (từ CommissionPolicy)
--   4. Gửi notification cho tất cả Owner bị ảnh hưởng

CREATE TABLE CommissionTier (
    tier_id              INT IDENTITY PRIMARY KEY,

    -- Khoảng giá/phút áp dụng
    min_price_per_minute DECIMAL(12,2) NOT NULL,    -- giá/phút tối thiểu (inclusive)
    max_price_per_minute DECIMAL(12,2) NULL,         -- giá/phút tối đa (exclusive). NULL = không giới hạn

    -- Tỷ lệ hoa hồng
    commission_rate      DECIMAL(5,4) NOT NULL,      -- vd: 0.0500 = 5%

    -- Versioning (SCD Type 2): thời gian hiệu lực
    effective_from       DATETIME NOT NULL,           -- ngày bắt đầu có hiệu lực
    effective_to         DATETIME NULL,               -- ngày hết hiệu lực. NULL = đang hiệu lực (vô thời hạn)
    is_current           BIT NOT NULL DEFAULT 1,      -- flag nhanh: bản ghi đang active không?

    -- Trạng thái workflow
    status               VARCHAR(20)
        CHECK (status IN ('DRAFT','ANNOUNCED','ACTIVE','EXPIRED'))
        DEFAULT 'DRAFT',
    -- DRAFT     : Admin tạo nháp, chưa công bố
    -- ANNOUNCED : Đã thông báo cho Owner, chờ effective_from
    -- ACTIVE    : Đang áp dụng (effective_from <= NOW < effective_to)
    -- EXPIRED   : Đã hết hiệu lực (bị thay thế bởi version mới)

    -- Thông báo
    announced_at         DATETIME NULL,               -- ngày thông báo cho Owner
    notice_days          INT NULL,                    -- số ngày thông báo thực tế = effective_from - announced_at

    description          NVARCHAR(500) NULL,
    created_by           INT NULL,                    -- Admin tạo
    created_at           DATETIME DEFAULT GETDATE(),
    updated_at           DATETIME NULL,

    FOREIGN KEY (created_by) REFERENCES Account(account_id),
    CHECK (min_price_per_minute >= 0),
    CHECK (max_price_per_minute IS NULL OR max_price_per_minute > min_price_per_minute),
    CHECK (effective_to IS NULL OR effective_to > effective_from)
);
GO

-- ============================================================================
-- 21. COMMISSION CHANGE LOG (Lịch sử thay đổi hoa hồng — Audit trail)
-- ============================================================================
-- Mỗi khi Admin tạo/sửa/thay thế CommissionTier → ghi log đầy đủ.
-- Owner có thể xem lịch sử thay đổi để quyết định có ở lại sàn hay không.

CREATE TABLE CommissionChangeLog (
    change_log_id     INT IDENTITY PRIMARY KEY,

    -- Bản ghi cũ bị thay thế (NULL nếu là tạo mới)
    old_tier_id       INT NULL,
    old_rate          DECIMAL(5,4) NULL,

    -- Bản ghi mới
    new_tier_id       INT NOT NULL,
    new_rate          DECIMAL(5,4) NOT NULL,

    -- Khoảng giá áp dụng
    min_price_per_minute DECIMAL(12,2) NOT NULL,
    max_price_per_minute DECIMAL(12,2) NULL,

    -- Thời gian
    effective_from    DATETIME NOT NULL,              -- ngày hiệu lực của rate mới
    announced_at      DATETIME NOT NULL,              -- ngày thông báo
    notice_days       INT NOT NULL,                   -- = effective_from - announced_at (ngày)

    change_type       VARCHAR(20)
        CHECK (change_type IN ('CREATE','UPDATE','EXPIRE')) NOT NULL,
    -- CREATE : Tạo tier mới
    -- UPDATE : Thay đổi rate (expire cũ + tạo mới)
    -- EXPIRE : Xoá/vô hiệu hoá tier

    reason            NVARCHAR(500) NULL,              -- lý do thay đổi
    changed_by        INT NOT NULL,                    -- Admin thực hiện
    created_at        DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (old_tier_id) REFERENCES CommissionTier(tier_id),
    FOREIGN KEY (new_tier_id) REFERENCES CommissionTier(tier_id),
    FOREIGN KEY (changed_by)  REFERENCES Account(account_id)
);
GO

-- ============================================================================
-- 22. PLATFORM COMMISSION (Hoa hồng sàn — chỉ tính trên tiền sân)
-- ============================================================================
-- Khi customer thanh toán, chỉ cần trả full tiền sân trước.
-- Tiền sản phẩm/dịch vụ thanh toán riêng, không tính hoa hồng.
-- commission_rate snapshot từ CommissionTier tại thời điểm booking → immutable.

CREATE TABLE PlatformCommission (
    commission_id         INT IDENTITY PRIMARY KEY,
    invoice_id            INT NOT NULL,
    owner_account_id      INT NOT NULL,

    court_revenue         DECIMAL(12,2) NOT NULL,    -- = Invoice.court_amount (chỉ tiền sân)
    commission_tier_id    INT NULL,                   -- FK → CommissionTier (bậc áp dụng tại thời điểm booking)
    commission_rate       DECIMAL(5,4) NOT NULL,      -- SNAPSHOT tỷ lệ tại thời điểm tính (không đổi khi tier update)
    commission_amount     DECIMAL(12,2) NOT NULL,     -- = court_revenue × commission_rate

    voucher_cost_owner    DECIMAL(12,2) DEFAULT 0,    -- phí voucher Owner tự tạo → Owner chịu
    voucher_cost_platform DECIMAL(12,2) DEFAULT 0,    -- phí voucher Admin tạo → Platform chịu

    owner_payout          DECIMAL(12,2) NOT NULL,     -- = court_revenue - commission_amount - voucher_cost_owner

    status                VARCHAR(20)
        CHECK (status IN ('PENDING','SETTLED','FAILED'))
        DEFAULT 'PENDING',
    settled_at            DATETIME NULL,
    created_at            DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (invoice_id)          REFERENCES Invoice(invoice_id),
    FOREIGN KEY (owner_account_id)    REFERENCES Account(account_id),
    FOREIGN KEY (commission_tier_id)  REFERENCES CommissionTier(tier_id)
);
GO

-- ============================================================================
-- 20. COURT SCHEDULE EXCEPTION (Bảo trì, sự kiện...)
-- ============================================================================

CREATE TABLE CourtScheduleException (
    exception_id   INT IDENTITY(1,1) PRIMARY KEY,
    court_id       INT NOT NULL,
    facility_id    INT NOT NULL,

    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,

    start_time     TIME NULL,        -- NULL = đóng cả ngày
    end_time       TIME NULL,

    exception_type VARCHAR(20)
        CHECK (exception_type IN ('MAINTENANCE','EVENT','PRIVATE_USE','OTHER')),
    reason         NVARCHAR(300) NULL,

    created_by     INT NULL,
    created_at     DATETIME DEFAULT GETDATE(),
    updated_at     DATETIME NULL,
    is_active      BIT DEFAULT 1,

    FOREIGN KEY (court_id)    REFERENCES Court(court_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (created_by)  REFERENCES Staff(staff_id),

    CONSTRAINT CK_Exception_Dates CHECK (end_date >= start_date),
    CONSTRAINT CK_Exception_Times CHECK (
        (start_time IS NULL AND end_time IS NULL)
        OR
        (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
    )
);
GO

-- ============================================================================
-- 21. REVIEW
-- ============================================================================

CREATE TABLE Review (
    review_id  INT IDENTITY PRIMARY KEY,
    booking_id INT UNIQUE NOT NULL,
    account_id INT NOT NULL,
    rating     INT CHECK (rating BETWEEN 1 AND 5),
    comment    NVARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO

-- ============================================================================
-- 22. NOTIFICATION
-- ============================================================================

CREATE TABLE Notification (
    notification_id INT IDENTITY PRIMARY KEY,
    account_id      INT NOT NULL,
    title           NVARCHAR(255),
    content         NVARCHAR(500),
    type            VARCHAR(10) CHECK (type IN ('EMAIL','SYSTEM')),
    is_read         BIT DEFAULT 0,
    is_sent         BIT DEFAULT 0,
    created_at      DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO

-- ============================================================================
-- 23. EMAIL QUEUE
-- ============================================================================

CREATE TABLE EmailQueue (
    email_id      INT IDENTITY PRIMARY KEY,
    email_type    VARCHAR(30) NOT NULL
        CHECK (email_type IN (
            'CREATE','UPDATE','CANCEL',
            'REMINDER_UPCOMING_24H','REMINDER_UPCOMING_2H','REMINDER_PAYMENT_12H',
            'PAY_SUCCESS','PAY_REMAINING','REMINDER_CUS_24H',
            'OWNER_APPROVED','FACILITY_APPROVED'
        )),
    booking_id    INT NULL,       -- NULL cho email không liên quan booking (owner approval...)
    to_email      NVARCHAR(255) NOT NULL,
    payload_json  NVARCHAR(MAX) NULL,
    reminder_at   DATETIME NULL,
    status        VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','SENDING','SENT','FAILED'))
        DEFAULT 'PENDING',
    retry_count   INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL DEFAULT GETDATE(),
    last_error    NVARCHAR(500) NULL,
    created_at    DATETIME NOT NULL DEFAULT GETDATE(),
    sent_at       DATETIME NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);
GO

-- ============================================================================
-- 24. CUSTOMER FAVORITE FACILITY
-- ============================================================================

CREATE TABLE CustomerFavoriteFacility (
    favorite_id INT IDENTITY PRIMARY KEY,
    account_id  INT NOT NULL,
    facility_id INT NOT NULL,

    FOREIGN KEY (account_id)  REFERENCES Account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE,
    UNIQUE (account_id, facility_id)
);
GO

-- ============================================================================
-- 25. BLOG & COMMENT
-- ============================================================================

CREATE TABLE BlogPost (
    post_id           INT IDENTITY(1,1) PRIMARY KEY,
    author_account_id INT NOT NULL,
    title             NVARCHAR(200) NOT NULL,
    summary           NVARCHAR(500) NULL,
    content           NVARCHAR(MAX) NOT NULL,
    status            VARCHAR(20) NOT NULL
        CHECK (status IN ('PUBLISHED','DRAFT')),
    published_at      DATETIME NULL,
    created_at        DATETIME DEFAULT GETDATE(),
    updated_at        DATETIME NULL,
    is_deleted        BIT DEFAULT 0,

    FOREIGN KEY (author_account_id) REFERENCES Account(account_id)
);
GO

CREATE TABLE BlogComment (
    comment_id             INT IDENTITY(1,1) PRIMARY KEY,
    post_id                INT NOT NULL,
    author_account_id      INT NOT NULL,
    content                NVARCHAR(1000) NOT NULL,
    status                 VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    moderated_by_account_id INT NULL,
    moderated_at           DATETIME NULL,
    created_at             DATETIME DEFAULT GETDATE(),
    updated_at             DATETIME NULL,
    is_deleted             BIT DEFAULT 0,

    FOREIGN KEY (post_id)                REFERENCES BlogPost(post_id) ON DELETE CASCADE,
    FOREIGN KEY (author_account_id)      REFERENCES Account(account_id),
    FOREIGN KEY (moderated_by_account_id) REFERENCES Account(account_id)
);
GO

CREATE TABLE BlogReaction (
    reaction_id INT IDENTITY(1,1) PRIMARY KEY,
    post_id     INT NOT NULL,
    account_id  INT NOT NULL,
    emoji_code  VARCHAR(30) NOT NULL
        CHECK (emoji_code IN ('LIKE','HEART','WOW','SAD','ANGRY','LAUGH')),
    created_at  DATETIME DEFAULT GETDATE(),

    CONSTRAINT UQ_BlogReaction UNIQUE (post_id, account_id, emoji_code),

    FOREIGN KEY (post_id)    REFERENCES BlogPost(post_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE
);
GO

-- ============================================================================
-- 26. INDEXES — Performance
-- ============================================================================

-- === FACILITY ===
CREATE NONCLUSTERED INDEX IX_Facility_Owner
    ON Facility(owner_account_id)
    INCLUDE (facility_id, name, approval_status, is_active);

CREATE NONCLUSTERED INDEX IX_Facility_ApprovalStatus
    ON Facility(approval_status)
    INCLUDE (facility_id, owner_account_id, name);

-- === FACILITY SPORT ===
CREATE NONCLUSTERED INDEX IX_FacilitySport_Facility
    ON FacilitySport(facility_id)
    INCLUDE (facility_sport_id, sport_id, slot_step_minutes, min_duration_minutes);

CREATE NONCLUSTERED INDEX IX_FacilitySport_Sport
    ON FacilitySport(sport_id)
    INCLUDE (facility_sport_id, facility_id);

-- === COURT ===
CREATE NONCLUSTERED INDEX IX_Court_FacilitySport
    ON Court(facility_sport_id)
    INCLUDE (court_id, court_name, is_active);

-- === FACILITY PRICE RULE ===
CREATE NONCLUSTERED INDEX IX_FacilityPriceRule_Main
    ON FacilityPriceRule(facility_sport_id, day_type)
    INCLUDE (start_time, end_time, price_per_slot, effective_from, effective_to);

-- === BOOKING ===
CREATE NONCLUSTERED INDEX IX_Booking_Account_Status
    ON Booking(account_id, booking_status)
    INCLUDE (booking_id, facility_id, created_at);

CREATE NONCLUSTERED INDEX IX_Booking_Facility_Status
    ON Booking(facility_id, booking_status)
    INCLUDE (account_id, guest_id, staff_id);

CREATE NONCLUSTERED INDEX IX_Booking_Staff
    ON Booking(staff_id)
    INCLUDE (booking_id, booking_status);

CREATE NONCLUSTERED INDEX IX_Booking_Status_HoldExpired
    ON Booking(booking_status, hold_expired_at)
    INCLUDE (booking_id);

-- === BOOKING SLOT ===
CREATE NONCLUSTERED INDEX IX_BookingSlot_Court_Date
    ON BookingSlot(court_id, booking_date, start_time, end_time)
    INCLUDE (booking_id, slot_status, price_snapshot);

CREATE NONCLUSTERED INDEX IX_BookingSlot_Date_Status
    ON BookingSlot(booking_date, court_id, slot_status)
    INCLUDE (booking_id, start_time, end_time, price_snapshot);

CREATE NONCLUSTERED INDEX IX_BookingSlot_BookingId
    ON BookingSlot(booking_id)
    INCLUDE (booking_slot_id, court_id, booking_date, start_time, end_time);

-- === COURT SLOT BOOKING (Anti-overlap) ===
CREATE NONCLUSTERED INDEX IX_CourtSlotBooking_DateRange
    ON CourtSlotBooking(court_id, booking_date, start_time, end_time)
    INCLUDE (booking_slot_id);

CREATE NONCLUSTERED INDEX IX_CourtSlotBooking_BookingSlotId
    ON CourtSlotBooking(booking_slot_id);

-- === PRODUCT ===
CREATE NONCLUSTERED INDEX IX_Product_Facility
    ON Product(facility_id, category_id)
    INCLUDE (product_name, product_type, price, is_active);

-- === VOUCHER ===
CREATE INDEX IX_VoucherUsage_Voucher ON VoucherUsage(voucher_id);
CREATE INDEX IX_VoucherUsage_Account ON VoucherUsage(account_id);

-- === COMMISSION ===
CREATE NONCLUSTERED INDEX IX_PlatformCommission_Owner
    ON PlatformCommission(owner_account_id, status)
    INCLUDE (invoice_id, court_revenue, commission_amount, owner_payout);

GO