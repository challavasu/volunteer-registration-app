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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final VolunteerJobRepository volunteerJobRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns(LocalDate.now());
    }

    public Optional<Campaign> getCampaignById(Long id) {
        return campaignRepository.findById(id);
    }

    public Campaign createCampaign(Campaign campaign) {
        return campaignRepository.save(campaign);
    }

    public Campaign updateCampaign(Long id, Campaign campaignDetails) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        campaign.setCampaignOwner(campaignDetails.getCampaignOwner());
        campaign.setCampaignName(campaignDetails.getCampaignName());
        campaign.setNumSentInCampaign(campaignDetails.getNumSentInCampaign());
        campaign.setCampaignRecordType(campaignDetails.getCampaignRecordType());
        campaign.setActive(campaignDetails.isActive());
        campaign.setType(campaignDetails.getType());
        campaign.setStatus(campaignDetails.getStatus());
        campaign.setStartDate(campaignDetails.getStartDate());
        campaign.setEndDate(campaignDetails.getEndDate());
        campaign.setDescription(campaignDetails.getDescription());
        campaign.setVolunteerWebsiteTimeZone(campaignDetails.getVolunteerWebsiteTimeZone());

        return campaignRepository.save(campaign);
    }

    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }

    public void deactivateCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        campaign.setActive(false);
        campaignRepository.save(campaign);
    }

    public List<Campaign> searchCampaigns(String name) {
        return campaignRepository.findByCampaignNameContainingIgnoreCase(name);
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return campaignRepository.findByStatus(status);
    }

    public List<Campaign> getCampaignsByType(String type) {
        return campaignRepository.findByType(type);
    }

    public List<String> getAllCampaignTypes() {
        return campaignRepository.findAllTypes();
    }

    public List<String> getAllCampaignStatuses() {
        return campaignRepository.findAllStatuses();
    }

    public List<Campaign> getChildCampaigns(Long parentCampaignId) {
        return campaignRepository.findByParentCampaignId(parentCampaignId);
    }

    public Campaign cloneCampaign(Long originalCampaignId, String newCampaignName, LocalDate newStartDate) {
        Campaign original = campaignRepository.findById(originalCampaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + originalCampaignId));

        // Calculate date offset
        long dayOffset = 0;
        if (original.getStartDate() != null && newStartDate != null) {
            dayOffset = ChronoUnit.DAYS.between(original.getStartDate(), newStartDate);
        }

        // Create new campaign
        Campaign newCampaign = new Campaign();
        newCampaign.setCampaignName(newCampaignName);
        newCampaign.setCampaignOwner(original.getCampaignOwner());
        newCampaign.setCampaignRecordType(original.getCampaignRecordType());
        newCampaign.setType(original.getType());
        newCampaign.setStatus("Planned");
        newCampaign.setActive(true);
        newCampaign.setDescription(original.getDescription());
        newCampaign.setVolunteerWebsiteTimeZone(original.getVolunteerWebsiteTimeZone());
        
        // Adjust dates
        if (newStartDate != null) {
            newCampaign.setStartDate(newStartDate);
        } else if (original.getStartDate() != null) {
            newCampaign.setStartDate(original.getStartDate());
        }
        
        if (original.getEndDate() != null) {
            newCampaign.setEndDate(original.getEndDate().plusDays(dayOffset));
        }

        Campaign savedCampaign = campaignRepository.save(newCampaign);

        // Clone jobs and shifts
        List<VolunteerJob> originalJobs = volunteerJobRepository.findByCampaignId(originalCampaignId);
        final long finalDayOffset = dayOffset;
        
        for (VolunteerJob originalJob : originalJobs) {
            VolunteerJob newJob = new VolunteerJob();
            newJob.setCampaign(savedCampaign);
            newJob.setVolunteerJobName(originalJob.getVolunteerJobName());
            newJob.setDescription(originalJob.getDescription());
            newJob.setLocationStreet(originalJob.getLocationStreet());
            newJob.setLocationCity(originalJob.getLocationCity());
            newJob.setLocationState(originalJob.getLocationState());
            newJob.setLocationZip(originalJob.getLocationZip());
            newJob.setLocationInformation(originalJob.getLocationInformation());
            newJob.setSkillsNeeded(originalJob.getSkillsNeeded());
            newJob.setTeamLead(originalJob.getTeamLead());
            newJob.setPreference(originalJob.getPreference());
            newJob.setJobOptionSelection(originalJob.isJobOptionSelection());
            newJob.setDisplayOnWebsite(originalJob.isDisplayOnWebsite());
            newJob.setOngoing(originalJob.isOngoing());
            newJob.setInactive(false);
            newJob.setVolunteerWebsiteTimeZone(originalJob.getVolunteerWebsiteTimeZone());

            VolunteerJob savedJob = volunteerJobRepository.save(newJob);

            // Clone shifts for this job
            List<VolunteerShift> originalShifts = volunteerShiftRepository.findByVolunteerJobId(originalJob.getId());
            for (VolunteerShift originalShift : originalShifts) {
                VolunteerShift newShift = new VolunteerShift();
                newShift.setVolunteerJob(savedJob);
                newShift.setShiftId(null); // Will be auto-generated
                newShift.setStartDate(originalShift.getStartDate().plusDays(finalDayOffset));
                newShift.setStartTime(originalShift.getStartTime());
                newShift.setDurationHours(originalShift.getDurationHours());
                newShift.setDescription(originalShift.getDescription());
                newShift.setDesiredNumVolunteers(originalShift.getDesiredNumVolunteers());
                newShift.setCurrentNumVolunteers(0); // Reset volunteer count
                
                volunteerShiftRepository.save(newShift);
            }
        }

        return savedCampaign;
    }
}
