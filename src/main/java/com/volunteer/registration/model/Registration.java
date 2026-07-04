package com.volunteer.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"volunteer_id", "volunteer_shift_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    @JsonIgnoreProperties({"registrations", "hibernateLazyInitializer", "handler"})
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_job_id", nullable = false)
    @JsonIgnoreProperties({"shifts", "registrations", "description", "locationInformation", "hibernateLazyInitializer", "handler"})
    private VolunteerJob volunteerJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_shift_id", nullable = false)
    @JsonIgnoreProperties({"registrations", "volunteerJob", "hibernateLazyInitializer", "handler"})
    private VolunteerShift volunteerShift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(length = 500)
    private String notes;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "hours_worked")
    private Double hoursWorked;

    @Column(name = "check_in_code", unique = true, length = 8)
    private String checkInCode;

    @Column(name = "checked_in", columnDefinition = "boolean default false")
    private Boolean checkedIn = false;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "reminder_sent", columnDefinition = "boolean default false")
    private Boolean reminderSent = false;

    @Column(name = "confirmation_email_sent", columnDefinition = "boolean default false")
    private Boolean confirmationEmailSent = false;

    @Column(name = "thank_you_email_sent", columnDefinition = "boolean default false")
    private Boolean thankYouEmailSent = false;

    public boolean isCheckedIn() {
        return checkedIn != null && checkedIn;
    }

    public boolean isReminderSent() {
        return reminderSent != null && reminderSent;
    }

    public boolean isConfirmationEmailSent() {
        return confirmationEmailSent != null && confirmationEmailSent;
    }

    public boolean isThankYouEmailSent() {
        return thankYouEmailSent != null && thankYouEmailSent;
    }

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        if (checkInCode == null) {
            checkInCode = generateCheckInCode();
        }
    }

    private String generateCheckInCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public enum RegistrationStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed"),
        NO_SHOW("No Show");

        private final String displayName;

        RegistrationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
