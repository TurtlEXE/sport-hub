-- Bỏ qua khóa ngoại tạm thời để dễ Insert
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ (Tùy chọn)
TRUNCATE TABLE account;
TRUNCATE TABLE sport;
TRUNCATE TABLE product_category;
TRUNCATE TABLE facility;
TRUNCATE TABLE owner_profile;
TRUNCATE TABLE facility_image;
TRUNCATE TABLE facility_sport;
TRUNCATE TABLE facility_price_rule;
TRUNCATE TABLE product;

-- 1. Account
INSERT INTO account (email, password_hash, full_name, phone, role, is_active, created_at) VALUES 
('admin@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'System Admin', '0901234567', 'ADMIN', 1, NOW()),
('owner1@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Trần Văn Chủ', '0912345678', 'OWNER', 1, NOW()),
('staff1@sporthub.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Nguyễn Thị Nhân Viên', '0923456789', 'STAFF', 1, NOW()),
('customer1@gmail.com', '$2a$10$u621Hcmgvdn.N49BJMThPujuxDoskViPcBNArFwmkAbhi96AdlvVK', 'Lê Khách Hàng', '0934567890', 'CUSTOMER', 1, NOW());

-- 2. Sport
INSERT INTO sport (sport_code, sport_name, default_min_duration_minutes, default_slot_step_minutes, is_active) VALUES 
('FOOTBALL', 'Bóng Đá', 60, 30, 1),
('TENNIS', 'Tennis', 60, 30, 1),
('BADMINTON', 'Cầu Lông', 60, 30, 1);

-- 3. ProductCategory
INSERT INTO product_category (category_code, category_name, is_active) VALUES 
('THUE_DUNG_CU', 'Thuê dụng cụ', 1),
('NUOC_UONG', 'Nước giải khát', 1),
('DICH_VU_KHAC', 'Dịch vụ khác', 1);

-- 4. Facility
INSERT INTO facility (owner_account_id, name, address, province, district, ward, latitude, longitude, description, open_time, close_time, approval_status, is_active, created_at) VALUES 
(2, 'Sân Bóng Đá Cỏ Nhân Tạo Chảo Lửa', '30 Phan Thúc Duyện', 'Hồ Chí Minh', 'Tân Bình', 'Phường 4', 10.8016, 106.6575, 'Sân bóng cỏ nhân tạo chất lượng cao, có hệ thống chiếu sáng ban đêm đạt chuẩn.', '05:00:00', '23:00:00', 'APPROVED', 1, NOW()),
(2, 'Sân Tennis Quận 7', '123 Nguyễn Văn Linh', 'Hồ Chí Minh', 'Quận 7', 'Tân Thuận Tây', 10.7308, 106.7029, 'Cụm sân Tennis tiêu chuẩn quốc tế, mặt sân nhựa tổng hợp.', '06:00:00', '22:00:00', 'APPROVED', 1, NOW()),
(2, 'Sân Cầu Lông VStar', '45 Khu dân cư VStar', 'Hồ Chí Minh', 'Quận 7', 'Phú Thuận', 10.7410, 106.7321, 'Sân cầu lông thảm PVC chống trượt, không gian thoáng mát.', '07:00:00', '22:00:00', 'APPROVED', 1, NOW()),
(2, 'Tổ hợp Thể thao Thanh Niên', '12 Phạm Ngọc Thạch', 'Hồ Chí Minh', 'Quận 1', 'Bến Nghé', 10.7825, 106.6970, 'Khu tổ hợp thể thao cao cấp trung tâm thành phố.', '06:00:00', '23:00:00', 'APPROVED', 1, NOW()),
(2, 'Sân Bóng Minh Hải', '88 Lê Văn Việt', 'Hồ Chí Minh', 'Thành phố Thủ Đức', 'Tăng Nhơn Phú A', 10.8412, 106.7905, 'Sân banh sinh viên giá rẻ, mặt cỏ mới làm lại.', '05:00:00', '23:30:00', 'APPROVED', 1, NOW());

-- 5. OwnerProfile
INSERT INTO owner_profile (account_id, business_name, tax_code, bank_name, bank_account_no, bank_account_name, approval_status, approved_by, approved_at, created_at) VALUES 
(2, 'Công Ty TNHH Thể Thao Chảo Lửa', '0312345678', 'Vietcombank', '0123456789', 'TRAN VAN CHU', 'APPROVED', 1, NOW(), NOW());

-- 6. FacilityImage
INSERT INTO facility_image (facility_id, image_path, is_thumbnail, created_at) VALUES 
(1, 'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=1000&auto=format&fit=crop', 1, NOW()),
(2, 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?q=80&w=1000&auto=format&fit=crop', 1, NOW()),
(3, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?q=80&w=1000&auto=format&fit=crop', 1, NOW()),
(4, 'https://images.unsplash.com/photo-1518605368461-1e1252220a77?q=80&w=1000&auto=format&fit=crop', 1, NOW()),
(5, 'https://images.unsplash.com/photo-1459865264687-595d652de67e?q=80&w=1000&auto=format&fit=crop', 1, NOW());

-- 7. FacilitySport
INSERT INTO facility_sport (facility_id, sport_id, min_duration_minutes, slot_step_minutes, is_active) VALUES 
(1, 1, 60, 30, 1),
(2, 2, 60, 30, 1),
(3, 3, 60, 30, 1),
(4, 1, 90, 30, 1),
(4, 3, 60, 30, 1),
(5, 1, 60, 30, 1);

-- 8. FacilityPriceRule
INSERT INTO facility_price_rule (facility_sport_id, day_type, start_time, end_time, price_per_slot, effective_from, is_active, created_at) VALUES 
-- Sân bóng Chảo Lửa
(1, 'WEEKDAY', '05:00:00', '16:00:00', 150000, '2024-01-01', 1, NOW()),
(1, 'WEEKDAY', '16:00:00', '23:00:00', 250000, '2024-01-01', 1, NOW()),
(1, 'WEEKEND', '05:00:00', '23:00:00', 300000, '2024-01-01', 1, NOW()),
-- Sân Tennis Quận 7
(2, 'WEEKDAY', '06:00:00', '18:00:00', 200000, '2024-01-01', 1, NOW()),
(2, 'WEEKEND', '06:00:00', '22:00:00', 350000, '2024-01-01', 1, NOW()),
-- Sân Cầu Lông VStar
(3, 'WEEKDAY', '07:00:00', '17:00:00', 80000, '2024-01-01', 1, NOW()),
(3, 'WEEKDAY', '17:00:00', '22:00:00', 120000, '2024-01-01', 1, NOW()),
(3, 'WEEKEND', '07:00:00', '22:00:00', 150000, '2024-01-01', 1, NOW()),
-- Tổ hợp Thanh Niên (Bóng đá)
(4, 'WEEKDAY', '06:00:00', '23:00:00', 400000, '2024-01-01', 1, NOW()),
-- Tổ hợp Thanh Niên (Cầu lông)
(5, 'WEEKDAY', '06:00:00', '23:00:00', 200000, '2024-01-01', 1, NOW()),
-- Sân Bóng Minh Hải
(6, 'WEEKDAY', '05:00:00', '17:00:00', 100000, '2024-01-01', 1, NOW()),
(6, 'WEEKDAY', '17:00:00', '23:30:00', 180000, '2024-01-01', 1, NOW());

-- 9. Product
INSERT INTO product (facility_id, category_id, product_name, description, product_type, price, rental_unit, stock_quantity, is_active, created_at) VALUES 
(1, 1, 'Áo Pitch (Bibs)', 'Áo chiến thuật', 'RENTAL', 10000, 'Cái/Trận', 50, 1, NOW()),
(1, 1, 'Thuê bóng', 'Bóng FIFA Size 5', 'RENTAL', 30000, 'Quả/Trận', 20, 1, NOW()),
(1, 2, 'Nước suối', 'Chai 500ml', 'SALE', 10000, 'Chai', 100, 1, NOW()),
(2, 1, 'Thuê vợt Tennis', 'Vợt Wilson', 'RENTAL', 50000, 'Cây/Trận', 10, 1, NOW()),
(3, 2, 'Revive', 'Nước bù khoáng', 'SALE', 15000, 'Chai', 50, 1, NOW()),
(4, 2, 'Gatorade', 'Nước thể thao', 'SALE', 25000, 'Chai', 30, 1, NOW()),
(5, 1, 'Thuê bóng', 'Bóng Động Lực', 'RENTAL', 20000, 'Quả/Trận', 15, 1, NOW());

-- Bật lại khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;
