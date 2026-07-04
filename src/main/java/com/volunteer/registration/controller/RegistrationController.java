package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.dto.ShiftRegistrationDTO;
import com.volunteer.registration.model.Registration;
import com.volunteer.registration.model.Registration.RegistrationStatus;
import com.volunteer.registration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Registration>>> getAllRegistrations() {
        List<Registration> registrations = registrationService.getAllRegistrations();
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Registration>> getRegistrationById(@PathVariable Long id) {
        return registrationService.getRegistrationById(id)
                .map(registration -> ResponseEntity.ok(ApiResponse.success(registration)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Registration not found")));
    }

    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByVolunteer(@PathVariable Long volunteerId) {
        List<Registration> registrations = registrationService.getRegistrationsByVolunteer(volunteerId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/volunteer/email/{email}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByVolunteerEmail(@PathVariable String email) {
        List<Registration> registrations = registrationService.getRegistrationsByVolunteerEmail(email);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByJob(@PathVariable Long jobId) {
        List<Registration> registrations = registrationService.getRegistrationsByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByShift(@PathVariable Long shiftId) {
        List<Registration> registrations = registrationService.getRegistrationsByShift(shiftId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByCampaign(@PathVariable Long campaignId) {
        List<Registration> registrations = registrationService.getRegistrationsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Registration>> registerForShift(
            @Valid @RequestBody ShiftRegistrationDTO dto) {
        try {
            Registration registration = registrationService.registerForShift(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Successfully registered for shift", registration));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Registration>> confirmRegistration(@PathVariable Long id) {
        try {
            Registration registration = registrationService.confirmRegistration(id);
            return ResponseEntity.ok(ApiResponse.success("Registration confirmed", registration));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Registration>> cancelRegistration(@PathVariable Long id) {
        try {
            Registration registration = registrationService.cancelRegistration(id);
            return ResponseEntity.ok(ApiResponse.success("Registration cancelled", registration));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Registration>> completeRegistration(
            @PathVariable Long id,
            @RequestParam(required = false) Double hoursWorked) {
        try {
            Registration registration = registrationService.completeRegistration(id, hoursWorked);
            return ResponseEntity.ok(ApiResponse.success("Registration completed", registration));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<ApiResponse<Registration>> markNoShow(@PathVariable Long id) {
        try {
            Registration registration = registrationService.markNoShow(id);
            return ResponseEntity.ok(ApiResponse.success("Marked as no show", registration));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkRegistration(
            @RequestParam Long volunteerId,
            @RequestParam Long shiftId) {
        boolean isRegistered = registrationService.isVolunteerRegisteredForShift(volunteerId, shiftId);
        return ResponseEntity.ok(ApiResponse.success(isRegistered));
    }

    @GetMapping("/shift/{shiftId}/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveRegistrationCount(@PathVariable Long shiftId) {
        long count = registrationService.getActiveRegistrationCountForShift(shiftId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Registration>>> getRegistrationsByStatus(
            @PathVariable RegistrationStatus status) {
        List<Registration> registrations = registrationService.getRegistrationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/volunteer/{volunteerId}/total-hours")
    public ResponseEntity<ApiResponse<Double>> getTotalHoursWorked(@PathVariable Long volunteerId) {
        Double hours = registrationService.getTotalHoursWorkedByVolunteer(volunteerId);
        return ResponseEntity.ok(ApiResponse.success(hours != null ? hours : 0.0));
    }

    @PostMapping("/send-confirmation/{email}")
    public ResponseEntity<ApiResponse<String>> sendConfirmationEmail(@PathVariable String email) {
        try {
            registrationService.sendConfirmationEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Confirmation email sent"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send confirmation email: " + e.getMessage()));
        }
    }
}
