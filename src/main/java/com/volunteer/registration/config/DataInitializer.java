package com.volunteer.registration.config;

import com.volunteer.registration.model.Campaign;
import com.volunteer.registration.model.EmailTemplate;
import com.volunteer.registration.model.EmailTemplate.EmailTemplateType;
import com.volunteer.registration.model.User;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.CampaignRepository;
import com.volunteer.registration.repository.EmailTemplateRepository;
import com.volunteer.registration.repository.UserRepository;
import com.volunteer.registration.repository.VolunteerJobRepository;
import com.volunteer.registration.repository.VolunteerShiftRepository;
import com.volunteer.registration.service.AuthService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;
    private final VolunteerJobRepository jobRepository;
    private final VolunteerShiftRepository shiftRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        // Initialize default admin user if not present
        initializeDefaultUser();

        // Fix any NULL checked_in values in existing registrations
        fixNullCheckedInValues();
        fixNullEmailFlags();

        // Initialize email templates if not present
        initializeEmailTemplates();

        // Only initialize data if database is empty
        if (campaignRepository.count() > 0) {
            System.out.println("Database already contains data. Skipping initialization.");
            return;
        }

        // Create Campaign 1: Spring Community Cleanup
        Campaign springCampaign = new Campaign();
        springCampaign.setCampaignOwner("VCC Volunteer Team");
        springCampaign.setCampaignName("Spring Community Cleanup 2026");
        springCampaign.setCampaignRecordType("Volunteer Campaign");
        springCampaign.setActive(true);
        springCampaign.setType("Community Event");
        springCampaign.setStatus("Planned");
        springCampaign.setStartDate(LocalDate.of(2026, 3, 1));
        springCampaign.setEndDate(LocalDate.of(2026, 5, 31));
        springCampaign.setDescription("Join us for our annual spring cleanup campaign! Help beautify our community by volunteering for various cleanup and beautification activities.");
        springCampaign.setVolunteerWebsiteTimeZone("America/Los_Angeles");
        springCampaign = campaignRepository.save(springCampaign);

        // Create Campaign 2: Summer Youth Program
        Campaign summerCampaign = new Campaign();
        summerCampaign.setCampaignOwner("VCC Volunteer Team");
        summerCampaign.setCampaignName("Summer Youth Program 2026");
        summerCampaign.setCampaignRecordType("Volunteer Campaign");
        summerCampaign.setActive(true);
        summerCampaign.setType("Education");
        summerCampaign.setStatus("Planned");
        summerCampaign.setStartDate(LocalDate.of(2026, 6, 1));
        summerCampaign.setEndDate(LocalDate.of(2026, 8, 31));
        summerCampaign.setDescription("Make a difference in young lives! Volunteer to mentor, teach, and engage with youth in our summer programs.");
        summerCampaign.setVolunteerWebsiteTimeZone("America/Los_Angeles");
        summerCampaign = campaignRepository.save(summerCampaign);

        // Create Campaign 3: Tech Conference
        Campaign techCampaign = new Campaign();
        techCampaign.setCampaignOwner("VCC Volunteer Team");
        techCampaign.setCampaignName("Tech Innovation Conference 2026");
        techCampaign.setCampaignRecordType("Volunteer Campaign");
        techCampaign.setActive(true);
        techCampaign.setType("Conference");
        techCampaign.setStatus("In Progress");
        techCampaign.setStartDate(LocalDate.of(2026, 2, 20));
        techCampaign.setEndDate(LocalDate.of(2026, 2, 22));
        techCampaign.setDescription("Help run our annual technology innovation conference with speakers from around the world.");
        techCampaign.setVolunteerWebsiteTimeZone("America/Los_Angeles");
        techCampaign = campaignRepository.save(techCampaign);

        // Create Jobs and Shifts for Spring Campaign
        createJobWithShifts(springCampaign, "Park Cleanup Crew",
                "Help clean up local parks by picking up litter, removing debris, and maintaining trails.",
                "Physical activity, outdoor work", "Central Park", "123 Park Ave", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 3, 15), LocalTime.of(9, 0), 4.0, 20),
                    new ShiftData(LocalDate.of(2026, 3, 15), LocalTime.of(14, 0), 4.0, 15),
                    new ShiftData(LocalDate.of(2026, 3, 22), LocalTime.of(9, 0), 4.0, 20)
                });

        createJobWithShifts(springCampaign, "Community Garden Volunteers",
                "Assist with planting, weeding, and maintaining our community garden.",
                "Gardening skills helpful", "Community Garden Center", "456 Garden Lane", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 3, 20), LocalTime.of(8, 0), 3.0, 12),
                    new ShiftData(LocalDate.of(2026, 3, 27), LocalTime.of(8, 0), 3.0, 12),
                    new ShiftData(LocalDate.of(2026, 4, 3), LocalTime.of(8, 0), 3.0, 12)
                });

        createJobWithShifts(springCampaign, "River Cleanup Team",
                "Join our river cleanup crew to remove trash and debris from local waterways.",
                "Physical fitness, water safety awareness", "Riverside Park", "789 River Road", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 4, 5), LocalTime.of(7, 0), 5.0, 25),
                    new ShiftData(LocalDate.of(2026, 4, 12), LocalTime.of(7, 0), 5.0, 25)
                });

        // Create Jobs and Shifts for Summer Campaign
        createJobWithShifts(summerCampaign, "Youth Tutoring",
                "Provide academic tutoring and homework help to students during summer break.",
                "Teaching experience, patience, subject expertise", "Public Library", "100 Library St", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 6, 15), LocalTime.of(10, 0), 2.0, 8),
                    new ShiftData(LocalDate.of(2026, 6, 15), LocalTime.of(14, 0), 2.0, 8),
                    new ShiftData(LocalDate.of(2026, 6, 17), LocalTime.of(10, 0), 2.0, 8),
                    new ShiftData(LocalDate.of(2026, 6, 17), LocalTime.of(14, 0), 2.0, 8)
                });

        createJobWithShifts(summerCampaign, "Sports Camp Coach",
                "Coach youth sports teams in basketball, soccer, or baseball.",
                "Sports experience, coaching skills, first aid certification preferred", "Sports Complex", "200 Athletic Blvd", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 6, 20), LocalTime.of(9, 0), 3.0, 6),
                    new ShiftData(LocalDate.of(2026, 6, 22), LocalTime.of(9, 0), 3.0, 6),
                    new ShiftData(LocalDate.of(2026, 6, 24), LocalTime.of(9, 0), 3.0, 6)
                });

        createJobWithShifts(summerCampaign, "Arts & Crafts Workshop Leader",
                "Lead creative arts and crafts sessions for children ages 6-12.",
                "Artistic skills, creativity, experience with children", "Community Center", "300 Community Way", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), 2.5, 4),
                    new ShiftData(LocalDate.of(2026, 7, 8), LocalTime.of(10, 0), 2.5, 4),
                    new ShiftData(LocalDate.of(2026, 7, 15), LocalTime.of(10, 0), 2.5, 4)
                });

        // Create Jobs and Shifts for Tech Conference
        createJobWithShifts(techCampaign, "Registration Desk",
                "Welcome attendees, check them in, and distribute badges and materials.",
                "Customer service, organizational skills", "Convention Center", "500 Convention Blvd", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 2, 20), LocalTime.of(7, 0), 4.0, 6),
                    new ShiftData(LocalDate.of(2026, 2, 20), LocalTime.of(11, 0), 4.0, 6),
                    new ShiftData(LocalDate.of(2026, 2, 21), LocalTime.of(7, 0), 4.0, 6),
                    new ShiftData(LocalDate.of(2026, 2, 21), LocalTime.of(11, 0), 4.0, 6)
                });

        createJobWithShifts(techCampaign, "Session Room Monitor",
                "Assist speakers, manage room logistics, and help attendees find seats.",
                "Communication skills, technical aptitude", "Convention Center", "500 Convention Blvd", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 2, 20), LocalTime.of(9, 0), 3.0, 10),
                    new ShiftData(LocalDate.of(2026, 2, 20), LocalTime.of(13, 0), 3.0, 10),
                    new ShiftData(LocalDate.of(2026, 2, 21), LocalTime.of(9, 0), 3.0, 10),
                    new ShiftData(LocalDate.of(2026, 2, 21), LocalTime.of(13, 0), 3.0, 10)
                });

        createJobWithShifts(techCampaign, "Tech Support",
                "Provide technical support for presentations, AV equipment, and WiFi issues.",
                "Technical skills, troubleshooting, AV experience", "Convention Center", "500 Convention Blvd", "Springfield", "CA", "90210",
                new ShiftData[]{
                    new ShiftData(LocalDate.of(2026, 2, 20), LocalTime.of(8, 0), 8.0, 4),
                    new ShiftData(LocalDate.of(2026, 2, 21), LocalTime.of(8, 0), 8.0, 4),
                    new ShiftData(LocalDate.of(2026, 2, 22), LocalTime.of(8, 0), 6.0, 4)
                });

        System.out.println("Sample data initialized successfully!");
        System.out.println("- 3 Campaigns created");
        System.out.println("- 9 Volunteer Jobs created");
        System.out.println("- Multiple shifts created for each job");
    }

    private void createJobWithShifts(Campaign campaign, String jobName, String description,
                                      String skills, String locationName, String street,
                                      String city, String state, String zip, ShiftData[] shifts) {
        VolunteerJob job = new VolunteerJob();
        job.setVolunteerJobName(jobName);
        job.setCampaign(campaign);
        job.setOngoing(false);
        job.setInactive(false);
        job.setDisplayOnWebsite(true);
        job.setVolunteerWebsiteTimeZone("America/Los_Angeles");
        job.setSkillsNeeded(skills);
        job.setDescription(description);
        job.setLocationStreet(street);
        job.setLocationCity(city);
        job.setLocationState(state);
        job.setLocationZip(zip);
        job.setLocationInformation("Located at " + locationName);

        job = jobRepository.save(job);

        // Create shifts for this job
        for (int i = 0; i < shifts.length; i++) {
            ShiftData sd = shifts[i];
            VolunteerShift shift = new VolunteerShift();
            shift.setShiftId("SHIFT-" + job.getId() + "-" + (i + 1));
            shift.setVolunteerJob(job);
            shift.setStartDate(sd.date);
            shift.setStartTime(sd.time);
            shift.setDurationHours(sd.duration);
            shift.setDesiredNumVolunteers(sd.maxVolunteers);
            shift.setCurrentNumVolunteers(0);
            shift.setDescription(jobName + " - " + sd.date.toString() + " at " + sd.time.toString());
            shiftRepository.save(shift);
        }
    }

    private void fixNullCheckedInValues() {
        try {
            int updated = entityManager.createNativeQuery(
                "UPDATE registrations SET checked_in = false WHERE checked_in IS NULL"
            ).executeUpdate();
            if (updated > 0) {
                System.out.println("Fixed " + updated + " registrations with NULL checked_in values.");
            }
        } catch (Exception e) {
            System.out.println("Note: Could not fix null checked_in values: " + e.getMessage());
        }
    }

    private void fixNullEmailFlags() {
        try {
            entityManager.createNativeQuery(
                "UPDATE registrations SET reminder_sent = false WHERE reminder_sent IS NULL"
            ).executeUpdate();
            entityManager.createNativeQuery(
                "UPDATE registrations SET confirmation_email_sent = false WHERE confirmation_email_sent IS NULL"
            ).executeUpdate();
            entityManager.createNativeQuery(
                "UPDATE registrations SET thank_you_email_sent = false WHERE thank_you_email_sent IS NULL"
            ).executeUpdate();
        } catch (Exception e) {
            System.out.println("Note: Could not fix null email flags: " + e.getMessage());
        }
    }

    private void initializeEmailTemplates() {
        if (emailTemplateRepository.count() > 0) {
            return;
        }

        System.out.println("Initializing default email templates...");

        // Registration Confirmation Template
        EmailTemplate registrationTemplate = new EmailTemplate();
        registrationTemplate.setTemplateType(EmailTemplateType.REGISTRATION_CONFIRMATION);
        registrationTemplate.setSubject("Welcome {{volunteerFirstName}}! Your Registration is Confirmed");
        registrationTemplate.setBodyHtml("""
            <h2>Welcome, {{volunteerFirstName}}!</h2>
            <p>Thank you for registering to volunteer with <strong>{{organizationName}}</strong>!</p>
            <p>Here are your registration details:</p>
            <ul>{{shiftsList}}</ul>
            <p><strong>What to bring:</strong></p>
            <ul>
                <li>Your check-in code (shown above)</li>
                <li>A valid ID</li>
                <li>Comfortable clothing appropriate for the activity</li>
            </ul>
            <p>If you have any questions, please don't hesitate to contact us.</p>
            <p>We look forward to seeing you!</p>
            <p>Best regards,<br>The {{organizationName}} Team</p>
            """);
        registrationTemplate.setEnabled(true);
        emailTemplateRepository.save(registrationTemplate);

        // 1-Day Reminder Template
        EmailTemplate reminderTemplate = new EmailTemplate();
        reminderTemplate.setTemplateType(EmailTemplateType.REMINDER_1_DAY);
        reminderTemplate.setSubject("Reminder: Your Volunteer Shift is Tomorrow!");
        reminderTemplate.setBodyHtml("""
            <h2>Hi {{volunteerFirstName}}!</h2>
            <p>This is a friendly reminder that you're scheduled to volunteer <strong>tomorrow</strong>!</p>
            <p><strong>Details:</strong></p>
            <ul>
                <li><strong>Campaign:</strong> {{campaignName}}</li>
                <li><strong>Activity:</strong> {{jobName}}</li>
                <li><strong>Date:</strong> {{shiftDate}}</li>
                <li><strong>Time:</strong> {{shiftTime}}</li>
                <li><strong>Location:</strong> {{shiftLocation}}</li>
            </ul>
            <p><strong>Your Check-in Code:</strong> <span style="font-size: 1.2em; font-weight: bold; color: #e91e63;">{{checkInCode}}</span></p>
            <p>Please arrive 10-15 minutes early to check in.</p>
            <p>See you tomorrow!</p>
            <p>Best regards,<br>The {{organizationName}} Team</p>
            """);
        reminderTemplate.setEnabled(true);
        emailTemplateRepository.save(reminderTemplate);

        // Thank You Template
        EmailTemplate thankYouTemplate = new EmailTemplate();
        thankYouTemplate.setTemplateType(EmailTemplateType.THANK_YOU);
        thankYouTemplate.setSubject("Thank You for Volunteering, {{volunteerFirstName}}!");
        thankYouTemplate.setBodyHtml("""
            <h2>Thank You, {{volunteerFirstName}}!</h2>
            <p>We wanted to take a moment to express our sincere gratitude for your time and effort volunteering with <strong>{{organizationName}}</strong>.</p>
            <p>Your contribution to <strong>{{jobName}}</strong> on <strong>{{shiftDate}}</strong> made a real difference in our community.</p>
            <p>We hope you had a great experience and we'd love to see you again at future events!</p>
            <p>Thank you for being part of our volunteer family.</p>
            <p>With gratitude,<br>The {{organizationName}} Team</p>
            """);
        thankYouTemplate.setEnabled(true);
        emailTemplateRepository.save(thankYouTemplate);

        System.out.println("- 3 Email templates created");
    }

    private void initializeDefaultUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            authService.createUser("admin", "admin123", User.UserRole.ADMIN);
            System.out.println("Default admin user created - Username: admin, Password: admin123");
        }
    }

    // Helper class for shift data
    private static class ShiftData {
        LocalDate date;
        LocalTime time;
        Double duration;
        Integer maxVolunteers;

        ShiftData(LocalDate date, LocalTime time, Double duration, Integer maxVolunteers) {
            this.date = date;
            this.time = time;
            this.duration = duration;
            this.maxVolunteers = maxVolunteers;
        }
    }
}
