package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.dto.VolunteerRegistrationDTO;
import com.volunteer.registration.model.Volunteer;
import com.volunteer.registration.service.VolunteerManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerManagementService volunteerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        // Add registration count to each volunteer
        List<Map<String, Object>> result = volunteers.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("firstName", v.getFirstName());
            map.put("lastName", v.getLastName());
            map.put("email", v.getEmail());
            map.put("phoneNumber", v.getPhoneNumber());
            map.put("address", v.getAddress());
            map.put("city", v.getCity());
            map.put("state", v.getState());
            map.put("zipCode", v.getZipCode());
            map.put("skills", v.getSkills());
            map.put("interests", v.getInterests());
            map.put("active", v.isActive());
            map.put("registrationDate", v.getRegistrationDate());
            map.put("registrationCount", volunteerService.getRegistrationCount(v.getId()));
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Volunteer>>> getActiveVolunteers() {
        List<Volunteer> volunteers = volunteerService.getActiveVolunteers();
        return ResponseEntity.ok(ApiResponse.success(volunteers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Volunteer>> getVolunteerById(@PathVariable Long id) {
        return volunteerService.getVolunteerById(id)
                .map(volunteer -> ResponseEntity.ok(ApiResponse.success(volunteer)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer not found")));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Volunteer>> getVolunteerByEmail(@PathVariable String email) {
        return volunteerService.getVolunteerByEmail(email)
                .map(volunteer -> ResponseEntity.ok(ApiResponse.success(volunteer)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer not found")));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<Volunteer>> getVolunteerByPhone(@PathVariable String phone) {
        return volunteerService.getVolunteerByPhone(phone)
                .map(volunteer -> ResponseEntity.ok(ApiResponse.success(volunteer)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer not found")));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = volunteerService.emailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneExists(@PathVariable String phone) {
        boolean exists = volunteerService.phoneExists(phone);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Volunteer>> registerVolunteer(
            @Valid @RequestBody VolunteerRegistrationDTO dto) {
        try {
            Volunteer volunteer = volunteerService.registerVolunteer(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Volunteer registered successfully", volunteer));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Volunteer>> updateVolunteer(
            @PathVariable Long id,
            @RequestBody Volunteer volunteerDetails) {
        try {
            Volunteer updated = volunteerService.updateVolunteerEntity(id, volunteerDetails);
            return ResponseEntity.ok(ApiResponse.success("Volunteer updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVolunteer(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
        return ResponseEntity.ok(ApiResponse.success("Volunteer deleted successfully", null));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateVolunteer(@PathVariable Long id) {
        try {
            volunteerService.deactivateVolunteer(id);
            return ResponseEntity.ok(ApiResponse.success("Volunteer deactivated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Volunteer>>> searchVolunteers(@RequestParam String query) {
        List<Volunteer> volunteers = volunteerService.searchVolunteers(query);
        return ResponseEntity.ok(ApiResponse.success(volunteers));
    }

    @GetMapping("/skill/{skill}")
    public ResponseEntity<ApiResponse<List<Volunteer>>> findVolunteersBySkill(@PathVariable String skill) {
        List<Volunteer> volunteers = volunteerService.findVolunteersBySkill(skill);
        return ResponseEntity.ok(ApiResponse.success(volunteers));
    }

    @GetMapping("/{id}/registration-count")
    public ResponseEntity<ApiResponse<Long>> getRegistrationCount(@PathVariable Long id) {
        Long count = volunteerService.getRegistrationCount(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
