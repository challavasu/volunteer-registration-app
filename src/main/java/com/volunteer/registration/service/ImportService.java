package com.volunteer.registration.service;

import com.volunteer.registration.model.Campaign;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.CampaignRepository;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final CampaignRepository campaignRepository;
    private final VolunteerJobRepository jobRepository;
    private final VolunteerShiftRepository shiftRepository;
    private final VolunteerRepository volunteerRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional
    public Map<String, Object> importFromCSV(MultipartFile campaignsFile, MultipartFile jobsFile,
                                             MultipartFile shiftsFile, MultipartFile volunteersFile) {
        Map<String, Object> result = new HashMap<>();
        int campaignsCount = 0;
        int jobsCount = 0;
        int shiftsCount = 0;
        int volunteersCount = 0;

        try {
            // Import campaigns
            if (campaignsFile != null && !campaignsFile.isEmpty()) {
                campaignsCount = importCampaigns(campaignsFile);
            }

            // Import jobs
            if (jobsFile != null && !jobsFile.isEmpty()) {
                jobsCount = importJobs(jobsFile);
            }

            // Import shifts
            if (shiftsFile != null && !shiftsFile.isEmpty()) {
                shiftsCount = importShifts(shiftsFile);
            }

            // Import volunteers
            if (volunteersFile != null && !volunteersFile.isEmpty()) {
                volunteersCount = importVolunteers(volunteersFile);
            }

            result.put("success", true);
            result.put("message", "Import completed successfully!");
            result.put("campaignsImported", campaignsCount);
            result.put("jobsImported", jobsCount);
            result.put("shiftsImported", shiftsCount);
            result.put("volunteersImported", volunteersCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error during import: " + e.getMessage());
        }

        return result;
    }

    private int importCampaigns(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int count = 0;
            for (CSVRecord record : csvParser) {
                Campaign campaign = new Campaign();
                campaign.setCampaignName(record.get("campaign_name"));
                campaign.setCampaignOwner(record.get("campaign_owner"));
                campaign.setDescription(record.get("description"));
                campaign.setType(record.get("type"));
                campaign.setStatus(record.get("status"));
                campaign.setCampaignRecordType("Volunteer Campaign");
                campaign.setStartDate(LocalDate.parse(record.get("start_date"), DATE_FORMATTER));
                campaign.setEndDate(LocalDate.parse(record.get("end_date"), DATE_FORMATTER));
                campaign.setActive(Boolean.parseBoolean(record.get("active")));
                campaign.setVolunteerWebsiteTimeZone(getOrDefault(record, "time_zone", "America/Los_Angeles"));

                campaignRepository.save(campaign);
                count++;
            }
            return count;
        }
    }

    private int importJobs(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int count = 0;
            for (CSVRecord record : csvParser) {
                String campaignName = record.get("campaign_name");
                Campaign campaign = campaignRepository.findByCampaignName(campaignName)
                    .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignName));

                VolunteerJob job = new VolunteerJob();
                job.setVolunteerJobName(record.get("job_name"));
                job.setDescription(record.get("description"));
                job.setSkillsNeeded(record.get("skills_needed"));
                job.setLocationStreet(record.get("location_street"));
                job.setLocationCity(record.get("location_city"));
                job.setLocationState(record.get("location_state"));
                job.setLocationZip(record.get("location_zip"));
                job.setLocationInformation(getOrDefault(record, "location_info", ""));
                job.setCampaign(campaign);
                job.setInactive(false);
                job.setDisplayOnWebsite(true);
                job.setOngoing(Boolean.parseBoolean(getOrDefault(record, "ongoing", "false")));
                job.setVolunteerWebsiteTimeZone(getOrDefault(record, "time_zone", "America/Los_Angeles"));

                jobRepository.save(job);
                count++;
            }
            return count;
        }
    }

    private int importShifts(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int count = 0;
            for (CSVRecord record : csvParser) {
                String jobName = record.get("job_name");
                String campaignName = record.get("campaign_name");

                VolunteerJob job = jobRepository.findByVolunteerJobNameAndCampaignCampaignName(jobName, campaignName)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobName + " in campaign: " + campaignName));

                VolunteerShift shift = new VolunteerShift();
                shift.setShiftId(record.get("shift_id"));
                shift.setVolunteerJob(job);
                shift.setStartDate(LocalDate.parse(record.get("start_date"), DATE_FORMATTER));
                shift.setStartTime(LocalTime.parse(record.get("start_time"), TIME_FORMATTER));
                shift.setDurationHours(Double.parseDouble(record.get("duration_hours")));
                shift.setDesiredNumVolunteers(Integer.parseInt(record.get("desired_num_volunteers")));
                shift.setCurrentNumVolunteers(Integer.parseInt(getOrDefault(record, "current_num_volunteers", "0")));
                shift.setDescription(getOrDefault(record, "description", ""));

                shiftRepository.save(shift);
                count++;
            }
            return count;
        }
    }

    private int importVolunteers(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int count = 0;
            for (CSVRecord record : csvParser) {
                String email = record.get("email");

                // Check if volunteer already exists
                if (volunteerRepository.findByEmail(email).isPresent()) {
                    continue;
                }

                Volunteer volunteer = new Volunteer();
                volunteer.setFirstName(record.get("first_name"));
                volunteer.setLastName(record.get("last_name"));
                volunteer.setEmail(email);
                volunteer.setPhoneNumber(getOrDefault(record, "phone", ""));
                volunteer.setAddress(getOrDefault(record, "address", ""));
                volunteer.setCity(getOrDefault(record, "city", ""));
                volunteer.setState(getOrDefault(record, "state", ""));
                volunteer.setZipCode(getOrDefault(record, "zip_code", ""));
                volunteer.setSkills(getOrDefault(record, "skills", ""));
                volunteer.setInterests(getOrDefault(record, "interests", ""));
                volunteer.setEmergencyContactName(getOrDefault(record, "emergency_contact_name", ""));
                volunteer.setEmergencyContactPhone(getOrDefault(record, "emergency_contact_phone", ""));
                volunteer.setActive(true);

                volunteerRepository.save(volunteer);
                count++;
            }
            return count;
        }
    }

    private String getOrDefault(CSVRecord record, String header, String defaultValue) {
        try {
            String value = record.get(header);
            return value == null || value.isEmpty() ? defaultValue : value;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
