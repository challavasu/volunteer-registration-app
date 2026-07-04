package com.volunteer.registration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, unique = true)
    private EmailTemplateType templateType;

    @Column(nullable = false)
    private String subject;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EmailTemplateType {
        REGISTRATION_CONFIRMATION("Registration Confirmation", "Sent when a volunteer registers for a shift"),
        REMINDER_1_DAY("1-Day Reminder", "Sent 1 day before the scheduled shift"),
        THANK_YOU("Thank You", "Sent after the event is completed");

        private final String displayName;
        private final String description;

        EmailTemplateType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}
