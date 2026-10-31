package com.fitness.model;

import jakarta.persistence.*;

@Entity
@Table(name = "packages")
public class PackageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String schedule;

    @Column(name = "trainer_info", columnDefinition = "TEXT")
    private String trainerInfo;

    public PackageDetail() {
    }

    public PackageDetail(String benefits, String schedule, String trainerInfo) {
        this.benefits = benefits;
        this.schedule = schedule;
        this.trainerInfo = trainerInfo;
    }

    public Long getId() {
        return id;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getTrainerInfo() {
        return trainerInfo;
    }

    public void setTrainerInfo(String trainerInfo) {
        this.trainerInfo = trainerInfo;
    }
}
