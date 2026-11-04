package com.fitness.repository;

import com.fitness.model.WorkoutPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageDetailRepository extends JpaRepository<WorkoutPackage, Long> {
}
