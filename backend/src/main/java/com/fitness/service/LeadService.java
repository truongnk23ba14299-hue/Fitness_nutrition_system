package com.fitness.service;

import com.fitness.dto.LeadRequest;
import com.fitness.dto.LeadResponse;
import com.fitness.model.ContactLead;
import com.fitness.repository.ContactLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final ContactLeadRepository leadRepository;

    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        ContactLead lead = ContactLead.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phone(request.getPhone().trim())
                .topic(request.getTopic() != null ? request.getTopic().trim() : "Tư vấn chung")
                .message(request.getMessage() != null ? request.getMessage().trim() : "Khách hàng để lại thông tin tư vấn")
                .status("NEW")
                .build();

        ContactLead saved = leadRepository.save(lead);

        return LeadResponse.builder()
                .id(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .topic(saved.getTopic())
                .message(saved.getMessage())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .responseMessage("Gửi yêu cầu tư vấn thành công! Nhân viên chăm sóc khách hàng sẽ liên hệ với bạn trong vòng 24h.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<ContactLead> getAllLeads() {
        return leadRepository.findAllByOrderByCreatedAtDesc();
    }
}