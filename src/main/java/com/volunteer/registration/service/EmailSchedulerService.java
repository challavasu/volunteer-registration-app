package com.volunteer.registration.service;

import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Registration.RegistrationStatus;
import com.volunteer.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSchedulerService {

    private final RegistrationRepository registrationRepository;
    private final EmailService emailService;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Scheduled(cron = "${app.email.reminder.cron:0 0 9 * * ?}")
    @Transactional
    public void sendDailyReminders() {
        if (!emailEnabled) {
            log.info("Email is disabled. Skipping daily reminder job.");
            return;
        }

        log.info("Starting daily reminder email job...");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        List<Registration> registrations = registrationRepository.findRegistrationsForReminder(tomorrow);
        
        log.info("Found {} registrations needing reminders for {}", registrations.size(), tomorrow);

        int sentCount = 0;
        int errorCount = 0;

        for (Registration registration : registrations) {
            try {
                if (registration.getStatus() == RegistrationStatus.CANCELLED ||
                    registration.getStatus() == RegistrationStatus.NO_SHOW) {
                    continue;
                }

                emailService.sendReminder(registration);
                
                registration.setReminderSent(true);
                registrationRepository.save(registration);
                
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send reminder for registration {}: {}", 
                        registration.getId(), e.getMessage());
                errorCount++;
            }
        }

        log.info("Daily reminder job completed. Sent: {}, Errors: {}", sentCount, errorCount);
    }

    public void sendReminderManually(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        emailService.sendReminder(registration);
        
        registration.setReminderSent(true);
        registrationRepository.save(registration);
        
        log.info("Manual reminder sent for registration {}", registrationId);
    }
}
