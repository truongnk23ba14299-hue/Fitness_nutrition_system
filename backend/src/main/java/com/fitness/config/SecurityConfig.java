package com.fitness.config;

import com.fitness.repository.RegisterUserRepository;
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
    public UserDetailsService userDetailsService(RegisterUserRepository userRepository) {
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
                        // Cho phép Preflight request (OPTIONS) từ trình duyệt
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Cho phép gọi API đăng ký và đăng nhập tự do mà không cần Token
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

                        // Yêu cầu phải có Token hợp lệ khi xem thông tin cá nhân
                        .requestMatchers("/api/auth/me").authenticated()

                        // Mở các API public khác để phục vụ học viên/khách xem thông tin
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/trainers/**", "/api/packages/**", "/api/equipments/**").permitAll()

                        // Tạm thời cho phép các request khác để nhóm tiện phát triển
                        .anyRequest().permitAll()
                )

                // Đăng ký JWT Filter đứng trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
