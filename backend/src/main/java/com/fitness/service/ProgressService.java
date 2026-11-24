package com.fitness.service;

import com.fitness.dto.BodyRecordResponse;
import com.fitness.model.BodyRecord;
import com.fitness.model.User;
import com.fitness.repository.BodyRecordRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final BodyRecordRepository bodyRecordRepository;
    private final UserRepository userRepository;

    // 1. GET /api/progress/my-records: Lấy lịch sử chỉ số InBody của học viên hiện tại
    @Transactional(readOnly = true)
    public List<BodyRecordResponse> getMyRecords(String email) {
        User user = getUserByEmail(email);
        return bodyRecordRepository.findByUser_IdOrderByRecordDateDescIdDesc(user.getId())
                .stream()
                .map(BodyRecordResponse::fromEntity)
                .toList();
    }

    // 2. DELETE /api/progress/my-records/{id}: Xóa 1 bản ghi InBody bị nhập nhầm
    @Transactional
    public void deleteMyRecord(Long id, String email) {
        User user = getUserByEmail(email);
        BodyRecord record = bodyRecordRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy bản ghi chỉ số InBody hoặc bạn không có quyền xóa!"));

        bodyRecordRepository.delete(record);
    }

    // Helper: Tìm User theo Email từ JWT
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng!"));
    }
}
