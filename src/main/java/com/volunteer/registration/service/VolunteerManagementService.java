package com.volunteer.registration.service;

import com.volunteer.registration.dto.VolunteerRegistrationDTO;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerManagementService {

    private final VolunteerRepository volunteerRepository;

    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }

    public List<Volunteer> getActiveVolunteers() {
        return volunteerRepository.findByActiveTrue();
    }

    public Optional<Volunteer> getVolunteerById(Long id) {
        return volunteerRepository.findById(id);
    }

    public Optional<Volunteer> getVolunteerByEmail(String email) {
        return volunteerRepository.findByEmail(email);
    }

    public Optional<Volunteer> getVolunteerByPhone(String phoneNumber) {
        return volunteerRepository.findByPhoneNumber(phoneNumber);
    }

    public boolean emailExists(String email) {
        return volunteerRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phoneNumber) {
        return volunteerRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * Find existing volunteer by email or phone number.
     * Returns the existing volunteer if found, allowing updates instead of duplicates.
     */
    public Optional<Volunteer> findExistingVolunteer(String email, String phoneNumber) {
        // First check by email
        Optional<Volunteer> byEmail = volunteerRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        
        // Then check by phone (if provided)
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            String normalizedPhone = normalizePhoneNumber(phoneNumber);
            return volunteerRepository.findByPhoneNumber(normalizedPhone);
        }
        
        return Optional.empty();
    }

    /**
     * Register a new volunteer or return existing one if email/phone already exists.
     * This ensures unique volunteers based on email AND phone.
     */
    public Volunteer registerOrFindVolunteer(VolunteerRegistrationDTO dto) {
        String normalizedPhone = normalizePhoneNumber(dto.getPhoneNumber());
        
        // Check for existing volunteer
        Optional<Volunteer> existingByEmail = volunteerRepository.findByEmail(dto.getEmail());
        if (existingByEmail.isPresent()) {
            // Update existing volunteer's info if needed
            Volunteer existing = existingByEmail.get();
            updateVolunteerInfo(existing, dto, normalizedPhone);
            return volunteerRepository.save(existing);
        }
        
        // Check by phone number
        if (normalizedPhone != null && !normalizedPhone.isEmpty()) {
            Optional<Volunteer> existingByPhone = volunteerRepository.findByPhoneNumber(normalizedPhone);
            if (existingByPhone.isPresent()) {
                // Update email and other info
                Volunteer existing = existingByPhone.get();
                existing.setEmail(dto.getEmail()); // Update email
                updateVolunteerInfo(existing, dto, normalizedPhone);
                return volunteerRepository.save(existing);
            }
        }
        
        // Create new volunteer
        Volunteer volunteer = new Volunteer();
        volunteer.setFirstName(dto.getFirstName());
        volunteer.setLastName(dto.getLastName());
        volunteer.setEmail(dto.getEmail());
        volunteer.setPhoneNumber(normalizedPhone);
        volunteer.setAddress(dto.getAddress());
        volunteer.setCity(dto.getCity());
        volunteer.setState(dto.getState());
        volunteer.setZipCode(dto.getZipCode());
        volunteer.setEmergencyContactName(dto.getEmergencyContactName());
        volunteer.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        volunteer.setSkills(dto.getSkills());
        volunteer.setInterests(dto.getInterests());
        volunteer.setActive(true);

        return volunteerRepository.save(volunteer);
    }

    private void updateVolunteerInfo(Volunteer volunteer, VolunteerRegistrationDTO dto, String normalizedPhone) {
        // Update name if changed
        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
            volunteer.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isEmpty()) {
            volunteer.setLastName(dto.getLastName());
        }
        // Update phone if not set
        if ((volunteer.getPhoneNumber() == null || volunteer.getPhoneNumber().isEmpty()) && normalizedPhone != null) {
            volunteer.setPhoneNumber(normalizedPhone);
        }
        // Update other fields if provided
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            volunteer.setSkills(dto.getSkills());
        }
    }

    /**
     * Normalize phone number for consistent storage and comparison.
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        // Remove all non-digit characters
        return phone.replaceAll("[^0-9]", "");
    }

    public Volunteer registerVolunteer(VolunteerRegistrationDTO dto) {
        // Use the new method that handles duplicates
        return registerOrFindVolunteer(dto);
    }

    public Volunteer updateVolunteer(Long id, VolunteerRegistrationDTO dto) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));

        String normalizedPhone = normalizePhoneNumber(dto.getPhoneNumber());

        // Check for conflicts with other volunteers (excluding this one)
        if (dto.getEmail() != null && !dto.getEmail().equals(volunteer.getEmail())) {
            if (volunteerRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already in use by another volunteer");
            }
            volunteer.setEmail(dto.getEmail());
        }

        if (normalizedPhone != null && !normalizedPhone.equals(volunteer.getPhoneNumber())) {
            Optional<Volunteer> existingPhone = volunteerRepository.findByPhoneNumber(normalizedPhone);
            if (existingPhone.isPresent() && !existingPhone.get().getId().equals(id)) {
                throw new RuntimeException("Phone number already in use by another volunteer");
            }
            volunteer.setPhoneNumber(normalizedPhone);
        }

        volunteer.setFirstName(dto.getFirstName());
        volunteer.setLastName(dto.getLastName());
        volunteer.setAddress(dto.getAddress());
        volunteer.setCity(dto.getCity());
        volunteer.setState(dto.getState());
        volunteer.setZipCode(dto.getZipCode());
        volunteer.setEmergencyContactName(dto.getEmergencyContactName());
        volunteer.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        volunteer.setSkills(dto.getSkills());
        volunteer.setInterests(dto.getInterests());

        return volunteerRepository.save(volunteer);
    }

    public Volunteer updateVolunteerEntity(Long id, Volunteer volunteerDetails) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));

        String normalizedPhone = normalizePhoneNumber(volunteerDetails.getPhoneNumber());

        // Check for email conflict
        if (volunteerDetails.getEmail() != null && !volunteerDetails.getEmail().equals(volunteer.getEmail())) {
            if (volunteerRepository.existsByEmail(volunteerDetails.getEmail())) {
                throw new RuntimeException("Email already in use by another volunteer");
            }
            volunteer.setEmail(volunteerDetails.getEmail());
        }

        // Check for phone conflict
        if (normalizedPhone != null && !normalizedPhone.equals(volunteer.getPhoneNumber())) {
            Optional<Volunteer> existingPhone = volunteerRepository.findByPhoneNumber(normalizedPhone);
            if (existingPhone.isPresent() && !existingPhone.get().getId().equals(id)) {
                throw new RuntimeException("Phone number already in use by another volunteer");
            }
            volunteer.setPhoneNumber(normalizedPhone);
        }

        volunteer.setFirstName(volunteerDetails.getFirstName());
        volunteer.setLastName(volunteerDetails.getLastName());
        volunteer.setAddress(volunteerDetails.getAddress());
        volunteer.setCity(volunteerDetails.getCity());
        volunteer.setState(volunteerDetails.getState());
        volunteer.setZipCode(volunteerDetails.getZipCode());
        volunteer.setEmergencyContactName(volunteerDetails.getEmergencyContactName());
        volunteer.setEmergencyContactPhone(volunteerDetails.getEmergencyContactPhone());
        volunteer.setSkills(volunteerDetails.getSkills());
        volunteer.setInterests(volunteerDetails.getInterests());
        volunteer.setActive(volunteerDetails.isActive());

        return volunteerRepository.save(volunteer);
    }

    public void deleteVolunteer(Long id) {
        volunteerRepository.deleteById(id);
    }

    public void deactivateVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));
        volunteer.setActive(false);
        volunteerRepository.save(volunteer);
    }

    public List<Volunteer> searchVolunteers(String query) {
        return volunteerRepository.searchVolunteers(query);
    }

    public List<Volunteer> findVolunteersBySkill(String skill) {
        return volunteerRepository.findBySkillsContainingIgnoreCase(skill);
    }

    public Long getRegistrationCount(Long volunteerId) {
        return volunteerRepository.countRegistrationsByVolunteerId(volunteerId);
    }
}
