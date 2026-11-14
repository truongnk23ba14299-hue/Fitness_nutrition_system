-- ====================================================================
-- BƯỚC 1: XÓA SẠCH DỮ LIỆU VÀ RESET TẤT CẢ ID VỀ 1
-- ====================================================================
TRUNCATE TABLE
    package_enrollments,
    products,
    equipments,
    trainers,
    packages,
    users
RESTART IDENTITY CASCADE;

-- ====================================================================
-- BƯỚC 2: NẠP LẠI DỮ LIỆU MẪU CHUẨN (BẮT ĐẦU TỪ ID = 1)
-- MẬT KHẨU MẶC ĐỊNH CỦA TẤT CẢ TÀI KHOẢN LÀ: 123456
-- ====================================================================

-- 1. Nạp Users (ID sẽ là 1, 2, 3, 4, 5, 6)
INSERT INTO users (full_name, email, password_hash, role, phone, address, height_cm, weight_kg) VALUES
('Nguyễn Khắc Trường (Admin)', 'admin@gym.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_ADMIN', '0988111222', 'Hà Nội', 175.0, 72.0),
('Trần Văn Hoàng (PT)', 'pt_hoang@gym.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0912345678', 'Hồ Chí Minh', 180.0, 80.0),
('Lê Thị Mai (PT Yoga)', 'pt_mai@gym.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0922334455', 'Đà Nẵng', 165.0, 52.0),
('Nguyễn Quốc Cường (PT Calisthenics)', 'pt_cuong@gym.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0933445566', 'Hà Nội', 178.0, 75.0),
('Đỗ Hải Đăng (PT Classic)', 'pt_dang@gym.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0944556677', 'Hồ Chí Minh', 182.0, 85.0),
('Cao Tuấn (Học viên)', 'caotuan@gmail.com', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_CLIENT', '0977889900', 'Hải Phòng', 170.0, 65.5);

-- 2. Nạp Khóa tập (Bao gồm gói trải nghiệm ngày, 1 tháng, 6 tháng, 12 tháng)
INSERT INTO packages (title, description, price, duration_months, image_url, benefits, schedule_details, is_active) VALUES
('Gói Vé Tập Trải Nghiệm Trong Ngày (Daily Pass)', 'Trải nghiệm không giới hạn tất cả các khu vực máy tập, phòng xông hơi Sauna và tủ đồ VIP trong 01 ngày.', 50000.00, 1, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd', 'Sử dụng miễn phí tủ đồ thông minh, nước uống, khăn tập, đo InBody miễn phí 1 lần', 'Áp dụng toàn bộ khung giờ từ 05:30 đến 21:30 trong ngày đăng ký', TRUE),
('Gói Gym Tiêu Chuẩn (1 Tháng)', 'Tập luyện không giới hạn khung giờ tất cả các ngày trong tuần tại phòng tập.', 500000.00, 1, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48', 'Miễn phí tủ đồ, nước uống, phòng tắm nóng lạnh', 'Từ 05:30 đến 21:30 hàng ngày', TRUE),
('Gói Khỏe Đẹp Toàn Diện (6 Tháng)', 'Bao gồm Gym + Lớp Yoga + Phòng xông hơi khô Sauna chuẩn quốc tế.', 2400000.00, 6, 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Tặng 2 buổi tập thử cùng PT chuyên nghiệp, tủ đồ VIP', 'Không giới hạn thời gian', TRUE),
('Gói Hội Viên VIP Thượng Hạng (12 Tháng)', 'Trải nghiệm đỉnh cao không giới hạn mọi dịch vụ trên toàn hệ thống phòng tập.', 4200000.00, 12, 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f', 'Tặng 5 buổi PT riêng, đo chỉ số InBody miễn phí hàng tháng', 'Full 24/7', TRUE);

-- 3. Nạp Sản phẩm dinh dưỡng, dụng cụ & phụ kiện
INSERT INTO products (name, description, category, brand, price, stock_quantity, weight_kg, image_url) VALUES
-- Thực phẩm bổ sung (4 sản phẩm)
('Sữa Tăng Cơ Whey Gold Standard 5Lbs', 'Bổ sung 24g Protein tinh khiết phục hồi cơ bắp nhanh chóng sau khi tập.', 'THỰC PHẨM BỔ SUNG', 'Optimum Nutrition', 1850000.00, 25, 2.27, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d'),
('Creatine Monohydrate Tăng Sức Mạnh Rule 1 (300g)', 'Tăng cường sản sinh ATP, hỗ trợ bộc phát sức mạnh tối đa trong các bài tập tạ nặng.', 'THỰC PHẨM BỔ SUNG', 'Rule 1', 520000.00, 50, 0.30, 'https://images.unsplash.com/photo-1579722821273-0f6c7d44362f'),
('BCAA + EAA Phục Hồi Chống Mệt Mỏi Mutant Geaar 30 Servings', 'Cung cấp 9 loại Amino Acid thiết yếu chống dị hóa cơ và phục hồi tức thì trong khi tập.', 'THỰC PHẨM BỔ SUNG', 'Mutant', 680000.00, 35, 0.38, 'https://images.unsplash.com/photo-1594882645126-14020914d58d'),
('Pre-Workout C4 Original Bùng Nổ Năng Lượng 60 Servings', 'Kích thích tỉnh táo, tăng cường lưu thông máu và tối đa hóa năng lượng tập luyện.', 'THỰC PHẨM BỔ SUNG', 'Cellucor', 890000.00, 30, 0.39, 'https://images.unsplash.com/photo-1546483875-ad9014c88eba'),

-- Dụng cụ tập luyện (3 sản phẩm)
('Bình Lắc Tập Gym Shaker 700ml', 'Bình nhựa cao cấp không chứa BPA có lưới đánh tan bột siêu mịn.', 'DỤNG CỤ', 'GymPro', 120000.00, 100, 0.35, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd'),
('Con Lăn Tập Cơ Bụng 4 Bánh Kèm Đệm Đầu Gối', 'Thiết kế 4 bánh chịu lực chống trơn trượt, hỗ trợ siết cơ bụng 6 múi săn chắc.', 'DỤNG CỤ', 'GymPro', 190000.00, 60, 0.85, 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00'),
('Bộ Dây Kháng Lực Đàn Hồi Power Band (Set 5 Dây)', 'Cao su thiên nhiên cao cấp, hỗ trợ tập đu xà, khởi động và tập toàn thân.', 'DỤNG CỤ', 'Aolikes', 290000.00, 80, 0.60, 'https://images.unsplash.com/photo-1518611012118-696072aa579a'),

-- Phụ kiện tập gym (3 sản phẩm)
('Găng Tay Tập Gym Có Cuốn Cổ Tay Harbinger', 'Bảo vệ lòng bàn tay chống chai sạn, tăng độ bám khi nâng tạ nặng.', 'PHỤ KIỆN', 'Harbinger', 350000.00, 40, 0.20, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61'),
('Đai Lưng Cứng Deadlift & Squat Da Bò Valeo', 'Bảo vệ cột sống thắt lưng tối đa khi nâng tạ nặng, chất liệu da bò êm ái bền bỉ.', 'PHỤ KIỆN', 'Valeo', 450000.00, 45, 0.50, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd'),
('Dây Kéo Lưng Lifting Straps Trợ Lực Nâng Tạ', 'Hỗ trợ chống mỏi cổ tay, gia tăng lực kéo trong các bài Deadlift và Kéo xô.', 'PHỤ KIỆN', 'Harbinger', 180000.00, 90, 0.15, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61');

-- 4. Nạp Huấn luyện viên (ID sẽ là 1, 2, 3, 4)
INSERT INTO trainers (full_name, avatar_url, bio, experience_years, specialty, rating, is_active) VALUES
('Trần Văn Hoàng', 'https://images.unsplash.com/photo-1567013127542-490d757e51fc', 'Chứng chỉ HLV Thể hình Quốc tế NASM, từng đoạt HCB Thể hình Quốc gia 2022.', 5, 'Tăng cơ, Siết mỡ, Boxing cường độ cao', 4.90, TRUE),
('Lê Thị Mai', 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Bậc thầy Yoga 200 giờ tại Ấn Độ, chuyên trị liệu xương khớp và thon gọn vóc dáng.', 4, 'Yoga Trị Liệu, Pilates, Cải thiện tư thế', 5.00, TRUE),
('Nguyễn Quốc Cường', 'https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5', 'Chuyên gia huấn luyện thể lực & Calisthenics, có 6 năm kinh nghiệm đào tạo VĐV thể hình.', 6, 'Calisthenics, Phục hồi chức năng, Tăng sức bền', 4.95, TRUE),
('Đỗ Hải Đăng', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b', 'VĐV Thể hình cổ điển (Classic Physique), chuyên gia dinh dưỡng và giảm mỡ cấp tốc.', 5, 'Classic Physique, Siết cơ chuyên sâu, Kickboxing', 4.90, TRUE);

-- 5. Nạp Thiết bị phòng tập (ID sẽ là 1, 2, 3, 4, 5, 6, 7)
INSERT INTO equipments (name, brand, image_url, workout_guide, target_muscle) VALUES
(
    'Máy Chạy Bộ Điện Cao Cấp',
    'Technogym',
    'https://images.unsplash.com/photo-1540497077202-7c8a3999166f',
    '1. Đứng giữa thảm chạy, cài kẹp an toàn vào áo.\n2. Bấm Start, chọn tốc độ khởi động 4-6 km/h trong 3 phút.\n3. Tăng dần tốc độ lên 8-10 km/h để chạy đốt mỡ.\n4. Giữ thẳng lưng, mắt nhìn phía trước, tiếp đất bằng nửa bàn chân trước để bảo vệ khớp gối.',
    'Tim mạch & Đốt mỡ (Cardio)'
),
(
    'Máy Ép Ngực Bằng Cần Gạt (Pec Fly Machine)',
    'Life Fitness',
    'https://images.unsplash.com/photo-1534438327276-14e5300c3a48',
    '1. Chỉnh độ cao ghế sao cho tay cầm ngang ngực.\n2. Lưng và đầu áp sát đệm, hai tay nắm cần gạt, cùi chỏ hơi cong nhẹ.\n3. Hít một hơi sâu, dùng lực cơ ngực ép hai tay vào nhau trước mặt.\n4. Giữ siết cơ ngực 1 giây rồi từ từ mở rộng tay về vị trí ban đầu và thở ra.',
    'Cơ Ngực (Chest)'
),
(
    'Máy Kéo Xô Ngồi (Lat Pulldown Machine)',
    'Impulse',
    'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e',
    '1. Ngồi cố định đùi dưới đệm giữ chân, hai tay nắm thanh đòn rộng hơn vai.\n2. Hơi ngả người về sau khoảng 10-15 độ, ngực ưỡn.\n3. Thở ra và kéo thanh đòn xuống ngang phần ngực trên, siết chặt hai bả vai vào nhau.\n4. Hít vào và từ từ thả tay đưa thanh đòn trở về vị trí cao nhất.',
    'Cơ Lưng & Xô (Back / Lats)'
),
(
    'Máy Đạp Đùi Nghiêng 45 Độ (Leg Press Machine)',
    'Matrix',
    'https://images.unsplash.com/photo-1517838277536-f5f99be501cd',
    '1. Nằm áp lưng và mông sát đệm ghế, đặt 2 bàn chân rộng bằng vai lên bàn đạp.\n2. Đẩy nhẹ bàn chân lên để gạt mở chốt an toàn 2 bên.\n3. Hít vào, từ từ hạ bàn đạp xuống cho đến khi đầu gối tạo góc 90 độ (không để đùi ép sát ngực).\n4. Thở ra, dồn lực vào gót chân đẩy mạnh bàn đạp lên vị trí ban đầu (chú ý KHÔNG khóa thẳng khớp gối).',
    'Cơ Đùi Trước & Mông (Quads & Glutes)'
),
(
    'Khung Gánh Tạ Đa Năng (Smith Machine)',
    'Hammer Strength',
    'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2',
    '1. Đặt thanh đòn ngang vai sau (phần cơ cầu vai, không tì vào đốt sống cổ).\n2. Hai chân đứng rộng bằng vai, xoay cổ tay mở khóa tạ.\n3. Giữ lưng thẳng, siết chặt cơ bụng và từ từ đẩy hông ra sau hạ người xuống tư thế Squat đến khi đùi song song sàn.\n4. Đạp mạnh chân đứng thẳng dậy, thở ra và xoay cổ tay khóa móc an toàn.',
    'Toàn thân & Cơ Đùi Mông (Legs & Glutes)'
),
(
    'Máy Móc Đùi Sau Nằm (Lying Leg Curl)',
    'Life Fitness',
    'https://images.unsplash.com/photo-1574680096145-d05b474e2155',
    '1. Nằm sấp lên đệm máy, móc gót chân vào bên dưới con lăn đệm.\n2. Giữ tay nắm 2 bên thành ghế, giữ thân người cố định áp sát mặt đệm.\n3. Thở ra và dùng lực cơ đùi sau cuộn cẳng chân lên cao tối đa về phía mông.\n4. Giữ siết cơ 1 giây rồi hít vào, từ từ hạ chân về vị trí ban đầu có kiểm soát.',
    'Cơ Đùi Sau & Mông (Hamstrings & Glutes)'
),
(
    'Dàn Kéo Cáp Đôi Đa Năng (Dual Cable Crossover)',
    'Technogym',
    'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2',
    '1. Chỉnh độ cao 2 bên ròng rọc ngang ngực hoặc cao hơn đầu tùy theo góc tập.\n2. Hai tay nắm chặt tay cầm cáp, bước 1 chân lên trước để tạo thế đứng thăng bằng.\n3. Hơi ngả thân người về trước 15 độ, hít một hơi sâu và gập nhẹ khuỷu tay.\n4. Thở ra, dùng cơ ngực ép 2 tay vào nhau trước mặt và siết cơ trong 1 giây trước khi mở tay chậm rãi.',
    'Cơ Ngực & Tay Sau (Chest & Triceps)'
);
