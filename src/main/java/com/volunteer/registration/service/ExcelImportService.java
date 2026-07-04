package com.volunteer.registration.service;

import com.volunteer.registration.model.Campaign;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Registration.RegistrationStatus;
import com.volunteer.registration.repository.CampaignRepository;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import com.volunteer.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final CampaignRepository campaignRepository;
    private final VolunteerJobRepository jobRepository;
    private final VolunteerShiftRepository shiftRepository;
    private final VolunteerRepository volunteerRepository;
    private final RegistrationRepository registrationRepository;
    private final EntityManager entityManager;

    public Map<String, Object> importFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Extract campaign name from the title row
            String campaignName = extractCampaignName(sheet);
            if (campaignName == null || campaignName.isEmpty()) {
                result.put("success", false);
                result.put("message", "Could not find campaign name in Excel file");
                return result;
            }

            // Create or get campaign
            Campaign campaign = campaignRepository.findByCampaignName(campaignName)
                .orElseGet(() -> createDefaultCampaign(campaignName));

            int jobsCount = 0;
            int shiftsCount = 0;
            int volunteersCount = 0;
            int registrationsCount = 0;

            // Track unique jobs and shifts
            Map<String, VolunteerJob> jobMap = new HashMap<>();
            Map<String, VolunteerShift> shiftMap = new HashMap<>();

            // Track shift dates for campaign start/end date calculation
            LocalDate earliestShiftDate = null;
            LocalDate latestShiftDate = null;

            // Find header row (row 10 in the sample)
            int headerRow = findHeaderRow(sheet);
            if (headerRow == -1) {
                result.put("success", false);
                result.put("message", "Could not find header row in Excel file");
                return result;
            }

            // Process data rows
            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Extract data from row
                    String jobName = getCellValue(row, 1); // Col B
                    String startDateTime = getCellValue(row, 3); // Col D
                    String volunteerName = getCellValue(row, 4); // Col E
                    String phone = getCellValue(row, 5); // Col F
                    String email = getCellValue(row, 6); // Col G
                    String desiredVolunteerStr = getCellValue(row, 7); // Col H
                    String durationStr = getCellValue(row, 10); // Col K

                    // Location columns (new)
                    String locStreet = getCellValue(row, 11); // Col L
                    String locCity = getCellValue(row, 12); // Col M
                    String locState = getCellValue(row, 13); // Col N
                    String locZip = getCellValue(row, 14); // Col O

                    if (jobName == null || jobName.isEmpty() || email == null || email.isEmpty()) {
                        continue;
                    }

                    // Parse job name
                    // Format: "Job Name @ Location" or "Job Name - Details @ Location"
                    int atIndex = jobName.lastIndexOf("@");
                    String jobTitle;

                    if (atIndex > 0) {
                        jobTitle = jobName.substring(0, atIndex).trim();
                    } else {
                        jobTitle = jobName.trim();
                    }

                    // Use location from Excel columns (or defaults if empty)
                    final String locationStreet = (locStreet != null && !locStreet.isEmpty()) ? locStreet : "";
                    final String locationCity = (locCity != null && !locCity.isEmpty()) ? locCity : "";
                    final String locationState = (locState != null && !locState.isEmpty()) ? locState : "WA";
                    final String locationZip = (locZip != null && !locZip.isEmpty()) ? locZip : "";

                    // Create unique key for job
                    String jobKey = jobName;
                    VolunteerJob job = jobMap.computeIfAbsent(jobKey, k -> {
                        VolunteerJob newJob = new VolunteerJob();
                        newJob.setVolunteerJobName(jobTitle);
                        newJob.setCampaign(campaign);
                        newJob.setDescription(jobName);
                        newJob.setLocationInformation(locationCity + ", " + locationState);
                        newJob.setLocationStreet(locationStreet);
                        newJob.setLocationCity(locationCity);
                        newJob.setLocationState(locationState);
                        newJob.setLocationZip(locationZip);
                        newJob.setInactive(false);
                        newJob.setDisplayOnWebsite(true);
                        newJob.setOngoing(false);
                        newJob.setVolunteerWebsiteTimeZone("America/Los_Angeles");
                        return jobRepository.save(newJob);
                    });

                    // Parse date and time
                    LocalDateTime shiftDateTime = parseDateTime(startDateTime);
                    if (shiftDateTime == null) continue;

                    double duration = durationStr != null && !durationStr.isEmpty()
                        ? Double.parseDouble(durationStr) : 2.0;
                    int desiredVolunteers = desiredVolunteerStr != null && !desiredVolunteerStr.isEmpty()
                        ? Integer.parseInt(desiredVolunteerStr) : 1;

                    // Create unique key for shift
                    String shiftKey = jobName + "_" + shiftDateTime;
                    VolunteerShift shift = shiftMap.getOrDefault(shiftKey, null);

                    if (shift == null) {
                        String shiftId = generateShiftId(jobName, shiftDateTime);
                        VolunteerShift newShift = new VolunteerShift();
                        newShift.setShiftId(shiftId);
                        newShift.setVolunteerJob(job);
                        newShift.setStartDate(shiftDateTime.toLocalDate());
                        newShift.setStartTime(shiftDateTime.toLocalTime());
                        newShift.setDurationHours(duration);
                        newShift.setDesiredNumVolunteers(desiredVolunteers);
                        newShift.setCurrentNumVolunteers(0);
                        newShift.setDescription(jobName + " - " + shiftDateTime);

                        try {
                            shift = shiftRepository.save(newShift);
                            shiftMap.put(shiftKey, shift);

                            // Track shift dates for campaign date calculation
                            LocalDate shiftDate = shiftDateTime.toLocalDate();
                            if (earliestShiftDate == null || shiftDate.isBefore(earliestShiftDate)) {
                                earliestShiftDate = shiftDate;
                            }
                            LocalDate endDate = shiftDate.plusDays((long) Math.ceil(duration / 24.0));
                            if (latestShiftDate == null || endDate.isAfter(latestShiftDate)) {
                                latestShiftDate = endDate;
                            }
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            // Shift with this ID already exists, just skip
                            System.err.println("Shift " + shiftId + " already exists, skipping");
                            continue;
                        } catch (Exception e) {
                            System.err.println("Error saving shift: " + e.getMessage());
                            e.printStackTrace();
                            throw e;
                        }
                    }

                    // Create or update volunteer
                    boolean volunteerExists = volunteerRepository.findByEmail(email).isPresent();
                    Volunteer volunteer;
                    if (!volunteerExists) {
                        volunteer = new Volunteer();
                        volunteer.setEmail(email);
                        volunteer.setFirstName(extractFirstName(volunteerName));
                        volunteer.setLastName(extractLastName(volunteerName));
                        volunteer.setPhoneNumber(phone != null ? phone : "");
                        volunteer.setActive(true);
                        volunteer = volunteerRepository.save(volunteer);
                        volunteersCount++;
                    } else {
                        volunteer = volunteerRepository.findByEmail(email).get();
                    }

                    // Create registration if not exists
                    if (!registrationRepository.existsByVolunteerIdAndVolunteerShiftId(volunteer.getId(), shift.getId())) {
                        try {
                            Registration registration = new Registration();
                            registration.setVolunteer(volunteer);
                            registration.setVolunteerJob(job);  // IMPORTANT: Set the job!
                            registration.setVolunteerShift(shift);
                            registration.setStatus(RegistrationStatus.CONFIRMED);
                            registration.setRegistrationDate(LocalDateTime.now());
                            registration.setCheckedIn(false);
                            registration.setConfirmationEmailSent(false);
                            registration.setReminderSent(false);
                            registration.setThankYouEmailSent(false);
                            registrationRepository.save(registration);
                            registrationsCount++;
                        } catch (Exception e) {
                            System.err.println("Error creating registration for " + email + " to shift " + shift.getShiftId() + ": " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error processing row " + i + ": " + e.getMessage());
                }
            }

            // Update campaign dates based on imported shifts
            if (earliestShiftDate != null && latestShiftDate != null) {
                campaign.setStartDate(earliestShiftDate);
                campaign.setEndDate(latestShiftDate);
                campaignRepository.save(campaign);
            }

            jobsCount = (int) jobRepository.count();
            shiftsCount = (int) shiftRepository.count();

            result.put("success", true);
            result.put("message", "Import completed successfully!");
            result.put("campaignName", campaignName);
            result.put("jobsImported", jobMap.size());
            result.put("shiftsImported", shiftMap.size());
            result.put("volunteersImported", volunteersCount);
            result.put("registrationsImported", registrationsCount);

            workbook.close();
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error during import: " + e.getMessage());
        }

        return result;
    }

    private String extractCampaignName(Sheet sheet) {
        // Look for campaign name in first few rows
        for (int i = 0; i < 20; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String cellValue = getCellValue(row, 0);
                if (cellValue != null && cellValue.contains("Campaign")) {
                    // Extract campaign name - handle various formats
                    String cleanedName = cellValue;

                    // Remove "Campaign:" prefix if present
                    if (cleanedName.startsWith("Campaign:")) {
                        cleanedName = cleanedName.substring("Campaign:".length()).trim();
                    }

                    // Remove "Campaign Name equals" if present
                    if (cleanedName.contains("Campaign Name equals")) {
                        String[] parts = cleanedName.split("Campaign Name equals");
                        if (parts.length > 1) {
                            cleanedName = parts[1].trim();
                        }
                    }

                    // Return the cleaned campaign name if it's not empty and not just "Campaign"
                    if (!cleanedName.isEmpty() && !cleanedName.equalsIgnoreCase("Campaign")) {
                        return cleanedName;
                    }
                }
                // Also check second column
                cellValue = getCellValue(row, 1);
                if (cellValue != null && (cellValue.contains("VegFest") ||
                    cellValue.contains("Festival") ||
                    cellValue.contains("Mela") ||
                    cellValue.contains("Cleanup") ||
                    cellValue.contains("Program"))) {
                    return cellValue.trim();
                }
            }
        }
        return null;
    }

    private int findHeaderRow(Sheet sheet) {
        // Look for row containing "Volunteer Job Name"
        for (int i = 0; i < 20; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String cellValue = getCellValue(row, 1);
                if (cellValue != null && cellValue.contains("Volunteer Job Name")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getCellValue(Row row, int columnIndex) {
        if (row == null) return null;
        try {
            var cell = row.getCell(columnIndex);
            if (cell == null) return null;

            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    }
                    return String.valueOf(numValue);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;

        try {
            // Try parsing "6/20/2026 8:30 AM" format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e1) {
            try {
                // Try other formats
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String generateShiftId(String jobName, LocalDateTime dateTime) {
        // Use the full job name (including location) for uniqueness
        String jobClean = jobName.replaceAll("[^a-zA-Z0-9]", "");
        if (jobClean.length() > 20) {
            jobClean = jobClean.substring(0, 20);
        }
        String dateTime_str = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "SHIFT-" + jobClean + "-" + dateTime_str;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "Unknown";
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "Volunteer";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "Volunteer";
    }


    private Campaign createDefaultCampaign(String campaignName) {
        Campaign campaign = new Campaign();
        campaign.setCampaignName(campaignName);
        campaign.setCampaignOwner("Imported Campaign");
        campaign.setDescription("Campaign imported from Excel file");
        campaign.setType("Community Event");
        campaign.setStatus("In Progress");
        campaign.setCampaignRecordType("Volunteer Campaign");
        campaign.setStartDate(LocalDate.now());
        campaign.setEndDate(LocalDate.now().plusMonths(3));
        campaign.setActive(true);
        campaign.setVolunteerWebsiteTimeZone("America/Los_Angeles");
        return campaignRepository.save(campaign);
    }
}
