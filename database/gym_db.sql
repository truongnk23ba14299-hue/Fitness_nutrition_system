-- ====================================================================
-- DỰ ÁN: FITNESS & NUTRITION MANAGEMENT SYSTEM
-- CƠ CHẾ: Hibernate ddl-auto=update ĐÃ TỰ TẠO BẢNG
-- FILE NÀY CHỈ DÙNG ĐỂ NẠP DỮ LIỆU TEST (SEED DATA)
-- Mật khẩu mặc định của tất cả tài khoản bên dưới là: password123
-- ====================================================================

-- 1. Nạp Users mẫu (Mật khẩu mặc định là: password123)
INSERT INTO users (full_name, email, password_hash, role, phone, address, height_cm, weight_kg) VALUES
('Nguyễn Khắc Trường (Admin)', 'admin@gym.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFjKN.bpKjVuacOA0e1DqAup.pS1LgSm', 'ROLE_ADMIN', '0988111222', 'Hà Nội', 175.0, 72.0),
('Trần Văn Hoàng (PT)', 'pt_hoang@gym.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFjKN.bpKjVuacOA0e1DqAup.pS1LgSm', 'ROLE_TRAINER', '0912345678', 'Hồ Chí Minh', 180.0, 80.0),
('Lê Thị Mai (PT Yoga)', 'pt_mai@gym.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFjKN.bpKjVuacOA0e1DqAup.pS1LgSm', 'ROLE_TRAINER', '0922334455', 'Đà Nẵng', 165.0, 52.0),
('Cao Tuấn (Học viên)', 'caotuan@gmail.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFjKN.bpKjVuacOA0e1DqAup.pS1LgSm', 'ROLE_CLIENT', '0977889900', 'Hải Phòng', 170.0, 65.5)
ON CONFLICT (email) DO NOTHING;
-- 2. Nạp Khóa tập mẫu (Packages)
INSERT INTO packages (title, description, price, duration_months, image_url, benefits, schedule_details, is_active) VALUES
('Gói Gym Tiêu Chuẩn (1 Tháng)', 'Tập luyện không giới hạn khung giờ tất cả các ngày trong tuần tại phòng tập.', 500000.00, 1, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48', 'Miễn phí tủ đồ, nước uống, phòng tắm nóng lạnh', 'Từ 05:30 đến 21:30 hàng ngày', TRUE),
('Gói Khỏe Đẹp Toàn Diện (6 Tháng)', 'Bao gồm Gym + Lớp Yoga + Phòng xông hơi khô Sauna chuẩn quốc tế.', 2400000.00, 6, 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Tặng 2 buổi tập thử cùng PT chuyên nghiệp, tủ đồ VIP', 'Không giới hạn thời gian', TRUE),
('Gói Hội Viên VIP Thượng Hạng (12 Tháng)', 'Trải nghiệm đỉnh cao không giới hạn mọi dịch vụ trên toàn hệ thống phòng tập.', 4200000.00, 12, 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f', 'Tặng 5 buổi PT riêng, đo chỉ số InBody miễn phí hàng tháng', 'Full 24/7', TRUE);
-- 3. Nạp Thiết bị & Đồ dùng vào bảng EQUIPMENTS
INSERT INTO equipments (name, description, category, brand, price, stock_quantity, weight_kg, image_url) VALUES
('Sữa Tăng Cơ Whey Gold Standard 5Lbs', 'Bổ sung 24g Protein tinh khiết phục hồi cơ bắp nhanh chóng sau khi tập.', 'THỰC PHẨM BỔ SUNG', 'Optimum Nutrition', 1850000.00, 25, 2.27, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d'),
('Bình Lắc Tập Gym Shaker 700ml', 'Bình nhựa cao cấp không chứa BPA có lưới đánh tan bột siêu mịn.', 'DỤNG CỤ', 'GymPro', 120000.00, 100, 0.35, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd'),
('Găng Tay Tập Gym Có Cuốn Cổ Tay Harbinger', 'Bảo vệ lòng bàn tay chống chai sạn, tăng độ bám khi nâng tạ nặng.', 'PHỤ KIỆN', 'Harbinger', 350000.00, 40, 0.20, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61');
-- 4. Nạp Huấn luyện viên mẫu (Trainers)
INSERT INTO trainers (full_name, avatar_url, bio, experience_years, specialty, rating, is_active) VALUES
('Trần Văn Hoàng', 'https://images.unsplash.com/photo-1567013127542-490d757e51fc', 'Chứng chỉ HLV Thể hình Quốc tế NASM, từng đoạt HCB Thể hình Quốc gia 2022.', 5, 'Tăng cơ, Siết mỡ, Boxing cường độ cao', 4.90, TRUE),
('Lê Thị Mai', 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Bậc thầy Yoga 200 giờ tại Ấn Độ, chuyên trị liệu xương khớp và thon gọn vóc dáng.', 4, 'Yoga Trị Liệu, Pilates, Cải thiện tư thế', 5.00, TRUE);