package com.volunteer.registration.service;

import com.volunteer.registration.dto.ShiftRegistrationDTO;
import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Registration.RegistrationStatus;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.RegistrationRepository;
import com.volunteer.registration.repository.VolunteerRepository;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerJobRepository jobRepository;
    private final VolunteerShiftRepository shiftRepository;
    private final VolunteerShiftService shiftService;
    private final EmailService emailService;

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAllWithDetails();
    }

    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }

    public List<Registration> getRegistrationsByVolunteer(Long volunteerId) {
        return registrationRepository.findByVolunteerId(volunteerId);
    }

    public List<Registration> getRegistrationsByVolunteerEmail(String email) {
        return registrationRepository.findByVolunteerEmail(email);
    }

    public List<Registration> getRegistrationsByJob(Long jobId) {
        return registrationRepository.findByVolunteerJobId(jobId);
    }

    public List<Registration> getRegistrationsByShift(Long shiftId) {
        return registrationRepository.findByShiftIdWithDetails(shiftId);
    }

    public List<Registration> getRegistrationsByCampaign(Long campaignId) {
        return registrationRepository.findByCampaignIdWithDetails(campaignId);
    }

    public Registration registerForShift(ShiftRegistrationDTO dto) {
        // Find volunteer by email
        Volunteer volunteer = volunteerRepository.findByEmail(dto.getVolunteerEmail())
                .orElseThrow(() -> new RuntimeException("Volunteer not found with email: " + dto.getVolunteerEmail()));

        // Find shift
        VolunteerShift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with id: " + dto.getShiftId()));

        // Get the job from the shift
        VolunteerJob job = shift.getVolunteerJob();

        // Check if already registered
        Optional<Registration> existingReg = registrationRepository.findByVolunteerIdAndVolunteerShiftId(volunteer.getId(), shift.getId());
        if (existingReg.isPresent()) {
            Registration existing = existingReg.get();
            // If cancelled, allow re-registration
            if (existing.getStatus() == RegistrationStatus.CANCELLED) {
                // Check availability before reactivating
                if (!shift.hasAvailableSlots()) {
                    throw new RuntimeException("No available slots for this shift");
                }
                existing.setStatus(RegistrationStatus.PENDING);
                existing.setNotes(dto.getNotes());
                Registration reactivated = registrationRepository.save(existing);
                shiftService.incrementVolunteerCount(shift.getId());
                return reactivated;
            }
            // Already actively registered - throw error
            throw new RuntimeException("You are already registered for this shift. Please cancel the existing registration first if you want to make changes.");
        }

        // Check availability
        if (!shift.hasAvailableSlots()) {
            throw new RuntimeException("No available slots for this shift");
        }

        // Create registration
        Registration registration = new Registration();
        registration.setVolunteer(volunteer);
        registration.setVolunteerJob(job);
        registration.setVolunteerShift(shift);
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setNotes(dto.getNotes());

        Registration savedRegistration = registrationRepository.save(registration);

        // Increment volunteer count
        shiftService.incrementVolunteerCount(shift.getId());

        return savedRegistration;
    }

    public Registration confirmRegistration(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setConfirmedDate(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    public Registration cancelRegistration(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new RuntimeException("Registration is already cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledDate(LocalDateTime.now());

        // Decrement volunteer count
        shiftService.decrementVolunteerCount(registration.getVolunteerShift().getId());

        return registrationRepository.save(registration);
    }

    public Registration completeRegistration(Long registrationId, Double hoursWorked) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        registration.setStatus(RegistrationStatus.COMPLETED);
        registration.setCompletedDate(LocalDateTime.now());
        registration.setHoursWorked(hoursWorked != null ? hoursWorked : registration.getVolunteerShift().getDurationHours());

        Registration saved = registrationRepository.save(registration);
        
        // Send thank you email if not already sent
        if (!saved.isThankYouEmailSent()) {
            try {
                emailService.sendThankYou(saved);
                saved.setThankYouEmailSent(true);
                registrationRepository.save(saved);
                log.info("Sent thank you email for registration {}", registrationId);
            } catch (Exception e) {
                log.error("Failed to send thank you email for registration {}: {}", registrationId, e.getMessage());
            }
        }

        return saved;
    }

    public Registration markNoShow(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        registration.setStatus(RegistrationStatus.NO_SHOW);

        // Decrement volunteer count
        shiftService.decrementVolunteerCount(registration.getVolunteerShift().getId());

        return registrationRepository.save(registration);
    }

    public boolean isVolunteerRegisteredForShift(Long volunteerId, Long shiftId) {
        return registrationRepository.existsByVolunteerIdAndVolunteerShiftId(volunteerId, shiftId);
    }

    public long getActiveRegistrationCountForShift(Long shiftId) {
        return registrationRepository.countActiveByShiftId(shiftId);
    }

    public List<Registration> getRegistrationsByStatus(RegistrationStatus status) {
        return registrationRepository.findByStatus(status);
    }

    public Double getTotalHoursWorkedByVolunteer(Long volunteerId) {
        return registrationRepository.getTotalHoursWorkedByVolunteer(volunteerId);
    }

    /**
     * Send registration confirmation email to volunteer for all their recent registrations.
     * This should be called after all shift registrations are complete.
     */
    public void sendConfirmationEmail(String volunteerEmail) {
        List<Registration> registrations = registrationRepository.findByVolunteerEmail(volunteerEmail);
        
        // Filter to only include registrations that haven't had confirmation sent
        List<Registration> pendingConfirmation = registrations.stream()
                .filter(r -> !r.isConfirmationEmailSent() && 
                             r.getStatus() != RegistrationStatus.CANCELLED &&
                             r.getStatus() != RegistrationStatus.NO_SHOW)
                .toList();
        
        if (pendingConfirmation.isEmpty()) {
            log.info("No pending registrations to send confirmation email for: {}", volunteerEmail);
            return;
        }

        try {
            emailService.sendRegistrationConfirmations(pendingConfirmation);
            
            // Mark confirmations as sent
            for (Registration reg : pendingConfirmation) {
                reg.setConfirmationEmailSent(true);
                registrationRepository.save(reg);
            }
            
            log.info("Sent confirmation email for {} registrations to {}", pendingConfirmation.size(), volunteerEmail);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}: {}", volunteerEmail, e.getMessage());
        }
    }
}
