package com.volunteer.registration.service;

import com.volunteer.registration.model.Campaign;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.CampaignRepository;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerJobService {

    private final VolunteerJobRepository jobRepository;
    private final CampaignRepository campaignRepository;
    private final VolunteerShiftRepository shiftRepository;

    public List<VolunteerJob> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<VolunteerJob> getActiveJobs() {
        return jobRepository.findByInactiveFalse();
    }

    public List<VolunteerJob> getAvailableJobs() {
        return jobRepository.findAvailableJobs();
    }

    public List<VolunteerJob> getJobsByCampaign(Long campaignId) {
        return jobRepository.findByCampaignIdAndInactiveFalse(campaignId);
    }

    public List<VolunteerJob> getAvailableJobsByCampaign(Long campaignId) {
        return jobRepository.findAvailableJobsByCampaign(campaignId);
    }

    public Optional<VolunteerJob> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public VolunteerJob createJob(VolunteerJob job, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + campaignId));
        job.setCampaign(campaign);
        return jobRepository.save(job);
    }

    public VolunteerJob updateJob(Long id, VolunteerJob jobDetails) {
        VolunteerJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer Job not found with id: " + id));

        job.setVolunteerJobName(jobDetails.getVolunteerJobName());
        job.setOngoing(jobDetails.isOngoing());
        job.setInactive(jobDetails.isInactive());
        job.setDisplayOnWebsite(jobDetails.isDisplayOnWebsite());
        job.setVolunteerWebsiteTimeZone(jobDetails.getVolunteerWebsiteTimeZone());
        job.setSkillsNeeded(jobDetails.getSkillsNeeded());
        job.setJobOptionSelection(jobDetails.isJobOptionSelection());
        job.setPreference(jobDetails.getPreference());
        job.setTeamLead(jobDetails.getTeamLead());
        job.setDescription(jobDetails.getDescription());
        job.setLocationStreet(jobDetails.getLocationStreet());
        job.setLocationCity(jobDetails.getLocationCity());
        job.setLocationState(jobDetails.getLocationState());
        job.setLocationZip(jobDetails.getLocationZip());
        job.setLocationInformation(jobDetails.getLocationInformation());

        // Update campaign if provided
        if (jobDetails.getCampaign() != null && jobDetails.getCampaign().getId() != null) {
            Campaign campaign = campaignRepository.findById(jobDetails.getCampaign().getId())
                    .orElseThrow(() -> new RuntimeException("Campaign not found"));
            job.setCampaign(campaign);
        }

        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public void deactivateJob(Long id) {
        VolunteerJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer Job not found with id: " + id));
        job.setInactive(true);
        jobRepository.save(job);
    }

    public List<VolunteerJob> searchJobs(String name) {
        return jobRepository.findByVolunteerJobNameContainingIgnoreCaseAndInactiveFalse(name);
    }

    public List<VolunteerJob> getOngoingJobs() {
        return jobRepository.findByOngoingTrue();
    }

    public List<VolunteerJob> getJobsByTeamLead(String teamLead) {
        return jobRepository.findByTeamLead(teamLead);
    }

    public VolunteerJob cloneJob(Long originalJobId, String newJobName) {
        VolunteerJob original = jobRepository.findById(originalJobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + originalJobId));

        // Create new job
        VolunteerJob newJob = new VolunteerJob();
        newJob.setVolunteerJobName(newJobName);
        newJob.setCampaign(original.getCampaign());
        newJob.setDescription(original.getDescription());
        newJob.setLocationStreet(original.getLocationStreet());
        newJob.setLocationCity(original.getLocationCity());
        newJob.setLocationState(original.getLocationState());
        newJob.setLocationZip(original.getLocationZip());
        newJob.setLocationInformation(original.getLocationInformation());
        newJob.setSkillsNeeded(original.getSkillsNeeded());
        newJob.setTeamLead(original.getTeamLead());
        newJob.setPreference(original.getPreference());
        newJob.setJobOptionSelection(original.isJobOptionSelection());
        newJob.setDisplayOnWebsite(original.isDisplayOnWebsite());
        newJob.setOngoing(original.isOngoing());
        newJob.setInactive(false);
        newJob.setVolunteerWebsiteTimeZone(original.getVolunteerWebsiteTimeZone());

        VolunteerJob savedJob = jobRepository.save(newJob);

        // Clone all shifts
        List<VolunteerShift> originalShifts = shiftRepository.findByVolunteerJobId(originalJobId);
        for (VolunteerShift originalShift : originalShifts) {
            VolunteerShift newShift = new VolunteerShift();
            newShift.setVolunteerJob(savedJob);
            newShift.setShiftId(null); // Will be auto-generated
            newShift.setStartDate(originalShift.getStartDate());
            newShift.setStartTime(originalShift.getStartTime());
            newShift.setDurationHours(originalShift.getDurationHours());
            newShift.setDescription(originalShift.getDescription());
            newShift.setDesiredNumVolunteers(originalShift.getDesiredNumVolunteers());
            newShift.setCurrentNumVolunteers(0); // Reset volunteer count
            
            shiftRepository.save(newShift);
        }

        return savedJob;
    }
}
