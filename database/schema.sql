-- ====================================================================
-- FITNESS & NUTRITION MANAGEMENT SYSTEM - DATABASE SCHEMA (PostgreSQL)
-- Phiên bản hoàn chỉnh, chuẩn hóa thực thể, khóa chính, khóa ngoại & chỉ mục
-- ====================================================================

-- 1. BẢNG PHÂN QUYỀN & NGƯỜI DÙNG (AUTH & USERS)
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, -- ROLE_ADMIN, ROLE_COACH, ROLE_CLIENT, ROLE_RECEPTIONIST
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    date_of_birth DATE,
    address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 2. HỒ SƠ CHI TIẾT TỪNG ĐỐI TƯỢNG (PROFILES)
-- --------------------------------------------------------------------

-- Hồ sơ Học viên / Khách hàng thành viên
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    height_cm NUMERIC(5, 2),
    current_weight_kg NUMERIC(5, 2),
    target_weight_kg NUMERIC(5, 2),
    fitness_goal VARCHAR(50) CHECK (fitness_goal IN ('WEIGHT_LOSS', 'MUSCLE_GAIN', 'ENDURANCE', 'MAINTENANCE', 'GENERAL_FITNESS')),
    activity_level VARCHAR(50) CHECK (activity_level IN ('SEDENTARY', 'LIGHTLY_ACTIVE', 'MODERATELY_ACTIVE', 'VERY_ACTIVE')),
    health_notes TEXT,
    emergency_contact VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Hồ sơ Huấn luyện viên
CREATE TABLE IF NOT EXISTS coaches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialty VARCHAR(100) NOT NULL, -- Gym, Yoga, Pilates, Nutrition, Crossfit...
    experience_years INT DEFAULT 0,
    bio TEXT,
    certifications TEXT,
    hourly_rate NUMERIC(12, 2) DEFAULT 0,
    rating_avg NUMERIC(3, 2) DEFAULT 5.00,
    rating_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Hồ sơ Lễ tân
CREATE TABLE IF NOT EXISTS receptionists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    shift_type VARCHAR(30) DEFAULT 'FULL_TIME' CHECK (shift_type IN ('MORNING', 'AFTERNOON', 'EVENING', 'FULL_TIME')),
    hire_date DATE DEFAULT CURRENT_DATE
);

-- Bảng lưu khách vãng lai / người dùng chưa login gửi form tư vấn từ Homepage
CREATE TABLE IF NOT EXISTS consultation_leads (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    fitness_goal VARCHAR(100),
    preferred_time VARCHAR(50),
    status VARCHAR(30) DEFAULT 'NEW' CHECK (status IN ('NEW', 'CONTACTED', 'CONVERTED', 'CANCELLED')),
    notes TEXT,
    assigned_receptionist_id BIGINT REFERENCES receptionists(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. GÓI DỊCH VỤ, HỘI VIÊN, THANH TOÁN & CHECK-IN
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS membership_packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_days INT NOT NULL, -- 30, 90, 180, 365...
    price NUMERIC(12, 2) NOT NULL,
    package_type VARCHAR(50) NOT NULL CHECK (package_type IN ('BASIC', 'VIP', 'PT_1ON1', 'GROUP_CLASS')),
    sessions_count INT DEFAULT 0, -- Số buổi tập cùng PT hoặc lớp học (0 nếu không giới hạn)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    package_id BIGINT NOT NULL REFERENCES membership_packages(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    remaining_sessions INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'ACTIVE' CHECK (status IN ('PENDING_PAYMENT', 'ACTIVE', 'EXPIRED', 'SUSPENDED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES client_subscriptions(id) ON DELETE RESTRICT,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'VNPAY', 'MOMO', 'CREDIT_CARD')),
    transaction_code VARCHAR(100) UNIQUE,
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Điểm danh / Check-in của hội viên khi đến phòng tập
CREATE TABLE IF NOT EXISTS check_ins (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    subscription_id BIGINT REFERENCES client_subscriptions(id) ON DELETE SET NULL,
    receptionist_id BIGINT REFERENCES receptionists(id) ON DELETE SET NULL,
    check_in_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255)
);

-- 4. LỊCH LÀM VIỆC & LỊCH HẸN (APPOINTMENTS & SCHEDULES)
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS coach_availabilities (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL REFERENCES coaches(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1: Chủ nhật, 2: Thứ 2 ...
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    coach_id BIGINT NOT NULL REFERENCES coaches(id) ON DELETE CASCADE,
    subscription_id BIGINT REFERENCES client_subscriptions(id) ON DELETE SET NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    type VARCHAR(30) DEFAULT 'WORKOUT' CHECK (type IN ('WORKOUT', 'NUTRITION_CONSULTATION', 'BODY_ASSESSMENT')),
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. QUẢN LÝ TẬP LUYỆN (WORKOUTS & EXERCISES)
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    muscle_group VARCHAR(50) NOT NULL CHECK (muscle_group IN ('CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE', 'CARDIO', 'FULL_BODY')),
    difficulty VARCHAR(30) DEFAULT 'BEGINNER' CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    equipment_needed VARCHAR(100),
    video_url VARCHAR(255),
    image_url VARCHAR(255),
    calories_burned_per_min NUMERIC(5, 2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS workout_plans (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    coach_id BIGINT REFERENCES coaches(id) ON DELETE SET NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workout_plan_details (
    id BIGSERIAL PRIMARY KEY,
    workout_plan_id BIGINT NOT NULL REFERENCES workout_plans(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    sets INT DEFAULT 3,
    reps INT DEFAULT 10,
    weight_kg NUMERIC(6, 2) DEFAULT 0,
    duration_minutes INT DEFAULT 0,
    rest_seconds INT DEFAULT 60,
    order_index INT DEFAULT 1,
    notes TEXT
);

-- Nhật ký tập luyện thực tế của học viên
CREATE TABLE IF NOT EXISTS workout_logs (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    log_date DATE DEFAULT CURRENT_DATE,
    actual_sets INT,
    actual_reps INT,
    actual_weight_kg NUMERIC(6, 2),
    actual_duration_min INT,
    calories_burned NUMERIC(6, 2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. QUẢN LÝ DINH DƯỠNG (NUTRITION & MEAL PLANNING)
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS foods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50), -- Rau củ, Thịt cá, Ngũ cốc, Trái cây...
    serving_size VARCHAR(50) DEFAULT '100g',
    calories NUMERIC(6, 2) NOT NULL,
    protein_g NUMERIC(6, 2) DEFAULT 0,
    carbs_g NUMERIC(6, 2) DEFAULT 0,
    fat_g NUMERIC(6, 2) DEFAULT 0,
    fiber_g NUMERIC(6, 2) DEFAULT 0,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS nutrition_plans (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    coach_id BIGINT REFERENCES coaches(id) ON DELETE SET NULL,
    title VARCHAR(150) NOT NULL,
    daily_calories_target NUMERIC(6, 2) NOT NULL,
    protein_target_g NUMERIC(6, 2),
    carbs_target_g NUMERIC(6, 2),
    fat_target_g NUMERIC(6, 2),
    notes TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS meal_plan_details (
    id BIGSERIAL PRIMARY KEY,
    nutrition_plan_id BIGINT NOT NULL REFERENCES nutrition_plans(id) ON DELETE CASCADE,
    meal_type VARCHAR(30) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'NIGHT_SNACK')),
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE RESTRICT,
    portion_amount NUMERIC(6, 2) DEFAULT 1.0, -- Gấp bao nhiêu lần serving_size
    notes TEXT
);

-- Nhật ký dinh dưỡng hàng ngày của học viên
CREATE TABLE IF NOT EXISTS daily_nutrition_logs (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE RESTRICT,
    log_date DATE DEFAULT CURRENT_DATE,
    meal_type VARCHAR(30) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'NIGHT_SNACK')),
    portion_multiplier NUMERIC(6, 2) DEFAULT 1.0,
    total_calories NUMERIC(6, 2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. THEO DÕI TIẾN ĐỘ & CHỈ SỐ CƠ THỂ (BODY METRICS)
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS body_metrics (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    recorded_date DATE DEFAULT CURRENT_DATE,
    weight_kg NUMERIC(5, 2) NOT NULL,
    height_cm NUMERIC(5, 2),
    body_fat_pct NUMERIC(4, 2),
    muscle_mass_kg NUMERIC(5, 2),
    bmi NUMERIC(4, 2),
    chest_cm NUMERIC(5, 2),
    waist_cm NUMERIC(5, 2),
    hips_cm NUMERIC(5, 2),
    biceps_cm NUMERIC(5, 2),
    progress_photo_url VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. ĐÁNH GIÁ, PHẢN HỒI, TIN NHẮN & THÔNG BÁO
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    coach_id BIGINT REFERENCES coaches(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(30) DEFAULT 'SYSTEM' CHECK (type IN ('APPOINTMENT', 'WORKOUT', 'NUTRITION', 'MEMBERSHIP', 'SYSTEM')),
    is_read BOOLEAN DEFAULT FALSE,
    link_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. TẠO CÁC CHỈ MỤC TỐI ƯU HIỆU NĂNG (INDEXES)
-- --------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_client_user_id ON clients(user_id);
CREATE INDEX IF NOT EXISTS idx_coach_user_id ON coaches(user_id);
CREATE INDEX IF NOT EXISTS idx_receptionist_user_id ON receptionists(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_client ON client_subscriptions(client_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON client_subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_appointments_client_coach ON appointments(client_id, coach_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_workout_plans_client ON workout_plans(client_id);
CREATE INDEX IF NOT EXISTS idx_workout_logs_client_date ON workout_logs(client_id, log_date);
CREATE INDEX IF NOT EXISTS idx_nutrition_plans_client ON nutrition_plans(client_id);
CREATE INDEX IF NOT EXISTS idx_daily_food_logs_client_date ON daily_nutrition_logs(client_id, log_date);
CREATE INDEX IF NOT EXISTS idx_body_metrics_client_date ON body_metrics(client_id, recorded_date);
CREATE INDEX IF NOT EXISTS idx_chat_sender_receiver ON chat_messages(sender_id, receiver_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read);