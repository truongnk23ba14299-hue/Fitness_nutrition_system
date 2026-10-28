package com.usth.fitness_project.repository;

import com.usth.fitness_project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm vai trò theo tên (ví dụ: "ROLE_CLIENT", "ROLE_ADMIN").
     */
    Optional<Role> findByName(String name);
}
