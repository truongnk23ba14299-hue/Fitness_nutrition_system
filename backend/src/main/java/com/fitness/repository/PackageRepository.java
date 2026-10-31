package com.example.demo.repository;

import com.example.demo.model.WorkoutPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<WorkoutPackage, Long> {
   //get list are active
    List<WorkoutPackage> findByIsActiveTrue();
}