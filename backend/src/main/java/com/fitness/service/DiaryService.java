package com.fitness.service;

import com.fitness.dto.DiaryEntryRequest;
import com.fitness.dto.DiaryEntryResponse;
import com.fitness.model.DiaryEntry;
import com.fitness.model.User;
import com.fitness.repository.DiaryEntryRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final UserRepository userRepository;

    public List<DiaryEntryResponse> getDiaryByDate(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return diaryEntryRepository.findByUser_IdAndEntryDateOrderByLoggedTimeAscIdAsc(user.getId(), targetDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DiaryEntryResponse createDiaryEntry(String email, DiaryEntryRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String normalizedType = request.getEntryType().trim().toUpperCase(Locale.ROOT);
        if (!"MEAL".equals(normalizedType) && !"WORKOUT".equals(normalizedType)) {
            throw new IllegalArgumentException("entryType chỉ chấp nhận MEAL hoặc WORKOUT");
        }

        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .entryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now())
                .entryType(normalizedType)
                .title(request.getTitle().trim())
                .calories(request.getCalories())
                .loggedTime(request.getLoggedTime())
                .build();

        return toResponse(diaryEntryRepository.save(entry));
    }

    public void deleteDiaryEntry(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng nhật ký với id = " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa dòng nhật ký này");
        }

        diaryEntryRepository.delete(entry);
    }

    private DiaryEntryResponse toResponse(DiaryEntry entry) {
        return DiaryEntryResponse.builder()
                .id(entry.getId())
                .entryDate(entry.getEntryDate())
                .entryType(entry.getEntryType())
                .title(entry.getTitle())
                .calories(entry.getCalories())
                .loggedTime(entry.getLoggedTime())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}