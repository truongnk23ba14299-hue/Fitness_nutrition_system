package com.fitness.repository;

import com.fitness.model.ContactLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactLeadRepository extends JpaRepository<ContactLead, Long> {

    // Lấy danh sách khách đăng ký tư vấn mới nhất xếp lên đầu
    List<ContactLead> findAllByOrderByCreatedAtDesc();
}