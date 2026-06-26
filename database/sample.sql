-- ============================================================================
-- 27. SEED DATA — Sports, Attributes, Product Categories
-- ============================================================================

-- === SPORTS ===
INSERT INTO Sport (sport_code, sport_name, default_min_duration_minutes, default_slot_step_minutes)
VALUES
    ('BADMINTON',   N'Cầu lông',    30, 30),
    ('FOOTBALL',    N'Bóng đá',     60, 60),
    ('PICKLEBALL',  N'Pickleball',   30, 30),
    ('TENNIS',      N'Tennis',       60, 60),
    ('BASKETBALL',  N'Bóng rổ',     60, 60),
    ('VOLLEYBALL',  N'Bóng chuyền', 60, 60),
    ('TABLE_TENNIS',N'Bóng bàn',    30, 30);
GO

-- === SPORT ATTRIBUTES ===
-- Cầu lông
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'BADMINTON'),
        'COURT_SURFACE', N'Bề mặt sân', 'SELECT', N'["Sàn gỗ","Sàn nhựa PVC","Sàn vinyl","Sàn bê tông"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'BADMINTON'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor","Semi-outdoor"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'BADMINTON'),
        'HAS_LIGHTING', N'Có đèn chiếu sáng', 'BOOLEAN', NULL, 0);

-- Bóng đá
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'FOOTBALL'),
        'FIELD_SIZE', N'Quy mô sân', 'SELECT', N'["5 người","7 người","11 người"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'FOOTBALL'),
        'SURFACE_TYPE', N'Bề mặt sân', 'SELECT', N'["Cỏ nhân tạo","Cỏ tự nhiên","Sân cứng"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'FOOTBALL'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'FOOTBALL'),
        'HAS_LIGHTING', N'Có đèn chiếu sáng', 'BOOLEAN', NULL, 0);

-- Pickleball
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'PICKLEBALL'),
        'COURT_SURFACE', N'Bề mặt sân', 'SELECT', N'["Sàn bê tông","Sàn nhựa","Sàn gỗ"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'PICKLEBALL'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1);

-- Tennis
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'TENNIS'),
        'COURT_SURFACE', N'Bề mặt sân', 'SELECT', N'["Sân cứng (Hard)","Sân đất nện (Clay)","Sân cỏ (Grass)","Sân thảm (Carpet)"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'TENNIS'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'TENNIS'),
        'HAS_LIGHTING', N'Có đèn chiếu sáng', 'BOOLEAN', NULL, 0);

-- Bóng rổ
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'BASKETBALL'),
        'COURT_TYPE', N'Loại sân', 'SELECT', N'["Full court","Half court"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'BASKETBALL'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1);

-- Bóng chuyền
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'VOLLEYBALL'),
        'COURT_SURFACE', N'Bề mặt sân', 'SELECT', N'["Sân cát","Sân cứng","Sân gỗ"]', 1),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'VOLLEYBALL'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1);

-- Bóng bàn
INSERT INTO SportAttribute (sport_id, attribute_code, attribute_name, data_type, options_json, is_required)
VALUES
    ((SELECT sport_id FROM Sport WHERE sport_code = 'TABLE_TENNIS'),
        'TABLE_BRAND', N'Hãng bàn', 'TEXT', NULL, 0),
    ((SELECT sport_id FROM Sport WHERE sport_code = 'TABLE_TENNIS'),
        'INDOOR_OUTDOOR', N'Trong nhà / Ngoài trời', 'SELECT', N'["Indoor","Outdoor"]', 1);
GO

-- === PRODUCT CATEGORIES ===
INSERT INTO ProductCategory (category_code, category_name)
VALUES
    ('BEVERAGE',         N'Đồ uống'),
    ('FOOD',             N'Đồ ăn'),
    ('SPORT_GOODS',      N'Dụng cụ thể thao (bán)'),
    ('EQUIPMENT_RENTAL', N'Thuê dụng cụ'),
    ('ACCESSORY',        N'Phụ kiện'),
    ('OTHER',            N'Khác');
GO

-- === COMMISSION POLICY ===
-- Cấu hình ban đầu: thông báo tối thiểu 14 ngày trước khi thay đổi hoa hồng
INSERT INTO CommissionPolicy (min_notice_days, description)
VALUES (14, N'Chính sách mặc định: thông báo Owner ít nhất 14 ngày trước khi thay đổi hoa hồng có hiệu lực');
GO

-- === COMMISSION TIERS ===
-- Xác định % hoa hồng dựa trên giá/phút của slot
-- price_per_minute = price_per_slot / slot_step_minutes
-- Ví dụ: slot 30p giá 50,000đ → 50,000/30 ≈ 1,667đ/phút → bậc 2 (5%)
-- Tất cả tier ban đầu đều ACTIVE với effective_from = ngày tạo database
INSERT INTO CommissionTier (min_price_per_minute, max_price_per_minute, commission_rate, effective_from, effective_to, is_current, status, announced_at, notice_days, description)
VALUES
    (0,       1000,   0.0300, GETDATE(), NULL, 1, 'ACTIVE', GETDATE(), 0, N'Bậc 1: Giá rẻ (0–1,000đ/phút) → Hoa hồng 3%'),
    (1000,    2000,   0.0500, GETDATE(), NULL, 1, 'ACTIVE', GETDATE(), 0, N'Bậc 2: Giá trung bình (1,000–2,000đ/phút) → Hoa hồng 5%'),
    (2000,    3500,   0.0700, GETDATE(), NULL, 1, 'ACTIVE', GETDATE(), 0, N'Bậc 3: Giá khá cao (2,000–3,500đ/phút) → Hoa hồng 7%'),
    (3500,    5000,   0.0800, GETDATE(), NULL, 1, 'ACTIVE', GETDATE(), 0, N'Bậc 4: Giá cao (3,500–5,000đ/phút) → Hoa hồng 8%'),
    (5000,    NULL,   0.1000, GETDATE(), NULL, 1, 'ACTIVE', GETDATE(), 0, N'Bậc 5: Giá premium (>5,000đ/phút) → Hoa hồng 10%');
GO

PRINT N'✅ Database [sport_booking_marketplace] created successfully!';
PRINT N'   - 31 tables (thêm CommissionPolicy, CommissionTier, CommissionChangeLog)';
PRINT N'   - 7 sports with attributes';
PRINT N'   - 6 product categories';
PRINT N'   - 5 commission tiers (3%→10%) với versioning SCD Type 2';
PRINT N'   - Commission policy: 14 ngày thông báo trước khi đổi rate';
PRINT N'   - Flexible slot system (owner-configurable)';
PRINT N'   - Dual voucher system (PLATFORM / OWNER)';
PRINT N'   - Commission chỉ tính trên tiền sân';
GO

