package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.model.EmailTemplate;
import com.volunteer.registration.model.EmailTemplate.EmailTemplateType;
import com.volunteer.registration.repository.EmailTemplateRepository;
import com.volunteer.registration.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailTemplateController {

    private final EmailTemplateRepository templateRepository;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getAllTemplates() {
        List<EmailTemplate> templates = templateRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmailTemplate>> getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> ResponseEntity.ok(ApiResponse.success(template)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<EmailTemplate>> getTemplateByType(@PathVariable EmailTemplateType type) {
        return templateRepository.findByTemplateType(type)
                .map(template -> ResponseEntity.ok(ApiResponse.success(template)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found for type: " + type)));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getTemplateTypes() {
        List<Map<String, String>> types = Arrays.stream(EmailTemplateType.values())
                .map(type -> {
                    Map<String, String> typeInfo = new HashMap<>();
                    typeInfo.put("name", type.name());
                    typeInfo.put("displayName", type.getDisplayName());
                    typeInfo.put("description", type.getDescription());
                    return typeInfo;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/variables")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAvailableVariables() {
        List<Map<String, String>> variables = List.of(
                createVariable("{{volunteerName}}", "Full name of volunteer"),
                createVariable("{{volunteerFirstName}}", "First name"),
                createVariable("{{volunteerLastName}}", "Last name"),
                createVariable("{{volunteerEmail}}", "Email address"),
                createVariable("{{campaignName}}", "Campaign name"),
                createVariable("{{jobName}}", "Volunteer job name"),
                createVariable("{{shiftDate}}", "Shift date (formatted)"),
                createVariable("{{shiftTime}}", "Shift start time"),
                createVariable("{{shiftLocation}}", "Location address"),
                createVariable("{{checkInCode}}", "Check-in code"),
                createVariable("{{hoursWorked}}", "Hours worked (for thank you emails)"),
                createVariable("{{organizationName}}", "Organization name"),
                createVariable("{{shiftsList}}", "List of all shifts (for registration confirmation)")
        );
        return ResponseEntity.ok(ApiResponse.success(variables));
    }

    private Map<String, String> createVariable(String variable, String description) {
        Map<String, String> map = new HashMap<>();
        map.put("variable", variable);
        map.put("description", description);
        return map;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmailTemplate>> createTemplate(@RequestBody EmailTemplate template) {
        if (templateRepository.findByTemplateType(template.getTemplateType()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Template for this type already exists"));
        }
        EmailTemplate saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmailTemplate>> updateTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplate template) {
        return templateRepository.findById(id)
                .map(existing -> {
                    existing.setSubject(template.getSubject());
                    existing.setBodyHtml(template.getBodyHtml());
                    existing.setEnabled(template.isEnabled());
                    EmailTemplate saved = templateRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.success("Template updated successfully", saved));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<EmailTemplate>> toggleTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> {
                    template.setEnabled(!template.isEnabled());
                    EmailTemplate saved = templateRepository.save(template);
                    String status = saved.isEnabled() ? "enabled" : "disabled";
                    return ResponseEntity.ok(ApiResponse.success("Template " + status, saved));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<String>> sendTestEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String toEmail = request.get("email");
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email address is required"));
        }

        return templateRepository.findById(id)
                .map(template -> {
                    try {
                        emailService.sendTestEmail(toEmail, template);
                        return ResponseEntity.ok(ApiResponse.success("Test email sent to " + toEmail));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.<String>error("Failed to send test email: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<ApiResponse<String>> previewTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> {
                    String preview = emailService.previewTemplate(template);
                    return ResponseEntity.ok(ApiResponse.success(preview));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Template not found"));
        }
        templateRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }
}
