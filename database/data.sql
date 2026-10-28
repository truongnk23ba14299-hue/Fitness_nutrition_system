-- ====================================================================
-- FITNESS & NUTRITION MANAGEMENT SYSTEM - SEED DATA (PostgreSQL)
-- Dữ liệu mẫu ban đầu để test hệ thống
-- Mật khẩu mặc định cho tất cả user mẫu: 123456
-- Hash Bcrypt tương ứng: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ====================================================================

-- 1. TẠO CÁC VAI TRÒ (ROLES)
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên toàn quyền hệ thống'),
(2, 'ROLE_COACH', 'Huấn luyện viên cá nhân / Chuyên gia dinh dưỡng'),
(3, 'ROLE_CLIENT', 'Học viên / Thành viên sử dụng dịch vụ'),
(4, 'ROLE_RECEPTIONIST', 'Lễ tân quản lý lịch và check-in tại quầy')
ON CONFLICT (id) DO NOTHING;

-- 2. TẠO NGƯỜI DÙNG MẪU (USERS)
INSERT INTO users (id, email, password_hash, full_name, phone, gender, date_of_birth, status) VALUES
(1, 'admin@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Nguyễn Quản Trị', '0901000001', 'MALE', '1990-01-01', 'ACTIVE'),
(2, 'coach.alex@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Trần Hùng (Alex)', '0902000002', 'MALE', '1995-05-15', 'ACTIVE'),
(3, 'coach.sarah@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Lê Mai (Sarah)', '0902000003', 'FEMALE', '1997-08-20', 'ACTIVE'),
(4, 'receptionist.lisa@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Phạm Thu (Lisa)', '0903000004', 'FEMALE', '1999-03-10', 'ACTIVE'),
(5, 'client.john@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Hoàng Minh John', '0904000005', 'MALE', '2000-11-25', 'ACTIVE'),
(6, 'client.mary@fitness.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Đặng Thảo Mary', '0904000006', 'FEMALE', '2001-04-18', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 3. GÁN ROLE CHO TỪNG USER
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Admin
(2, 2), -- Coach Alex
(3, 2), -- Coach Sarah
(4, 4), -- Receptionist Lisa
(5, 3), -- Client John
(6, 3)  -- Client Mary
ON CONFLICT DO NOTHING;

-- 4. HỒ SƠ CHI TIẾT
-- Huấn luyện viên
INSERT INTO coaches (id, user_id, specialty, experience_years, bio, certifications, hourly_rate, rating_avg, rating_count) VALUES
(1, 2, 'Thể hình & Tăng cơ (Bodybuilding)', 5, 'HLV chuyên sâu về phát triển cơ bắp và kỹ thuật tập tạ chuẩn.', 'NASM-CPT, ISSA Nutritionist', 300000, 4.9, 15),
(2, 3, 'Giảm mỡ & Dinh dưỡng (Weight Loss & Pilates)', 4, 'Chuyên gia tư vấn chế độ ăn khoa học và tạo form dáng chuẩn cho nữ.', 'ACE Fitness, Precision Nutrition Level 1', 350000, 5.0, 22)
ON CONFLICT (id) DO NOTHING;

-- Lễ tân
INSERT INTO receptionists (id, user_id, shift_type, hire_date) VALUES
(1, 4, 'FULL_TIME', '2025-01-01')
ON CONFLICT (id) DO NOTHING;

-- Học viên
INSERT INTO clients (id, user_id, height_cm, current_weight_kg, target_weight_kg, fitness_goal, activity_level, health_notes) VALUES
(1, 5, 175.0, 78.5, 72.0, 'WEIGHT_LOSS', 'MODERATELY_ACTIVE', 'Từng đau nhẹ khớp gối khi chạy bộ lâu.'),
(2, 6, 160.0, 50.0, 54.0, 'MUSCLE_GAIN', 'LIGHTLY_ACTIVE', 'Không có bệnh lý nền, mong muốn săn chắc vùng eo và mông.')
ON CONFLICT (id) DO NOTHING;

-- 5. CÁC GÓI TẬP (MEMBERSHIP PACKAGES)
INSERT INTO membership_packages (id, name, description, duration_days, price, package_type, sessions_count, is_active) VALUES
(1, 'Gói Tự Do 1 Tháng (Basic)', 'Tập luyện không giới hạn khung giờ, sử dụng đầy đủ tiện ích phòng tập và phòng tắm.', 30, 600000, 'BASIC', 0, true),
(2, 'Gói VIP 3 Tháng', 'Tập luyện toàn thời gian, tặng 3 buổi đo InBody chi tiết và 1 buổi định hướng với PT.', 90, 1600000, 'VIP', 0, true),
(3, 'Gói PT 1-kèm-1 (12 Buổi)', '12 buổi tập cá nhân 1-kèm-1 cùng Huấn luyện viên, giáo án và thực đơn chuyên biệt.', 45, 3600000, 'PT_1ON1', 12, true),
(4, 'Gói Lớp Nhóm Yoga/Pilates (1 Tháng)', 'Tham gia tất cả các lớp Yoga và Pilates theo lịch trình hàng tuần.', 30, 850000, 'GROUP_CLASS', 0, true)
ON CONFLICT (id) DO NOTHING;

-- 6. THƯ VIỆN BÀI TẬP (EXERCISES)
INSERT INTO exercises (name, description, muscle_group, difficulty, equipment_needed, calories_burned_per_min) VALUES
('Barbell Bench Press', 'Nằm đẩy tạ đòn ngang ngực, phát triển cơ ngực lớn, tay sau và vai trước.', 'CHEST', 'INTERMEDIATE', 'Barbell, Bench', 8.5),
('Barbell Back Squat', 'Gánh tạ đòn ngồi xổm, bài tập vua phát triển đùi trước, đùi sau và mông.', 'LEGS', 'ADVANCED', 'Barbell, Squat Rack', 10.0),
('Lat Pulldown', 'Kéo xô cáp cao xuống trước ngực, phát triển độ rộng của lưng xô.', 'BACK', 'BEGINNER', 'Cable Machine', 7.0),
('Dumbbell Shoulder Press', 'Ngồi đẩy tạ đơn qua đầu, phát triển cơ vai tròn và khỏe mạnh.', 'SHOULDERS', 'INTERMEDIATE', 'Dumbbells, Bench', 7.5),
('Plank', 'Chống khuỷu tay giữ thăng bằng thân người, siết chặt cơ bụng và cơ lõi.', 'CORE', 'BEGINNER', 'Yoga Mat', 5.0),
('Treadmill Running (HIIT)', 'Chạy bộ biến tốc trên máy chạy giúp đốt mỡ nhanh và tăng sức bền tim mạch.', 'CARDIO', 'INTERMEDIATE', 'Treadmill', 12.0);

-- 7. THƯ VIỆN THỰC PHẨM (FOODS)
INSERT INTO foods (name, category, serving_size, calories, protein_g, carbs_g, fat_g, fiber_g) VALUES
('Ức gà hấp / áp chảo', 'Thịt & Hải sản', '100g', 165.0, 31.0, 0.0, 3.6, 0.0),
('Cơm gạo lứt nấu chín', 'Ngũ cốc & Tinh bột', '100g', 111.0, 2.6, 23.0, 0.9, 1.8),
('Trứng gà luộc (1 quả)', 'Trứng & Sữa', '50g', 78.0, 6.3, 0.6, 5.3, 0.0),
('Bông cải xanh luộc (Broccoli)', 'Rau củ', '100g', 35.0, 2.4, 7.0, 0.4, 2.6),
('Chuối tiêu chín (1 quả vừa)', 'Trái cây', '118g', 105.0, 1.3, 27.0, 0.3, 3.1),
('Sữa chua Hy Lạp không đường', 'Trứng & Sữa', '100g', 59.0, 10.0, 3.6, 0.4, 0.0);

-- 8. KHÁCH VÃNG LAI GỬI TƯ VẤN (CONSULTATION LEADS TỪ HOMEPAGE)
INSERT INTO consultation_leads (full_name, phone, email, fitness_goal, preferred_time, status, notes) VALUES
('Nguyễn Văn Tuấn', '0912345678', 'tuan.nguyen@gmail.com', 'Giảm 5kg trong 2 tháng', 'Buổi tối (18h-20h)', 'NEW', 'Khách vãng lai quan tâm gói PT 1-on-1 từ landing page'),
('Vũ Thị Lan', '0987654321', 'lan.vu@gmail.com', 'Tập Yoga giảm stress và cải thiện tư thế', 'Sáng sớm (6h-7h)', 'CONTACTED', 'Lễ tân đã gọi điện tư vấn lịch lớp');

-- Reset sequences để khi insert tự động không bị đụng id
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('coaches_id_seq', (SELECT MAX(id) FROM coaches));
SELECT setval('clients_id_seq', (SELECT MAX(id) FROM clients));
SELECT setval('receptionists_id_seq', (SELECT MAX(id) FROM receptionists));
SELECT setval('membership_packages_id_seq', (SELECT MAX(id) FROM membership_packages));
