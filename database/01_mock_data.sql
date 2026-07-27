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

SET FOREIGN_KEY_CHECKS = 1;
