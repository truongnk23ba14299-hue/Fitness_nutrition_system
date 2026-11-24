package com.fitness.service;

import com.fitness.Exception.ResourceNotFoundException;
import com.fitness.dto.DiaryEntryRequest;
import com.fitness.dto.DiaryEntryResponse;
import com.fitness.model.DiaryEntry;
import com.fitness.model.User;
import com.fitness.repository.DiaryEntryRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DiaryEntryResponse> getDiaryByDate(String email, LocalDate date) {
        User user = getUserByEmail(email);
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return diaryEntryRepository
                .findByUser_IdAndEntryDateOrderByLoggedTimeAscIdAsc(user.getId(), targetDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DiaryEntryResponse createDiaryEntry(String email, DiaryEntryRequest request) {
        User user = getUserByEmail(email);
        String entryType = request.getEntryType().trim().toUpperCase(Locale.ROOT);

        if (!entryType.equals("MEAL") && !entryType.equals("WORKOUT")) {
            throw new IllegalArgumentException(
                    "Loại nhật ký chỉ được là MEAL hoặc WORKOUT"
            );
        }

        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .entryDate(request.getEntryDate() != null
                        ? request.getEntryDate() : LocalDate.now())
                .entryType(entryType)
                .title(request.getTitle().trim())
                .calories(request.getCalories())
                .loggedTime(request.getLoggedTime())
                .build();

        return toResponse(diaryEntryRepository.save(entry));
    }

    @Transactional
    public void deleteDiaryEntry(String email, Long id) {
        User user = getUserByEmail(email);

        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhật ký với id = " + id
                ));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền xóa nhật ký này"
            );
        }

        diaryEntryRepository.delete(entry);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản với email: " + email
                ));
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
