package com.volunteer.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "volunteer_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Volunteer Job Name is required")
    @Size(min = 1, max = 255, message = "Job name must be between 1 and 255 characters")
    @Column(name = "volunteer_job_name", nullable = false)
    private String volunteerJobName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnoreProperties({"volunteerJobs", "childCampaigns", "parentCampaign", "description", "hibernateLazyInitializer", "handler"})
    private Campaign campaign;

    @Column(name = "ongoing")
    private boolean ongoing = false;

    @Column(name = "inactive")
    private boolean inactive = false;

    @Column(name = "display_on_website")
    private boolean displayOnWebsite = true;

    @Column(name = "volunteer_website_time_zone")
    private String volunteerWebsiteTimeZone;

    @Column(name = "skills_needed", length = 1000)
    private String skillsNeeded;

    @Column(name = "job_option_selection")
    private boolean jobOptionSelection = false;

    @Column(name = "preference")
    private String preference;

    @Column(name = "team_lead")
    private String teamLead;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "location_street")
    private String locationStreet;

    @Column(name = "location_city")
    private String locationCity;

    @Column(name = "location_state")
    private String locationState;

    @Column(name = "location_zip")
    private String locationZip;

    @Column(name = "location_information", length = 2000)
    private String locationInformation;

    @OneToMany(mappedBy = "volunteerJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VolunteerShift> shifts = new ArrayList<>();

    @OneToMany(mappedBy = "volunteerJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Registration> registrations = new ArrayList<>();

    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (locationStreet != null && !locationStreet.isEmpty()) sb.append(locationStreet);
        if (locationCity != null && !locationCity.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(locationCity);
        }
        if (locationState != null && !locationState.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(locationState);
        }
        if (locationZip != null && !locationZip.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(locationZip);
        }
        return sb.toString();
    }

    public boolean isAvailable() {
        return !inactive && displayOnWebsite;
    }
}
