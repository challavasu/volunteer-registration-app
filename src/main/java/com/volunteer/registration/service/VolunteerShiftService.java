package com.volunteer.registration.service;

import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerShiftService {

    private final VolunteerShiftRepository shiftRepository;
    private final VolunteerJobRepository jobRepository;

    public List<VolunteerShift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Optional<VolunteerShift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    public Optional<VolunteerShift> getShiftByShiftId(String shiftId) {
        return shiftRepository.findByShiftId(shiftId);
    }

    public List<VolunteerShift> getShiftsByJob(Long jobId) {
        return shiftRepository.findByVolunteerJobIdOrderByStartDateAscStartTimeAsc(jobId);
    }

    public List<VolunteerShift> getUpcomingShiftsByJob(Long jobId) {
        return shiftRepository.findUpcomingShiftsByJob(jobId, LocalDate.now());
    }

    public List<VolunteerShift> getAvailableShifts() {
        return shiftRepository.findAvailableShifts(LocalDate.now());
    }

    public List<VolunteerShift> getAvailableShiftsByJob(Long jobId) {
        return shiftRepository.findAvailableShiftsByJob(jobId);
    }

    public List<VolunteerShift> getShiftsByCampaign(Long campaignId) {
        return shiftRepository.findShiftsByCampaign(campaignId);
    }

    public List<VolunteerShift> getAvailableShiftsByCampaign(Long campaignId) {
        return shiftRepository.findAvailableShiftsByCampaign(campaignId, LocalDate.now());
    }

    public List<VolunteerShift> getShiftsByDateRange(LocalDate startDate, LocalDate endDate) {
        return shiftRepository.findByStartDateBetween(startDate, endDate);
    }

    public VolunteerShift createShift(VolunteerShift shift, Long jobId) {
        VolunteerJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Volunteer Job not found with id: " + jobId));
        shift.setVolunteerJob(job);
        return shiftRepository.save(shift);
    }

    public VolunteerShift updateShift(Long id, VolunteerShift shiftDetails) {
        VolunteerShift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer Shift not found with id: " + id));

        shift.setStartDate(shiftDetails.getStartDate());
        shift.setStartTime(shiftDetails.getStartTime());
        shift.setDurationHours(shiftDetails.getDurationHours());
        shift.setDescription(shiftDetails.getDescription());
        shift.setDesiredNumVolunteers(shiftDetails.getDesiredNumVolunteers());

        // Update job if provided
        if (shiftDetails.getVolunteerJob() != null && shiftDetails.getVolunteerJob().getId() != null) {
            VolunteerJob job = jobRepository.findById(shiftDetails.getVolunteerJob().getId())
                    .orElseThrow(() -> new RuntimeException("Volunteer Job not found"));
            shift.setVolunteerJob(job);
        }

        return shiftRepository.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    public void incrementVolunteerCount(Long shiftId) {
        VolunteerShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Volunteer Shift not found with id: " + shiftId));
        shift.setCurrentNumVolunteers(shift.getCurrentNumVolunteers() + 1);
        shiftRepository.save(shift);
    }

    public void decrementVolunteerCount(Long shiftId) {
        VolunteerShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Volunteer Shift not found with id: " + shiftId));
        if (shift.getCurrentNumVolunteers() > 0) {
            shift.setCurrentNumVolunteers(shift.getCurrentNumVolunteers() - 1);
            shiftRepository.save(shift);
        }
    }

    public long getShiftCountByJob(Long jobId) {
        return shiftRepository.countByVolunteerJobId(jobId);
    }
}
