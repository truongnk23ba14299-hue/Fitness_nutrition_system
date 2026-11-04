package com.fitness.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // ví dụ: "ROLE_CLIENT", "ROLE_ADMIN", "ROLE_PT", "ROLE_RECEPTION"

    public Role(String name) {
        this.name = name;
    }
}
