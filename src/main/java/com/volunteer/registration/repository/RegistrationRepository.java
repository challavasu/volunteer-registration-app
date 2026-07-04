package com.volunteer.registration.repository;

import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Registration.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByVolunteerId(Long volunteerId);

    List<Registration> findByVolunteerJobId(Long volunteerJobId);

    List<Registration> findByVolunteerShiftId(Long volunteerShiftId);

    List<Registration> findByStatus(RegistrationStatus status);

    Optional<Registration> findByVolunteerIdAndVolunteerShiftId(Long volunteerId, Long volunteerShiftId);

    boolean existsByVolunteerIdAndVolunteerShiftId(Long volunteerId, Long volunteerShiftId);

    @Query("SELECT r FROM Registration r WHERE r.volunteer.id = :volunteerId AND r.status = :status")
    List<Registration> findByVolunteerIdAndStatus(@Param("volunteerId") Long volunteerId, 
                                                   @Param("status") RegistrationStatus status);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.volunteerShift.id = :shiftId AND r.status = :status")
    long countByShiftIdAndStatus(@Param("shiftId") Long shiftId, 
                                  @Param("status") RegistrationStatus status);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.volunteerShift.id = :shiftId AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countActiveByShiftId(@Param("shiftId") Long shiftId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v WHERE r.volunteer.email = :email")
    List<Registration> findByVolunteerEmail(@Param("email") String email);

    @Query("SELECT r FROM Registration r WHERE r.volunteerJob.campaign.id = :campaignId")
    List<Registration> findByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT SUM(r.hoursWorked) FROM Registration r WHERE r.volunteer.id = :volunteerId AND r.status = 'COMPLETED'")
    Double getTotalHoursWorkedByVolunteer(@Param("volunteerId") Long volunteerId);

    Optional<Registration> findByCheckInCode(String checkInCode);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v " +
           "WHERE r.volunteerJob.campaign.id = :campaignId AND r.checkedIn = false AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Registration> findPendingCheckInsByCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v " +
           "WHERE r.volunteerJob.campaign.id = :campaignId AND r.checkedIn = true")
    List<Registration> findCheckedInByCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v " +
           "WHERE r.volunteerShift.id = :shiftId")
    List<Registration> findByShiftIdWithDetails(@Param("shiftId") Long shiftId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v " +
           "WHERE r.volunteerJob.campaign.id = :campaignId")
    List<Registration> findByCampaignIdWithDetails(@Param("campaignId") Long campaignId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v")
    List<Registration> findAllWithDetails();

    @Query("SELECT r FROM Registration r JOIN FETCH r.volunteerShift s JOIN FETCH r.volunteerJob j JOIN FETCH r.volunteer v " +
           "WHERE s.startDate = :shiftDate AND r.reminderSent = false AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Registration> findRegistrationsForReminder(@Param("shiftDate") LocalDate shiftDate);
}
