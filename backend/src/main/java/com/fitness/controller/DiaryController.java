package com.fitness.controller;

import com.fitness.dto.DiaryEntryRequest;
import com.fitness.dto.DiaryEntryResponse;
import com.fitness.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public ResponseEntity<List<DiaryEntryResponse>> getDiaryByDate(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                diaryService.getDiaryByDate(authentication.getName(), date)
        );
    }

    @PostMapping
    public ResponseEntity<DiaryEntryResponse> createDiaryEntry(
            Authentication authentication,
            @Valid @RequestBody DiaryEntryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(diaryService.createDiaryEntry(authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDiaryEntry(
            Authentication authentication,
            @PathVariable Long id
    ) {
        diaryService.deleteDiaryEntry(authentication.getName(), id);
        return ResponseEntity.ok(
                Map.of("message", "Xóa nhật ký thành công")
        );
    }
}
