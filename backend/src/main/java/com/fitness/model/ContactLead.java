package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String topic;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 15, nullable = false)
    @Builder.Default
    private String status = "NEW"; // NEW, CONTACTED, CLOSED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}