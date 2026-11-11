package com.fitness.config;

import com.fitness.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
public class SecurityConfig {

    // 1. Cung cấp bộ mã hóa mật khẩu BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cung cấp UserDetailsService để tìm kiếm user trong database theo Email
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + username));
    }

    // 3. Expose AuthenticationManager Bean cho LoginService
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 4. Cấu hình phân quyền cổng vào API và JWT Filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Tắt CSRF vì REST API giao tiếp qua JSON
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình CORS để cho phép Frontend kết nối
                .cors(Customizer.withDefaults())

                // Không tạo Session trên server vì dùng cơ chế JWT Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình quyền truy cập các đường dẫn
                .authorizeHttpRequests(auth -> auth

                        // Cho phép Preflight request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Đăng ký tài khoản và đăng nhập không cần JWT
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Yêu cầu phải có Token hợp lệ khi xem thông tin cá nhân
                        .requestMatchers("/api/users/**", "/api/auth/me").authenticated()

                        // Mở các API public khác để phục vụ học viên/khách xem thông tin
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/trainers/**", "/api/packages/**",  "/api/products/**").permitAll()
                        
                        // Danh sách gói tập: Public Preview
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/packages"
                        ).permitAll()

                        // Chi tiết gói tập: Phải đăng nhập
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/packages/{id}"
                        ).authenticated()

                        // Đăng ký mua gói tập: Phải đăng nhập
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/package-enrollments"
                        ).authenticated()
                        
                        // Đặt lịch PT: Phải đăng nhập
                        .requestMatchers(
                        HttpMethod.POST,
                         "/api/bookings/pt"
                        ).authenticated()

                        //CHỈ ADMIN MỚI ĐƯỢC THÊM/SỬA/XÓA THIẾT BỊ
                        .requestMatchers(HttpMethod.POST, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipments/**").hasRole("ADMIN")

                        //XEM CHI TIẾT BÀI TẬP BẮT BUỘC PHẢI ĐĂNG NHẬP (Bảo vệ nội dung)
                        .requestMatchers(HttpMethod.GET, "/api/equipments/{id}").authenticated()

                        //Khách vãng lai chỉ được xem danh sách tóm tắt thiết bị
                        .requestMatchers(HttpMethod.GET, "/api/equipments").permitAll()

                        // Tất cả các request còn lại
                        .anyRequest().authenticated()
                )

                // Đăng ký JWT Filter đứng trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
