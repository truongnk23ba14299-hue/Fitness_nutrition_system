package com.fitness.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "booking_datetime", nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    public Booking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return user != null ? user.getId() : null; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Trainer getTrainer() { return trainer; }
    public void setTrainer(Trainer trainer) { this.trainer = trainer; }
    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(LocalDateTime bookingDateTime) { this.bookingDateTime = bookingDateTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
