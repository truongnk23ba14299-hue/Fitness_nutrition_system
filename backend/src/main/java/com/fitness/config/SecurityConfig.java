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

                        // 1. Preflight requests (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 2. Auth: Đăng ký & Đăng nhập không cần Token
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        // 3. User Profile: Bắt buộc đăng nhập
                        .requestMatchers("/api/users/**").authenticated()
                        // 4. Các endpoint dành riêng cho ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipments/**").hasRole("ADMIN")
                        // 5. Nội dung cần Token của Học viên (Protected)
                        .requestMatchers(HttpMethod.GET, "/api/packages/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/equipments/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/package-enrollments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/package-enrollments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/bookings/**").authenticated()
                        // 6. Public Preview (Khách vãng lai xem trang chủ)
                        .requestMatchers(HttpMethod.GET, "/api/packages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/equipments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trainers/**").permitAll()
                        // 7. Tất cả các request còn lại bắt buộc có Token
                        .anyRequest().authenticated()
                )
                // Đăng ký JWT Filter đứng trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
