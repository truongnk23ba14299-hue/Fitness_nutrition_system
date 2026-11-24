-- XÓA SẠCH DỮ LIỆU CŨ VÀ RESET TẤT CẢ KHÓA CHÍNH (ID) VỀ 1
-- ====================================================================
TRUNCATE TABLE
    package_enrollments,
    payments,
    products,
    equipments,
    trainers,
    packages,
    users,
	orders,
	order_items,
    vouchers,
    bookings,
    trainer_reviews,
    checkins,
    body_records,
    nutrition_targets,
    meal_library,
    system_settings
RESTART IDENTITY CASCADE;

-- Gỡ CHECK constraint enum cũ (nếu có) để cho phép giá trị mới PENDING_PAYMENT
ALTER TABLE package_enrollments DROP CONSTRAINT IF EXISTS package_enrollments_status_check;

-- ====================================================================
-- NẠP DỮ LIỆU MẪU CHUẨN (CHUẨN XÁC 100% QUAN HỆ FOREIGN KEY)
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. NẠP BẢNG USERS (Chuẩn hóa 4 Role - Tổng cộng 8 tài khoản mẫu)
-- Hash BCrypt cho '123456': $2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te
-- --------------------------------------------------------------------
INSERT INTO users (id, full_name, email, username, auth_provider, password_hash, role, phone, address, height_cm, weight_kg, status) VALUES
(1, 'Nguyễn Khắc Trường (Admin)', 'admin@gym.com', 'admin', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_ADMIN', '0988111222', 'Hà Nội', 175.0, 72.0, 'ACTIVE'),
(2, 'Nguyễn Thu Trang (Lễ tân)', 'staff@gym.com', 'staff', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_STAFF', '0933221100', 'Hà Nội', 162.0, 48.0, 'ACTIVE'),
(3, 'Trần Văn Hoàng (PT)', 'pt_hoang@gym.com', 'pt_hoang', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0912345678', 'Hồ Chí Minh', 180.0, 80.0, 'ACTIVE'),
(4, 'Lê Thị Mai (PT Yoga)', 'pt_mai@gym.com', 'pt_mai', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0922334455', 'Đà Nẵng', 165.0, 52.0, 'ACTIVE'),
(5, 'Nguyễn Quốc Cường (PT Calisthenics)', 'pt_cuong@gym.com', 'pt_cuong', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0933445566', 'Hà Nội', 178.0, 75.0, 'ACTIVE'),
(6, 'Đỗ Hải Đăng (PT Classic)', 'pt_dang@gym.com', 'pt_dang', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_TRAINER', '0944556677', 'Hồ Chí Minh', 182.0, 85.0, 'ACTIVE'),
(7, 'Cao Tuấn (Hội viên VIP)', 'caotuan@gmail.com', 'caotuan', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_CLIENT', '0977889900', 'Hải Phòng', 170.0, 65.5, 'ACTIVE'),
(8, 'Trần Minh Đức (Thành viên mới)', 'minhduc@gmail.com', 'minhduc', 'LOCAL', '$2a$10$S/Do9GNmyWs2/uz.oEQk2e4afTCVyozeyVoTbc2apA7wOE1A6U2te', 'ROLE_CLIENT', '0966554433', 'Đà Nẵng', 172.0, 68.0, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));

-- --------------------------------------------------------------------
-- 2. NẠP BẢNG PACKAGES (Các gói tập Gym tiêu chuẩn & VIP - ID = 1, 2, 3, 4)
-- --------------------------------------------------------------------
INSERT INTO packages (id, title, description, price, duration_months, image_url, benefits, schedule_details, is_active) VALUES
(1, 'Gói Vé Tập Trải Nghiệm Trong Ngày (Daily Pass)', 'Trải nghiệm không giới hạn tất cả các khu vực máy tập, phòng xông hơi Sauna và tủ đồ VIP trong 01 ngày.', 50000.00, 1, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd', 'Sử dụng miễn phí tủ đồ thông minh, nước uống, khăn tập, đo InBody miễn phí 1 lần', 'Áp dụng toàn bộ khung giờ từ 05:30 đến 21:30 trong ngày đăng ký', TRUE),
(2, 'Gói Gym Tiêu Chuẩn (1 Tháng)', 'Tập luyện không giới hạn khung giờ tất cả các ngày trong tuần tại phòng tập.', 500000.00, 1, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48', 'Miễn phí tủ đồ, nước uống, phòng tắm nóng lạnh', 'Từ 05:30 đến 21:30 hàng ngày', TRUE),
(3, 'Gói Khỏe Đẹp Toàn Diện (6 Tháng)', 'Bao gồm Gym + Lớp Yoga + Phòng xông hơi khô Sauna chuẩn quốc tế.', 2400000.00, 6, 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Tặng 2 buổi tập thử cùng PT chuyên nghiệp, tủ đồ VIP', 'Không giới hạn thời gian', TRUE),
(4, 'Gói Hội Viên VIP Thượng Hạng (12 Tháng)', 'Trải nghiệm đỉnh cao không giới hạn mọi dịch vụ trên toàn hệ thống phòng tập.', 4200000.00, 12, 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f', 'Tặng 5 buổi PT riêng, đo chỉ số InBody miễn phí hàng tháng', 'Full 24/7', TRUE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('packages_id_seq', (SELECT COALESCE(MAX(id), 1) FROM packages));

-- --------------------------------------------------------------------
-- 3. NẠP BẢNG TRAINERS (Danh sách Huấn Luyện Viên đại diện - ID = 1, 2, 3, 4)
-- --------------------------------------------------------------------
INSERT INTO trainers (id, user_id, full_name, avatar_url, bio, experience_years, specialty, rating, is_active) VALUES
(1, 3, 'Trần Văn Hoàng (PT)', 'https://images.unsplash.com/photo-1567013127542-490d757e51fc', 'Cựu vận động viên thể hình quốc gia, chứng chỉ cử tạ quốc tế CSCS. Chuyên sâu Tăng cơ - Giảm mỡ cấp tốc và phục hồi chấn thương.', 6, 'Tăng cơ chuyên sâu & Giảm mỡ', 5.0, TRUE),
(2, 4, 'Lê Thị Mai (PT Yoga)', 'https://images.unsplash.com/photo-1518611012118-696072aa579a', 'Master Yoga Alliance 500h tại Rishikesh Ấn Độ. Huấn luyện viên Yoga Vinyasa, Yin Yoga và Thon gọn vóc dáng cho nữ giới.', 5, 'Yoga Nâng Cao & Thon Gọn', 4.9, TRUE),
(3, 5, 'Nguyễn Quốc Cường (PT Calisthenics)', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', 'Chuyên gia Calisthenics & Street Workout. Tăng cường sức mạnh bền bỉ từ trọng lượng cơ thể và thăng bằng đỉnh cao.', 4, 'Calisthenics & Sức mạnh cốt lõi', 4.8, TRUE),
(4, 6, 'Đỗ Hải Đăng (PT Classic)', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', 'HLV Thể hình cổ điển (Classic Physique), chuyên gia thiết kế thực đơn siết cơ Eat Clean và Macro dinh dưỡng cho VĐV.', 7, 'Thể hình Cổ Điển & Dinh Dưỡng', 5.0, TRUE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('trainers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM trainers));

-- --------------------------------------------------------------------
-- 4. NẠP HỢP ĐỒNG GÓI TẬP (PACKAGE_ENROLLMENTS - MÔ HÌNH CHUẨN F8)
-- Kích hoạt gói 6 tháng còn hạn cho Hội viên VIP Cao Tuấn (user_id = 7, package_id = 3, trainer_id = 1)
-- Tài khoản Trần Minh Đức (user_id = 8) cố tình KHÔNG có bản ghi nào để mô phỏng Member chưa mua gói
-- --------------------------------------------------------------------
INSERT INTO package_enrollments (id, user_id, package_id, trainer_id, total_price, start_date, end_date, status, notes, subtotal, discount_amount, payment_method, pt_sessions_total, pt_sessions_remaining) VALUES
(1, 7, 4, 1, 4000000.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '335 days', 'ACTIVE', 'Hội viên VIP đóng tiền cả năm, tặng 5 buổi PT riêng', 4200000.00, 200000.00, 'VIETQR', 5, 4),
(2, 8, 2, 2, 450000.00, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', 'ACTIVE', 'Đăng ký tập thử 1 tháng Yoga', 500000.00, 50000.00, 'CASH', 0, 0)
ON CONFLICT (id) DO NOTHING;
SELECT setval('package_enrollments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM package_enrollments));

-- --------------------------------------------------------------------
-- 5. NẠP BẢNG PRODUCTS (Dinh dưỡng, Dụng cụ & Phụ kiện chính hãng)
-- --------------------------------------------------------------------
INSERT INTO products (id, name, description, category, brand, price, stock_quantity, weight_kg, image_url, status) VALUES
(1, 'Sữa Tăng Cơ Whey Gold Standard 5Lbs', 'Whey Protein Isolate tinh khiết hỗ trợ phục hồi và phát triển cơ bắp tối ưu sau mỗi buổi tập nặng.', 'Thực phẩm bổ sung', 'Optimum Nutrition', 1850000.00, 50, 2.27, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d', 'ACTIVE'),
(2, 'Creatine Monohydrate Tăng Sức Mạnh Rule 1 (300g)', 'Tăng cường ATP cơ bắp, hỗ trợ tăng sức mạnh và sức bền bùng nổ khi nâng tạ nặng.', 'Thực phẩm bổ sung', 'Rule 1', 520000.00, 80, 0.30, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c', 'ACTIVE'),
(3, 'BCAA + EAA Phục Hồi Chống Mệt Mỏi Mutant Geaar 30 Servings', 'Cung cấp 9 axit amin thiết yếu ngăn ngừa dị hóa cơ bắp và chống đau mỏi sau tập.', 'Thực phẩm bổ sung', 'Mutant', 680000.00, 35, 0.40, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd', 'ACTIVE'),
(4, 'Vitamin Tổng Hợp & Khoáng Chất Animal Pak (44 Gói)', 'Gói vitamin chuyên dụng cho VĐV thể hình, nâng cao hệ miễn dịch và thúc đẩy chuyển hóa năng lượng.', 'Vitamin & Khoáng chất', 'Universal', 890000.00, 25, 0.50, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'ACTIVE'),
(5, 'Bình Lắc Tập Gym Shaker 700ml Có Con Lắc Inox', 'Chất liệu nhựa Tritan an toàn cho sức khỏe, nắp đậy chống rò rỉ nước tuyệt đối.', 'Phụ kiện tập gym', 'FitLife Gear', 120000.00, 150, 0.15, 'https://images.unsplash.com/photo-1544816155-12df9643f363', 'ACTIVE'),
(6, 'Găng Tay Tập Gym Nam Nữ Có Quấn Cổ Tay Trợ Lực', 'Đệm mút lòng bàn tay chống chai tay, dây quấn bảo vệ khớp cổ tay khi đẩy ngực nặng.', 'Phụ kiện tập gym', 'Aolikes', 180000.00, 100, 0.10, 'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2', 'ACTIVE'),
(7, 'Dây Kháng Lực Đàn Hồi Miniband Tập Mông Đùi (Set 5 Dây)', 'Nhiều mức kháng lực từ nhẹ đến siêu nặng, chuyên dụng cho bài tập Kickback, Squat mông.', 'Phụ kiện tập gym', 'Liveup', 160000.00, 120, 0.20, 'https://images.unsplash.com/photo-1598289431512-b97b0917affc', 'ACTIVE'),
(8, 'Thảm Tập Yoga Định Tuyến Chống Trượt TPE 8mm', 'Chất liệu cao su non TPE đàn hồi cao, vạch định tuyến chuẩn xác giúp tư thế Yoga không bị lệch.', 'Phụ kiện Yoga', 'Liforme Style', 350000.00, 40, 0.90, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f', 'ACTIVE'),
(9, 'Đai Lưng Cứng Deadlift & Squat Da Bò Valeo', 'Bảo vệ cột sống thắt lưng, tăng áp lực ổ bụng khi gánh đùi Squat và Deadlift tạ nặng.', 'Phụ kiện tập gym', 'Valeo', 450000.00, 60, 0.80, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
SELECT setval('products_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products));

-- --------------------------------------------------------------------
-- 6. NẠP BẢNG EQUIPMENTS (Dàn máy tập & Hướng dẫn kỹ thuật chuẩn của PT)
-- --------------------------------------------------------------------
INSERT INTO equipments (id, name, brand, image_url, video_url, workout_guide, target_muscle) VALUES
(1, 'Máy Chạy Bộ Điện Cao Cấp Matrix T70', 'Matrix Fitness', 'https://images.unsplash.com/photo-1576678927484-cc907957088c', 'https://www.youtube.com/watch?v=running_matrix', '1. Bước lên thảm chạy, kẹp khóa an toàn từ tính vào vạt áo.\n2. Bấm phím START để máy khởi động ở tốc độ 1.0 km/h.\n3. Điều chỉnh độ dốc Incline và tốc độ Speed phù hợp nhịp tim Cardio.\n4. Giữ thẳng lưng, tiếp đất bằng nửa bàn chân trước.', 'Tim mạch & Đốt mỡ toàn thân (Cardio)'),
(2, 'Máy Đạp Đùi Nghiêng 45 Độ (Linear Leg Press)', 'Hammer Strength', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48', 'https://www.youtube.com/watch?v=leg_press_45', '1. Ngồi áp sát lưng và đầu vào đệm, đặt hai chân lên bàn đạp rộng bằng vai.\n2. Đạp bàn chân mở khóa an toàn hai bên hông.\n3. Hít vào, từ từ hạ mâm tạ xuống đến khi đầu gối tạo góc 90 độ (không nhấc mông khỏi đệm).\n4. Thở ra, đạp mạnh chân đẩy bàn tạ lên (không khóa khớp gối).', 'Cơ Đùi Trước, Đùi Sau & Cơ Mông (Legs)'),
(3, 'Máy Kéo Xô Lưng Cáp Đôi (Lat Pulldown)', 'Life Fitness', 'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e', 'https://www.youtube.com/watch?v=lat_pulldown_life', '1. Chỉnh đệm kê đùi vừa vặn để cố định thân dưới.\n2. Hai tay nắm thanh xô rộng hơn vai, lòng bàn tay hướng về trước.\n3. Hơi ưỡn ngực, thở ra và kéo thanh xô xuống sát xương quai xanh ngực trên.\n4. Giữ siết cơ xô lưng 1 giây rồi hít vào, thả tay chậm rãi lên vị trí ban đầu.', 'Cơ Lưng Xô (Lats & Upper Back)'),
(4, 'Khung Gánh Tạ Đa Năng Smith Machine', 'Technogym', 'https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2', 'https://www.youtube.com/watch?v=smith_squat_tech', '1. Đặt thanh đòn ngang vai sau (phần cơ cầu vai, không tì vào đốt sống cổ).\n2. Hai chân đứng rộng bằng vai, xoay cổ tay mở khóa tạ.\n3. Giữ lưng thẳng, siết chặt cơ bụng và từ từ đẩy hông ra sau hạ người xuống tư thế Squat đến khi đùi song song sàn.\n4. Đạp mạnh chân đứng thẳng dậy, thở ra và xoay cổ tay khóa móc an toàn.', 'Toàn thân & Cơ Đùi Mông (Legs & Glutes)')
ON CONFLICT (id) DO NOTHING;
SELECT setval('equipments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM equipments));

-- ====================================================================
-- 7. ĐƠN HÀNG SHOP (ORDERS & ORDER_ITEMS) — Minh Quang
--    Admin quản lý xử lý đơn hàng của học viên & tồn kho sản phẩm
--    Luồng trạng thái: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED / CANCELLED
--    (Schema khớp entity Order/OrderItem của tuantinhte17-droid)
-- ====================================================================

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    note TEXT,
    total_amount NUMERIC(12, 2) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL,
    sub_total NUMERIC(12, 2) NOT NULL,
    CONSTRAINT uk_order_items_order_product UNIQUE (order_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

TRUNCATE TABLE order_items, orders RESTART IDENTITY CASCADE;

-- --------------------------------------------------------------------
-- 7.1. NẠP ĐƠN HÀNG MẪU (ORDERS)
-- Đơn của Hội viên VIP Cao Tuấn (user_id = 7) và Thành viên Trần Minh Đức (user_id = 8)
-- --------------------------------------------------------------------
INSERT INTO orders (user_id, receiver_name, phone, shipping_address, note, subtotal, discount_amount, total_amount, payment_method, payment_status, order_status) VALUES
(7, 'Cao Tuấn', '0977889900', 'Hải Phòng', 'Giao giờ hành chính, gọi trước khi giao', 2540000.00, 0.00, 2540000.00, 'COD', 'UNPAID', 'PENDING'),
(8, 'Trần Minh Đức', '0966554433', 'Đà Nẵng', NULL, 680000.00, 0.00, 680000.00, 'VIETQR', 'PAID', 'CONFIRMED'),
(7, 'Cao Tuấn', '0977889900', 'Hải Phòng', 'Đơn đã hoàn tất - đã nhận hàng', 520000.00, 0.00, 520000.00, 'COD', 'PAID', 'COMPLETED');
-- --------------------------------------------------------------------
-- 7.2. NẠP CHI TIẾT ĐƠN HÀNG (ORDER_ITEMS - product_id tham chiếu bảng products ở mục 5)
-- sub_total = unit_price * quantity
-- --------------------------------------------------------------------
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, sub_total) VALUES
(1, 1, 'Sữa Tăng Cơ Whey Gold Standard 5Lbs', 1, 1850000.00, 1850000.00),
(1, 5, 'Bình Lắc Tập Gym Shaker 700ml', 2, 120000.00, 240000.00),
(1, 9, 'Đai Lưng Cứng Deadlift & Squat Da Bò Valeo', 1, 450000.00, 450000.00),
(2, 3, 'BCAA + EAA Phục Hồi Chống Mệt Mỏi Mutant Geaar 30 Servings', 1, 680000.00, 680000.00),
(3, 2, 'Creatine Monohydrate Tăng Sức Mạnh Rule 1 (300g)', 1, 520000.00, 520000.00);

-- --------------------------------------------------------------------
-- 8. NẠP MÃ GIẢM GIÁ (VOUCHERS)
-- --------------------------------------------------------------------
INSERT INTO vouchers (id, code, title, discount_type, discount_value, max_discount, min_order_value, scope, is_active, started_date, expiry_date, quantity, used_count) VALUES
(1, 'NEWMEMBER', 'Ưu đãi Hội viên mới - Giảm 10%', 'PERCENT', 10, 300000, 300000, 'ALL', TRUE, CURRENT_TIMESTAMP, '2027-12-31 23:59:59', 100, 0),
(2, 'VIP2026', 'Đặc quyền VIP - Giảm ngay 200K', 'FIXED', 200000, 200000, 1500000, 'ALL', TRUE, CURRENT_TIMESTAMP, '2027-12-31 23:59:59', 100, 0),
(3, 'FITLIFE50', 'Trợ giá năng lượng - Giảm 50K', 'FIXED', 50000, 50000, 400000, 'ALL', TRUE, CURRENT_TIMESTAMP, '2027-12-31 23:59:59', 100, 0),
(4, 'SUPPLEMENT15', 'Ưu đãi Dinh dưỡng - Giảm 15%', 'PERCENT', 15, 250000, 600000, 'PRODUCT', TRUE, CURRENT_TIMESTAMP, '2027-12-31 23:59:59', 100, 0)
ON CONFLICT (id) DO UPDATE SET 
    code = EXCLUDED.code,
    title = EXCLUDED.title,
    discount_type = EXCLUDED.discount_type,
    discount_value = EXCLUDED.discount_value,
    max_discount = EXCLUDED.max_discount,
    min_order_value = EXCLUDED.min_order_value,
    scope = EXCLUDED.scope,
    is_active = EXCLUDED.is_active,
    started_date = EXCLUDED.started_date,
    expiry_date = EXCLUDED.expiry_date,
    quantity = EXCLUDED.quantity,
    used_count = EXCLUDED.used_count;

SELECT setval('vouchers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM vouchers));

-- --------------------------------------------------------------------
-- 9. NẠP LỊCH TẬP PT & ĐÁNH GIÁ (BOOKINGS & TRAINER_REVIEWS)
-- --------------------------------------------------------------------
INSERT INTO bookings (id, user_id, trainer_id, booking_datetime, note, status, completed_at, trainer_note) VALUES
(1, 7, 1, CURRENT_DATE + INTERVAL '1 day' + TIME '09:00:00', 'Tập chuyên sâu Ngực & Tay sau', 'CONFIRMED', NULL, NULL),
(2, 7, 1, CURRENT_DATE - INTERVAL '2 days' + TIME '14:00:00', 'Buổi tập Chân & Mông', 'COMPLETED', CURRENT_DATE - INTERVAL '2 days' + TIME '15:30:00', 'Học viên hoàn thành giáo án xuất sắc, mức tạ Squat đạt 80kg.')
ON CONFLICT (id) DO NOTHING;
SELECT setval('bookings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM bookings));

INSERT INTO trainer_reviews (id, booking_id, trainer_id, user_id, rating, comment) VALUES
(1, 2, 1, 7, 5, 'HLV Hoàng hướng dẫn rất tận tình, chỉnh form Squat rất chuẩn giúp mình không còn bị đau lưng dưới!')
ON CONFLICT (id) DO NOTHING;
SELECT setval('trainer_reviews_id_seq', (SELECT COALESCE(MAX(id), 1) FROM trainer_reviews));

-- --------------------------------------------------------------------
-- 10. NẠP NHẬT KÝ QUÉT THẺ CHECK-IN (CHECKINS)
-- --------------------------------------------------------------------
INSERT INTO checkins (id, user_id, enrollment_id, method, staff_id, checked_in_at) VALUES
(1, 7, 1, 'QR', 2, CURRENT_DATE - INTERVAL '2 days' + TIME '13:55:00'),
(2, 7, 1, 'QR', 2, CURRENT_DATE - INTERVAL '1 day' + TIME '17:30:00')
ON CONFLICT (id) DO NOTHING;
SELECT setval('checkins_id_seq', (SELECT COALESCE(MAX(id), 1) FROM checkins));
-- --------------------------------------------------------------------
-- 11. NẠP TIẾN ĐỘ ĐO INBODY (BODY_RECORDS)
-- --------------------------------------------------------------------
INSERT INTO body_records (id, user_id, record_date, weight_kg, height_cm, body_fat_pct, muscle_kg, bmi, note) VALUES
(1, 7, CURRENT_DATE - INTERVAL '30 days', 68.0, 170.0, 20.5, 30.2, 23.5, 'Lần đo đầu tiên khi mới đăng ký gói VIP'),
(2, 7, CURRENT_DATE - INTERVAL '15 days', 66.8, 170.0, 19.2, 30.8, 23.1, 'Sau 2 tuần tập cùng PT Hoàng, mỡ giảm cơ tăng nhẹ'),
(3, 7, CURRENT_DATE, 65.5, 170.0, 17.8, 31.5, 22.7, 'Mục tiêu thể hình đạt chuẩn rất tốt')
ON CONFLICT (id) DO NOTHING;
SELECT setval('body_records_id_seq', (SELECT COALESCE(MAX(id), 1) FROM body_records));
-- --------------------------------------------------------------------
-- 12. NẠP MỤC TIÊU BMR / TDEE & MACRO (NUTRITION_TARGETS)
-- --------------------------------------------------------------------
INSERT INTO nutrition_targets (user_id, gender, age, height_cm, weight_kg, activity_level, goal, bmr, tdee, target_calories, protein_g, carbs_g, fat_g, water_ml, recommendation, source, set_by_trainer_id) VALUES
(7, 'MALE', 25, 170.0, 65.5, 'MODERATE', 'LOSE_FAT', 1620, 2250, 2000, 150, 220, 55, 2500, 'Chia 4 bữa trong ngày, ưu tiên ức gà, yến mạch và rau xanh; uống đủ 2.5 lít nước.', 'TRAINER', 1)
ON CONFLICT (user_id) DO NOTHING;
-- --------------------------------------------------------------------
-- 13. NẠP THƯ VIỆN MÓN ĂN EAT CLEAN (MEAL_LIBRARY)
-- --------------------------------------------------------------------
INSERT INTO meal_library (id, name, category, calories, protein_g, carbs_g, fat_g, serving, image_url, description, is_active) VALUES
(1, 'Ức gà nướng thảo mộc kèm khoai lang', 'Bữa trưa', 420, 45.0, 40.0, 6.0, '200g ức gà + 1 củ khoai', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c', 'Món ăn giàu đạm ít mỡ chuẩn fitness', TRUE),
(2, 'Salad cá ngừ sốt mè rang', 'Bữa tối', 310, 32.0, 15.0, 12.0, '1 đĩa lớn', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', 'Thanh mát, giàu Omega-3 tốt cho tim mạch', TRUE),
(3, 'Cháo yến mạch chuối & bơ đậu phộng', 'Bữa sáng', 380, 16.0, 55.0, 10.0, '1 bát 350ml', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'Cung cấp năng lượng tinh bột chậm cho buổi sáng', TRUE),
(4, 'Bò bít tết áp chảo măng tây', 'Bữa trưa', 490, 48.0, 12.0, 22.0, '180g thăn bò', 'https://images.unsplash.com/photo-1558030006-450675393462', 'Giàu kẽm và Creatine tự nhiên giúp phát triển cơ bắp', TRUE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('meal_library_id_seq', (SELECT COALESCE(MAX(id), 1) FROM meal_library));
-- --------------------------------------------------------------------
-- 14. NẠP CẤU HÌNH HỆ THỐNG (SYSTEM_SETTINGS)
-- --------------------------------------------------------------------
INSERT INTO system_settings (setting_key, setting_value) VALUES
('gym_name', 'FitLife Fitness & Nutrition Club'),
('cancel_hours_limit', '4'),
('daily_pass_price', '50000'),
('auto_approve_bookings', 'true')
ON CONFLICT (setting_key) DO NOTHING;

-- --------------------------------------------------------------------
-- 15. NẠP LỊCH SỬ THANH TOÁN MẪU (PAYMENTS)
-- --------------------------------------------------------------------
INSERT INTO payments (id, invoice_no, payment_code, user_id, ref_type, ref_id, amount, method, status, gateway, created_at, paid_at) VALUES
(1, 'INV-PKG-1', 'FLPKG1', 7, 'ENROLLMENT', 1, 4000000.00, 'VIETQR', 'PAID', 'SEPAY', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
(2, 'INV-PKG-2', 'FLPKG2', 8, 'ENROLLMENT', 2, 450000.00,  'CASH',   'PAID', 'CASH',  NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(3, 'INV-ORD-2', 'FLORD2', 8, 'ORDER',      2, 680000.00,  'VIETQR', 'PAID', 'SEPAY', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
(4, 'INV-ORD-3', 'FLORD3', 7, 'ORDER',      3, 520000.00,  'COD',    'PAID', 'COD',   NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
SELECT setval('payments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payments));
=======

-- --------------------------------------------------------------------
-- 16. NẠP thông báo mẫu (Notification)
-- --------------------------------------------------------------------
INSERT INTO notifications (user_id, type, severity, title, message, link, is_read, created_at)
VALUES 
(7, 'BOOKING', 'INFO', 'Nhắc lịch tập PT ngày mai', 'Bạn có ca tập Squat cùng HLV Hoàng lúc 09:00.', '/client/my-bookings', false, NOW()),
(7, 'ENROLLMENT', 'WARNING', 'Gói tập sắp hết hạn', 'Gói tập 6 tháng của bạn chỉ còn 7 ngày hiệu lực.', '/client/my-packages', false, NOW()),
(7, 'ORDER', 'INFO', 'Đơn hàng đã giao thành công', 'Đơn hàng Whey Gold Standard đã được giao tới bạn.', '/client/my-orders', true, NOW() - INTERVAL '1 day');
>>>>>>> Stashed changes
