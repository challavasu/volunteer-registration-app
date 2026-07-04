package com.volunteer.registration.repository;

import com.volunteer.registration.model.VolunteerJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerJobRepository extends JpaRepository<VolunteerJob, Long> {

    List<VolunteerJob> findByInactiveFalse();

    List<VolunteerJob> findByDisplayOnWebsiteTrueAndInactiveFalse();

    List<VolunteerJob> findByCampaignId(Long campaignId);

    List<VolunteerJob> findByCampaignIdAndInactiveFalse(Long campaignId);

    List<VolunteerJob> findByCampaignIdAndDisplayOnWebsiteTrueAndInactiveFalse(Long campaignId);

    List<VolunteerJob> findByOngoingTrue();

    List<VolunteerJob> findByVolunteerJobNameContainingIgnoreCaseAndInactiveFalse(String name);

    @Query("SELECT j FROM VolunteerJob j WHERE j.inactive = false AND j.displayOnWebsite = true AND j.campaign.active = true")
    List<VolunteerJob> findAvailableJobs();

    @Query("SELECT j FROM VolunteerJob j WHERE j.campaign.id = :campaignId AND j.inactive = false AND j.displayOnWebsite = true")
    List<VolunteerJob> findAvailableJobsByCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT j FROM VolunteerJob j WHERE j.volunteerJobName = :jobName AND j.campaign.campaignName = :campaignName")
    Optional<VolunteerJob> findByVolunteerJobNameAndCampaignCampaignName(@Param("jobName") String jobName, @Param("campaignName") String campaignName);

    @Query("SELECT DISTINCT j.skillsNeeded FROM VolunteerJob j WHERE j.skillsNeeded IS NOT NULL")
    List<String> findAllSkills();

    List<VolunteerJob> findByTeamLead(String teamLead);
}
