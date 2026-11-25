package com.fitness.config;

import com.fitness.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 1. Mã hóa mật khẩu BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Tìm user theo email hoặc Username(Đánh dấu @Primary để ưu tiên)
    @Bean
    @Primary
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return input -> userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(input, input)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy người dùng với tài khoản: " + input));
    }

    // 3. Provider xác thực bằng email + mật khẩu
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // 4. AuthenticationManager tự dựng -> Triệt tiêu 100% lỗi StackOverflowError
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    // 5. Cấu hình phân quyền cổng vào API và JWT Filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Tắt CSRF vì REST API giao tiếp qua JSON
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình CORS để cho phép Frontend kết nối
                .cors(Customizer.withDefaults())

                // Cơ chế Stateless (dùng JWT, không lưu session)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình quyền truy cập các đường dẫn
                .authorizeHttpRequests(auth -> auth

                        // 1. Preflight requests (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Auth: Đăng ký ,Đăng nhập & Google OAuth không cần Token
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login","/api/auth/google").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()

                        // 3. User Profile: Bắt buộc đăng nhập
                        .requestMatchers("/api/users/**").authenticated()

                        // ==========================================================
                        // 4. PHÂN QUYỀN NỘI BỘ (ADMIN, STAFF, TRAINER)
                        // ==========================================================
                        // 4.1. Dành riêng cho ADMIN (Quản trị hệ thống, thiết bị, gói tập, sản phẩm)
                        .requestMatchers(HttpMethod.POST, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipments/**").hasRole("ADMIN")

                        // 4.2. Dành cho LỄ TÂN (Check-in, kiểm tra hội viên, thanh toán tại quầy)
                        .requestMatchers("/api/checkins/**", "/api/staff/**").hasAnyRole("STAFF", "ADMIN")

                        // 4.3. Dành cho HUẤN LUYỆN VIÊN PT (Xem lịch dạy, quản lý học viên kèm riêng)
                        .requestMatchers("/api/trainer/**").hasAnyRole("TRAINER", "ADMIN")

                        // ==========================================================
                        // 5. NỘI DUNG CẦN ĐĂNG NHẬP CỦA HỌC VIÊN (PROTECTED)
                        // ==========================================================
                        .requestMatchers(HttpMethod.GET, "/api/packages/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/equipments/{id}").authenticated()
                        .requestMatchers("/api/package-enrollments/**").authenticated()
                        .requestMatchers("/api/bookings/**").authenticated()

                        // ==========================================================
                        // 6. PUBLIC PREVIEW (Khách vãng lai xem trang chủ)
                        // ==========================================================
                        .requestMatchers(HttpMethod.GET, "/api/packages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/equipments").permitAll()
                        // Public danh sách & chi tiết sản phẩm (/api/product/{id} và /api/products)
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/product/**").permitAll()
                        // Public thông tin giới thiệu các PT ngoài trang chủ (/api/trainers số nhiều)
                        .requestMatchers(HttpMethod.GET, "/api/trainers/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/leads").permitAll()
                        // Cho phép xem danh sách voucher & kiểm tra mã voucher công khai (hoặc cho mọi đối tượng đã đăng nhập)
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/vouchers/validate").permitAll()

                        // Webhook SePay: mở public (tự xác thực bằng Header Authorization: Apikey)
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook/sepay").permitAll()
                        // Các API thanh toán còn lại yêu cầu đăng nhập JWT
                        .requestMatchers("/api/payments/**").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/vouchers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/vouchers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/vouchers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/vouchers/**").hasRole("ADMIN")

                        // Public xem ảnh tĩnh đã upload
                        .requestMatchers("/uploads/**").permitAll()
                        
                        .requestMatchers(HttpMethod.POST, "api/auth/google").permitAll()

                        // 7. Tất cả các request còn lại bắt buộc có Token
                        .anyRequest().authenticated()
                )
                // Đăng ký JWT Filter đứng trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS (Cross-Origin Resource Sharing):
     * - Mục đích: Cho phép Frontend (chạy ở cổng 5173 hoặc domain khác) có thể gửi request,
     *   kèm Token JWT và các method HTTP tới Backend (cổng 8080) mà không bị trình duyệt chặn.
     * - Tự động được Spring Security áp dụng qua cấu hình .cors(Customizer.withDefaults())
     */
     @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}