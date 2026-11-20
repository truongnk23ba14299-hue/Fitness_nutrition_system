package com.fitness.service;

import com.fitness.dto.NotificationResponse;
import com.fitness.model.Notification;
import com.fitness.model.User;
import com.fitness.repository.NotificationRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 1. GET /api/notifications: Lấy danh sách thông báo của người dùng hiện tại
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 2. PUT /api/notifications/{id}/read: Đánh dấu 1 thông báo là đã đọc
    @Transactional
    public NotificationResponse markAsRead(Long id, String email) {
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Không tìm thấy thông báo hoặc bạn không có quyền truy cập!"));

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    // 3. PUT /api/notifications/read-all: Đánh dấu tất cả là đã đọc
    @Transactional
    public int markAllAsRead(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.markAllAsReadByUserId(user.getId());
    }

    // 4. DELETE /api/notifications/{id}: Xóa 1 thông báo
    @Transactional
    public void deleteNotification(Long id, String email) {
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Không tìm thấy thông báo hoặc bạn không có quyền truy cập!"));

        notificationRepository.delete(notification);
    }

    // Helper: Tìm User theo Email từ JWT
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Không tìm thấy tài khoản người dùng!"));
    }

    // Helper: Chuyển đổi Entity sang DTO
    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getSeverity(),
                n.getTitle(),
                n.getMessage(),
                n.getLink(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}