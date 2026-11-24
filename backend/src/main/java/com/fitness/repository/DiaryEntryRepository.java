package com.fitness.repository;

import com.fitness.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {
    List<DiaryEntry> findByUser_IdAndEntryDateOrderByLoggedTimeAscIdAsc(Long userId, LocalDate entryDate);
}