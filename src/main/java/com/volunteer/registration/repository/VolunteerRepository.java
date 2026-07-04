package com.volunteer.registration.repository;

import com.volunteer.registration.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    Optional<Volunteer> findByEmail(String email);

    Optional<Volunteer> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // Find by email or phone number (for duplicate detection)
    @Query("SELECT v FROM Volunteer v WHERE v.email = :email OR v.phoneNumber = :phoneNumber")
    List<Volunteer> findByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);

    // Find existing volunteer by email or phone (excluding a specific ID for updates)
    @Query("SELECT v FROM Volunteer v WHERE (v.email = :email OR v.phoneNumber = :phoneNumber) AND v.id != :excludeId")
    List<Volunteer> findByEmailOrPhoneNumberExcludingId(@Param("email") String email, @Param("phoneNumber") String phoneNumber, @Param("excludeId") Long excludeId);

    List<Volunteer> findByActiveTrue();

    List<Volunteer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Volunteer> findBySkillsContainingIgnoreCase(String skill);

    // Search volunteers by name, email, or phone
    @Query("SELECT v FROM Volunteer v WHERE " +
           "LOWER(v.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "v.phoneNumber LIKE CONCAT('%', :search, '%')")
    List<Volunteer> searchVolunteers(@Param("search") String search);

    // Count registrations for a volunteer
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.volunteer.id = :volunteerId")
    Long countRegistrationsByVolunteerId(@Param("volunteerId") Long volunteerId);
}
