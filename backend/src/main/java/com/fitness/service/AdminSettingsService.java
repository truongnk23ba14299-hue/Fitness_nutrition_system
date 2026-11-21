package com.fitness.service;

import com.fitness.model.SystemSetting;
import com.fitness.model.User;
import com.fitness.repository.SystemSettingRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private final SystemSettingRepository settingRepository;
    private final UserRepository userRepository;

    // GET /api/admin/settings: Đọc toàn bộ tham số hệ thống
    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        List<SystemSetting> list = settingRepository.findAll();
        Map<String, String> map = new HashMap<>();

        // Thiết lập giá trị mặc định nếu database chưa có
        map.put("gym_name", "FitLife Fitness & Nutrition Club");
        map.put("address", "Số 188 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. HCM");
        map.put("hotline", "1900 8899 / 0988 111 222");
        map.put("cancel_hours_limit", "4");
        map.put("daily_pass_price", "50000");
        map.put("auto_approve_bookings", "true");

        // Ghi đè bằng dữ liệu thực tế từ database
        for (SystemSetting s : list) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }

        return map;
    }

    // PUT /api/admin/settings: Lưu danh sách tham số
    @Transactional
    public Map<String, String> updateSettings(String adminEmail, Map<String, String> newSettings) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản quản trị!"));

        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue() != null ? entry.getValue().trim() : "";

            SystemSetting setting = settingRepository.findBySettingKey(key)
                    .orElse(SystemSetting.builder().settingKey(key).build());

            setting.setSettingValue(value);
            setting.setUpdatedBy(admin);
            setting.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(setting);
        }

        return getAllSettings();
    }
}