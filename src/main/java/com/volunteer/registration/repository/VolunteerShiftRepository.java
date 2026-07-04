package com.volunteer.registration.repository;

import com.volunteer.registration.model.VolunteerShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerShiftRepository extends JpaRepository<VolunteerShift, Long> {

    Optional<VolunteerShift> findByShiftId(String shiftId);

    List<VolunteerShift> findByVolunteerJobId(Long volunteerJobId);

    List<VolunteerShift> findByVolunteerJobIdOrderByStartDateAscStartTimeAsc(Long volunteerJobId);

    List<VolunteerShift> findByStartDate(LocalDate startDate);

    List<VolunteerShift> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM VolunteerShift s WHERE s.volunteerJob.id = :jobId AND s.startDate >= :date ORDER BY s.startDate, s.startTime")
    List<VolunteerShift> findUpcomingShiftsByJob(@Param("jobId") Long jobId, @Param("date") LocalDate date);

    @Query("SELECT s FROM VolunteerShift s WHERE s.startDate >= :date AND (s.desiredNumVolunteers IS NULL OR s.currentNumVolunteers < s.desiredNumVolunteers) ORDER BY s.startDate, s.startTime")
    List<VolunteerShift> findAvailableShifts(@Param("date") LocalDate date);

    @Query("SELECT s FROM VolunteerShift s WHERE s.volunteerJob.id = :jobId AND (s.desiredNumVolunteers IS NULL OR s.currentNumVolunteers < s.desiredNumVolunteers) ORDER BY s.startDate, s.startTime")
    List<VolunteerShift> findAvailableShiftsByJob(@Param("jobId") Long jobId);

    @Query("SELECT s FROM VolunteerShift s WHERE s.volunteerJob.campaign.id = :campaignId ORDER BY s.startDate, s.startTime")
    List<VolunteerShift> findShiftsByCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT s FROM VolunteerShift s WHERE s.volunteerJob.campaign.id = :campaignId AND s.startDate >= :date AND (s.desiredNumVolunteers IS NULL OR s.currentNumVolunteers < s.desiredNumVolunteers) ORDER BY s.startDate, s.startTime")
    List<VolunteerShift> findAvailableShiftsByCampaign(@Param("campaignId") Long campaignId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM VolunteerShift s WHERE s.volunteerJob.id = :jobId")
    long countByVolunteerJobId(@Param("jobId") Long jobId);
}
