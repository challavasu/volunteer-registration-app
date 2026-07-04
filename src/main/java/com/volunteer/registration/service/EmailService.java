package com.volunteer.registration.service;

import com.volunteer.registration.model.EmailTemplate;
import com.volunteer.registration.model.EmailTemplate.EmailTemplateType;
import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepository;

    @Value("${app.email.from:noreply@volunteer.org}")
    private String fromEmail;

    @Value("${app.email.organization-name:VolunteerHub}")
    private String organizationName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    @Async
    public void sendRegistrationConfirmation(Registration registration) {
        sendEmailForType(EmailTemplateType.REGISTRATION_CONFIRMATION, registration);
    }

    @Async
    public void sendRegistrationConfirmations(List<Registration> registrations) {
        if (registrations == null || registrations.isEmpty()) return;
        
        Registration first = registrations.get(0);
        Volunteer volunteer = first.getVolunteer();
        
        Optional<EmailTemplate> templateOpt = templateRepository.findByTemplateTypeAndEnabledTrue(
                EmailTemplateType.REGISTRATION_CONFIRMATION);
        
        if (templateOpt.isEmpty()) {
            log.info("Registration confirmation email template is disabled or not found");
            return;
        }

        EmailTemplate template = templateOpt.get();
        
        StringBuilder shiftsHtml = new StringBuilder();
        for (Registration reg : registrations) {
            VolunteerJob job = reg.getVolunteerJob();
            VolunteerShift shift = reg.getVolunteerShift();
            shiftsHtml.append("<li><strong>").append(job.getVolunteerJobName()).append("</strong> - ");
            shiftsHtml.append(shift.getStartDate().format(DATE_FORMATTER));
            shiftsHtml.append(" at ").append(shift.getStartTime().format(TIME_FORMATTER));
            shiftsHtml.append(" (Check-in Code: <strong>").append(reg.getCheckInCode()).append("</strong>)</li>");
        }

        Map<String, String> variables = buildBaseVariables(volunteer);
        variables.put("{{shiftsList}}", shiftsHtml.toString());
        variables.put("{{campaignName}}", first.getVolunteerJob().getCampaign().getCampaignName());

        String subject = processTemplate(template.getSubject(), variables);
        String body = processTemplate(template.getBodyHtml(), variables);

        sendHtmlEmail(volunteer.getEmail(), subject, body);
    }

    @Async
    public void sendReminder(Registration registration) {
        sendEmailForType(EmailTemplateType.REMINDER_1_DAY, registration);
    }

    @Async
    public void sendThankYou(Registration registration) {
        sendEmailForType(EmailTemplateType.THANK_YOU, registration);
    }

    private void sendEmailForType(EmailTemplateType type, Registration registration) {
        if (!emailEnabled) {
            log.info("Email is disabled. Skipping {} email for registration {}", type, registration.getId());
            return;
        }

        Optional<EmailTemplate> templateOpt = templateRepository.findByTemplateTypeAndEnabledTrue(type);
        if (templateOpt.isEmpty()) {
            log.info("{} email template is disabled or not found", type);
            return;
        }

        EmailTemplate template = templateOpt.get();
        Map<String, String> variables = buildVariables(registration);

        String subject = processTemplate(template.getSubject(), variables);
        String body = processTemplate(template.getBodyHtml(), variables);

        sendHtmlEmail(registration.getVolunteer().getEmail(), subject, body);
    }

    public void sendTestEmail(String toEmail, EmailTemplate template) {
        Map<String, String> sampleVariables = buildSampleVariables();
        String subject = processTemplate(template.getSubject(), sampleVariables);
        String body = processTemplate(template.getBodyHtml(), sampleVariables);
        sendHtmlEmail(toEmail, "[TEST] " + subject, body);
    }

    public String previewTemplate(EmailTemplate template) {
        Map<String, String> sampleVariables = buildSampleVariables();
        return processTemplate(template.getBodyHtml(), sampleVariables);
    }

    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null) return "";
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    private Map<String, String> buildVariables(Registration registration) {
        Volunteer volunteer = registration.getVolunteer();
        VolunteerJob job = registration.getVolunteerJob();
        VolunteerShift shift = registration.getVolunteerShift();

        Map<String, String> variables = buildBaseVariables(volunteer);
        variables.put("{{campaignName}}", job.getCampaign() != null ? job.getCampaign().getCampaignName() : "");
        variables.put("{{jobName}}", job.getVolunteerJobName());
        variables.put("{{shiftDate}}", shift.getStartDate() != null ? shift.getStartDate().format(DATE_FORMATTER) : "");
        variables.put("{{shiftTime}}", shift.getStartTime() != null ? shift.getStartTime().format(TIME_FORMATTER) : "");
        variables.put("{{shiftLocation}}", buildLocationString(job));
        variables.put("{{checkInCode}}", registration.getCheckInCode() != null ? registration.getCheckInCode() : "");
        variables.put("{{hoursWorked}}", registration.getHoursWorked() != null ? 
                String.format("%.1f", registration.getHoursWorked()) : "");

        return variables;
    }

    private Map<String, String> buildBaseVariables(Volunteer volunteer) {
        Map<String, String> variables = new HashMap<>();
        variables.put("{{volunteerName}}", volunteer.getFirstName() + " " + volunteer.getLastName());
        variables.put("{{volunteerFirstName}}", volunteer.getFirstName());
        variables.put("{{volunteerLastName}}", volunteer.getLastName());
        variables.put("{{volunteerEmail}}", volunteer.getEmail());
        variables.put("{{organizationName}}", organizationName);
        return variables;
    }

    private Map<String, String> buildSampleVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("{{volunteerName}}", "John Doe");
        variables.put("{{volunteerFirstName}}", "John");
        variables.put("{{volunteerLastName}}", "Doe");
        variables.put("{{volunteerEmail}}", "john.doe@example.com");
        variables.put("{{campaignName}}", "Spring Volunteer Campaign 2026");
        variables.put("{{jobName}}", "Community Garden Helper");
        variables.put("{{shiftDate}}", "Saturday, March 15, 2026");
        variables.put("{{shiftTime}}", "9:00 AM");
        variables.put("{{shiftLocation}}", "123 Main Street, Springfield, CA 90210");
        variables.put("{{checkInCode}}", "ABC123");
        variables.put("{{hoursWorked}}", "4.0");
        variables.put("{{organizationName}}", organizationName);
        variables.put("{{shiftsList}}", "<li><strong>Community Garden Helper</strong> - Saturday, March 15, 2026 at 9:00 AM (Check-in Code: <strong>ABC123</strong>)</li>");
        return variables;
    }

    private String buildLocationString(VolunteerJob job) {
        StringBuilder location = new StringBuilder();
        if (job.getLocationStreet() != null && !job.getLocationStreet().isEmpty()) {
            location.append(job.getLocationStreet());
        }
        if (job.getLocationCity() != null && !job.getLocationCity().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(job.getLocationCity());
        }
        if (job.getLocationState() != null && !job.getLocationState().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(job.getLocationState());
        }
        if (job.getLocationZip() != null && !job.getLocationZip().isEmpty()) {
            if (location.length() > 0) location.append(" ");
            location.append(job.getLocationZip());
        }
        return location.toString();
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send to: {}, subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}, subject: {}, error: {}", to, subject, e.getMessage());
        }
    }
}
