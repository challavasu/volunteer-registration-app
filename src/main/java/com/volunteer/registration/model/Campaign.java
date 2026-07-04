package com.volunteer.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255, message = "Campaign owner must not exceed 255 characters")
    @Column(name = "campaign_owner")
    private String campaignOwner;

    @NotBlank(message = "Campaign name is required")
    @Size(min = 1, max = 255, message = "Campaign name must be between 1 and 255 characters")
    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "num_sent_in_campaign")
    private Integer numSentInCampaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_campaign_id")
    @JsonIgnore
    private Campaign parentCampaign;

    @Column(name = "campaign_record_type")
    private String campaignRecordType = "Volunteer Campaign";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 4000)
    private String description;

    @Column(name = "volunteer_website_time_zone")
    private String volunteerWebsiteTimeZone;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VolunteerJob> volunteerJobs = new ArrayList<>();

    @OneToMany(mappedBy = "parentCampaign")
    @JsonIgnore
    private List<Campaign> childCampaigns = new ArrayList<>();
}
