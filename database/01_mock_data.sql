-- ==============================================================================
-- Kịch bản dữ liệu mẫu (Mock Data Script) cho SportHub
-- Mật khẩu mặc định cho tất cả các tài khoản là: password
-- Băm (Hash) BCrypt của "password" là: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ (Tùy chọn, comment lại nếu không muốn xóa)
-- TRUNCATE TABLE account;
-- TRUNCATE TABLE owner_profile;
-- TRUNCATE TABLE staff;
-- TRUNCATE TABLE facility;
-- TRUNCATE TABLE sport;
-- TRUNCATE TABLE facility_sport;
-- TRUNCATE TABLE court;
-- TRUNCATE TABLE facility_price_rule;
-- TRUNCATE TABLE facility_image;

-- ==============================================================================
-- 1. BẢNG ACCOUNT (Tạo 4 tài khoản với 4 Role khác nhau)
-- ==============================================================================
INSERT INTO account (account_id, email, password_hash, full_name, phone, role, is_active, created_at) VALUES 
(1, 'admin@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Hệ thống Quản Trị', '0901000001', 'ADMIN', 1, NOW()),
(2, 'owner@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Nguyễn Văn Chủ Sân', '0902000002', 'OWNER', 1, NOW()),
(3, 'staff@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Trần Thị Nhân Viên', '0903000003', 'STAFF', 1, NOW()),
(4, 'customer@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Lê Khách Hàng', '0904000004', 'CUSTOMER', 1, NOW());

-- ==============================================================================
-- 2. BẢNG OWNER_PROFILE (Hồ sơ cho Chủ sân - Account ID 2)
-- ==============================================================================
INSERT INTO owner_profile (owner_profile_id, account_id, business_name, tax_code, bank_name, bank_account_no, bank_account_name, approval_status, created_at, approved_at, approved_by) VALUES
(1, 2, 'Công ty TNHH SportHub Mẫu', '0123456789', 'Vietcombank', '10123456789', 'NGUYEN VAN CHU SAN', 'APPROVED', NOW(), NOW(), 1);

-- ==============================================================================
-- 3. BẢNG FACILITY (4 Cơ sở thể thao của Owner ID 2)
-- ==============================================================================
INSERT INTO facility (facility_id, owner_account_id, name, province, district, ward, address, latitude, longitude, description, open_time, close_time, approval_status, is_active, created_at, approved_at, approved_by) VALUES
(1, 2, 'SportHub Central - Tổ hợp Bóng đá & Tennis', 'Thành phố Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '123 Đường Cầu Giấy', 21.037000, 105.795000, 'Tổ hợp thể thao đa năng lớn nhất khu vực Cầu Giấy với sân bóng đá cỏ nhân tạo và sân Tennis chuẩn quốc tế.', '06:00:00', '22:00:00', 'APPROVED', 1, NOW(), NOW(), 1),
(2, 2, 'SportHub Badminton Arena', 'Thành phố Hà Nội', 'Quận Đống Đa', 'Phường Láng Hạ', '456 Đường Láng Hạ', 21.015000, 105.815000, 'Khu vực chuyên biệt dành cho những người yêu thích cầu lông với thảm tiêu chuẩn BWF.', '05:30:00', '23:00:00', 'APPROVED', 1, NOW(), NOW(), 1),
(3, 2, 'SportHub Swimming Complex', 'Thành phố Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '789 Đường Nguyễn Huệ', 10.774000, 106.703000, 'Hồ bơi trong nhà có mái che, chuẩn thi đấu Olympic dài 50m, 8 làn bơi.', '05:00:00', '20:00:00', 'APPROVED', 1, NOW(), NOW(), 1),
(4, 2, 'SportHub Premium Tennis', 'Thành phố Đà Nẵng', 'Quận Hải Châu', 'Phường Thạch Thang', '101 Đường Lê Duẩn', 16.074000, 108.216000, 'Hệ thống sân Tennis mặt cứng cao cấp, không gian mở thoáng mát, có bãi đỗ xe rộng rãi.', '06:00:00', '22:00:00', 'APPROVED', 1, NOW(), NOW(), 1);

-- ==============================================================================
-- 4. BẢNG STAFF (Gán Staff ID 3 quản lý Facility 1)
-- ==============================================================================
INSERT INTO staff (staff_id, account_id, facility_id, owner_account_id, is_active) VALUES
(1, 3, 1, 2, 1);

-- ==============================================================================
-- 5. BẢNG SPORT (Các môn thể thao hệ thống)
-- ==============================================================================
INSERT INTO sport (sport_id, sport_code, sport_name, icon_path, default_min_duration_minutes, default_slot_step_minutes, is_active) VALUES
(1, 'FB', 'Bóng đá (Football)', '/icons/football.png', 60, 30, 1),
(2, 'BM', 'Cầu lông (Badminton)', '/icons/badminton.png', 60, 30, 1),
(3, 'TN', 'Quần vợt (Tennis)', '/icons/tennis.png', 60, 30, 1),
(4, 'SW', 'Bơi lội (Swimming)', '/icons/swimming.png', 60, 60, 1);

-- ==============================================================================
-- 5.1. BẢNG SPORT_ATTRIBUTE (Đặc tả kĩ thuật cho từng môn)
-- ==============================================================================
INSERT INTO sport_attribute (attribute_id, sport_id, attribute_code, attribute_name, data_type, is_required, options_json, is_active) VALUES
-- Bóng đá (Sport ID 1)
(1, 1, 'pitch_type', 'Loại mặt sân', 'SELECT', 1, '["Cỏ tự nhiên", "Cỏ nhân tạo"]', 1),
(2, 1, 'max_players', 'Số người tối đa', 'NUMBER', 1, NULL, 1),
(3, 1, 'has_roof', 'Có mái che', 'BOOLEAN', 0, NULL, 1),
-- Cầu lông (Sport ID 2)
(4, 2, 'court_mat_type', 'Loại thảm (PVC/Gỗ)', 'TEXT', 1, NULL, 1),
(5, 2, 'ceiling_height', 'Chiều cao trần (m)', 'NUMBER', 1, NULL, 1),
-- Tennis (Sport ID 3)
(6, 3, 'surface_type', 'Mặt sân', 'SELECT', 1, '["Cứng", "Đất nện", "Cỏ"]', 1),
(7, 3, 'has_referee_chair', 'Có ghế trọng tài', 'BOOLEAN', 0, NULL, 1),
-- Bơi lội (Sport ID 4)
(8, 4, 'pool_length', 'Chiều dài hồ (m)', 'NUMBER', 1, NULL, 1),
(9, 4, 'max_depth', 'Độ sâu tối đa (m)', 'NUMBER', 1, NULL, 1),
(10, 4, 'water_temp_control', 'Có hệ thống gia nhiệt', 'BOOLEAN', 0, NULL, 1);

-- ==============================================================================
-- 6. BẢNG FACILITY_SPORT (Map Môn thể thao vào Cơ sở)
-- ==============================================================================
INSERT INTO facility_sport (facility_sport_id, facility_id, sport_id, min_duration_minutes, slot_step_minutes, is_active) VALUES
(1, 1, 1, 60, 30, 1), -- Facility 1 có Bóng đá
(2, 1, 3, 60, 30, 1), -- Facility 1 có Tennis
(3, 2, 2, 60, 30, 1), -- Facility 2 có Cầu lông
(4, 3, 4, 60, 60, 1), -- Facility 3 có Bơi lội
(5, 4, 3, 60, 30, 1); -- Facility 4 có Tennis

-- ==============================================================================
-- 7. BẢNG COURT (Sân nhỏ bên trong Facility Sport)
-- ==============================================================================
INSERT INTO court (court_id, facility_sport_id, court_name, description, is_active) VALUES
-- Facility 1 (Bóng đá) -> 2 sân
(1, 1, 'Sân bóng đá số 1 (7 người)', 'Sân cỏ nhân tạo cao cấp, thay mới năm 2024', 1),
(2, 1, 'Sân bóng đá số 2 (5 người)', 'Sân nhỏ dành cho 5 người, có mái che lưới', 1),
-- Facility 1 (Tennis) -> 1 sân
(3, 2, 'Sân Tennis VIP số 1', 'Mặt sân chuẩn quốc tế, đèn LED 1000W', 1),
-- Facility 2 (Cầu lông) -> 3 sân
(4, 3, 'Sân cầu lông số 1', 'Thảm Alite chính hãng', 1),
(5, 3, 'Sân cầu lông số 2', 'Nằm gần khu vực quầy nước', 1),
(6, 3, 'Sân cầu lông số 3', 'Khu vực riêng tư, ánh sáng tốt nhất', 1),
-- Facility 3 (Bơi lội) -> 1 hồ bơi (coi như 1 court)
(7, 4, 'Hồ bơi thi đấu Olympic', 'Dài 50m, rộng 25m, sâu 2m', 1),
-- Facility 4 (Tennis) -> 2 sân
(8, 5, 'Sân Tennis A', 'Mặt sân cứng, view toàn cảnh Đà Nẵng', 1),
(9, 5, 'Sân Tennis B', 'Khu vực yên tĩnh, có ghế VIP cho khán giả', 1);

-- ==============================================================================
-- 8. BẢNG FACILITY_PRICE_RULE (Khung giá cho từng môn thể thao của cơ sở)
-- DayType ENUM: 'HOLIDAY','WEEKDAY','WEEKEND'
-- ==============================================================================
INSERT INTO facility_price_rule (price_rule_id, facility_sport_id, day_type, start_time, end_time, price_per_slot, effective_from, effective_to, is_active, created_at) VALUES
-- Facility 1 (Bóng đá)
(1, 1, 'WEEKDAY', '06:00:00', '16:00:00', 300000.00, '2024-01-01', NULL, 1, NOW()),
(2, 1, 'WEEKDAY', '16:00:00', '22:00:00', 500000.00, '2024-01-01', NULL, 1, NOW()),
(3, 1, 'WEEKEND', '06:00:00', '22:00:00', 600000.00, '2024-01-01', NULL, 1, NOW()),
-- Facility 1 (Tennis)
(4, 2, 'WEEKDAY', '06:00:00', '22:00:00', 250000.00, '2024-01-01', NULL, 1, NOW()),
(5, 2, 'WEEKEND', '06:00:00', '22:00:00', 350000.00, '2024-01-01', NULL, 1, NOW()),
-- Facility 2 (Cầu lông)
(6, 3, 'WEEKDAY', '05:30:00', '17:00:00', 80000.00,  '2024-01-01', NULL, 1, NOW()),
(7, 3, 'WEEKDAY', '17:00:00', '23:00:00', 120000.00, '2024-01-01', NULL, 1, NOW()),
(8, 3, 'WEEKEND', '05:30:00', '23:00:00', 150000.00, '2024-01-01', NULL, 1, NOW()),
-- Facility 3 (Bơi lội)
(9, 4,  'WEEKDAY', '05:00:00', '20:00:00', 50000.00,  '2024-01-01', NULL, 1, NOW()),
(10, 4, 'WEEKEND', '05:00:00', '20:00:00', 70000.00,  '2024-01-01', NULL, 1, NOW()),
-- Facility 4 (Tennis)
(11, 5, 'WEEKDAY', '06:00:00', '22:00:00', 200000.00, '2024-01-01', NULL, 1, NOW()),
(12, 5, 'WEEKEND', '06:00:00', '22:00:00', 280000.00, '2024-01-01', NULL, 1, NOW());

-- ==============================================================================
-- 9. BẢNG FACILITY_IMAGE (Hình ảnh cho 4 Facility)
-- ==============================================================================
INSERT INTO facility_image (image_id, facility_id, image_path, is_thumbnail, created_at) VALUES
(1, 1, 'https://images.pexels.com/photos/114296/pexels-photo-114296.jpeg?auto=compress&cs=tinysrgb&w=1200', 1, NOW()),
(2, 1, 'https://images.unsplash.com/photo-1589487391730-58f20eb2c308?q=80&w=1200&auto=format&fit=crop', 0, NOW()),
(3, 2, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?q=80&w=1200&auto=format&fit=crop', 1, NOW()),
(4, 3, 'https://images.unsplash.com/photo-1576610616656-d3aa5d1f4534?q=80&w=1200&auto=format&fit=crop', 1, NOW()),
(5, 4, 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?q=80&w=1200&auto=format&fit=crop', 1, NOW());

-- ==============================================================================
-- 10. BẢNG COMMISSION_POLICY (Chính sách hoa hồng chung)
-- ==============================================================================
INSERT INTO commission_policy (policy_id, min_notice_days, description, updated_by, updated_at) VALUES
(1, 14, 'Chính sách thông báo thay đổi mức hoa hồng chung. Yêu cầu báo trước tối thiểu 14 ngày trước khi áp dụng biểu phí mới cho các đối tác chủ sân.', 1, NOW());

-- ==============================================================================
-- 11. BẢNG COMMISSION_TIER (Các bậc hoa hồng thu theo khung giá)
-- ==============================================================================
INSERT INTO commission_tier (tier_id, min_price_per_minute, max_price_per_minute, commission_rate, effective_from, effective_to, is_current, status, announced_at, notice_days, description, created_by, created_at, updated_at) VALUES
(1, 0, 1000, 0.05, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 0, 'EXPIRED', '2023-12-01 10:00:00', 30, 'Mức cũ cho khung giá thấp (< 1000 VNĐ/phút)', 1, '2023-11-01 10:00:00', NOW()),
(2, 0, 1000, 0.08, '2025-01-01 00:00:00', '2026-12-31 23:59:59', 1, 'ACTIVE', '2024-12-01 10:00:00', 31, 'Khung giá thấp áp dụng đến hết 2026', 1, '2024-11-01 10:00:00', NOW()),
(3, 1000.01, 3000, 0.10, '2025-01-01 00:00:00', '2026-12-31 23:59:59', 1, 'ACTIVE', '2024-12-01 10:00:00', 31, 'Khung giá trung bình áp dụng đến hết 2026', 1, '2024-11-01 10:00:00', NOW()),
(4, 3000.01, NULL, 0.15, '2025-01-01 00:00:00', NULL, 1, 'ACTIVE', '2024-12-01 10:00:00', 31, 'Khung giá cao (VIP)', 1, '2024-11-01 10:00:00', NOW()),
(5, 0, 1500, 0.09, '2027-01-01 00:00:00', NULL, 0, 'DRAFT', NULL, NULL, 'Bản nháp: Đề xuất mức giá mới thay thế cho khung thấp và trung bình từ 2027', 1, NOW(), NOW());
-- ==============================================================================
-- 12. BẢNG PRODUCT_CATEGORY (Danh mục hàng hóa)
-- ==============================================================================
INSERT INTO product_category (category_id, category_code, category_name, is_active) VALUES
(1, 'DRINK', 'Đồ uống & Giải khát', 1),
(2, 'EQUIPMENT', 'Dụng cụ & Phụ kiện Thể thao', 1),
(3, 'FOOD', 'Thức ăn nhẹ & Năng lượng', 1),
(4, 'RENTAL', 'Dịch vụ Cho thuê Giày / Vợt', 1),
(5, 'MEDICAL', 'Vật tư Y tế & Sơ cứu', 1);

SET FOREIGN_KEY_CHECKS = 1;
