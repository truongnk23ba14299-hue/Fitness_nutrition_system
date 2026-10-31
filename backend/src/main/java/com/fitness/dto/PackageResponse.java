package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageResponse {
    private Long id;
    private String title;
    private Double price;
    private Integer durationMonths;
    private String description;
    private String imageUrl;
}
