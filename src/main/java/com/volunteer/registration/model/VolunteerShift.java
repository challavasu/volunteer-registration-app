package com.volunteer.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "volunteer_shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", unique = true)
    private String shiftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_job_id", nullable = false)
    @JsonIgnoreProperties({"shifts", "registrations", "campaign", "hibernateLazyInitializer", "handler"})
    private VolunteerJob volunteerJob;

    @NotNull(message = "Start Date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Start Time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    @Column(name = "duration_hours", nullable = false)
    private Double durationHours;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "desired_num_volunteers")
    private Integer desiredNumVolunteers;

    @Column(name = "current_num_volunteers")
    private Integer currentNumVolunteers = 0;

    @OneToMany(mappedBy = "volunteerShift", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Registration> registrations = new ArrayList<>();

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(startDate, startTime);
    }

    public LocalDateTime getEndDateTime() {
        return getStartDateTime().plusMinutes((long) (durationHours * 60));
    }

    public LocalTime getEndTime() {
        return startTime.plusMinutes((long) (durationHours * 60));
    }

    public boolean hasAvailableSlots() {
        int registeredCount = getRegisteredVolunteerCount();
        return desiredNumVolunteers == null || registeredCount < desiredNumVolunteers;
    }

    // Getter for JSON serialization (Jackson needs "is" or "get" prefix)
    public boolean isHasAvailableSlots() {
        return hasAvailableSlots();
    }

    public int getAvailableSlots() {
        if (desiredNumVolunteers == null) return Integer.MAX_VALUE;
        int registeredCount = getRegisteredVolunteerCount();
        return Math.max(0, desiredNumVolunteers - registeredCount);
    }

    // Calculate current registered volunteers from registrations list
    public int getRegisteredVolunteerCount() {
        if (registrations == null || registrations.isEmpty()) {
            return 0;
        }
        // Count only confirmed registrations (not cancelled or no-show)
        return (int) registrations.stream()
            .filter(r -> r.getStatus() != null && !r.getStatus().toString().equals("CANCELLED") && !r.getStatus().toString().equals("NO_SHOW"))
            .count();
    }

    public String getFormattedTimeSlot() {
        return String.format("%s %s - %s (%s hrs)", 
            startDate.toString(),
            startTime.toString(),
            getEndTime().toString(),
            durationHours);
    }

    @PrePersist
    protected void onCreate() {
        if (shiftId == null || shiftId.isEmpty()) {
            shiftId = "SHIFT-" + System.currentTimeMillis();
        }
        if (currentNumVolunteers == null) {
            currentNumVolunteers = 0;
        }
    }
}
