package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.model.VolunteerShift;
import com.volunteer.registration.service.VolunteerShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class VolunteerShiftController {

    private final VolunteerShiftService shiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getAllShifts() {
        List<VolunteerShift> shifts = shiftService.getAllShifts();
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VolunteerShift>> getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id)
                .map(shift -> ResponseEntity.ok(ApiResponse.success(shift)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer Shift not found")));
    }

    @GetMapping("/shift-id/{shiftId}")
    public ResponseEntity<ApiResponse<VolunteerShift>> getShiftByShiftId(@PathVariable String shiftId) {
        return shiftService.getShiftByShiftId(shiftId)
                .map(shift -> ResponseEntity.ok(ApiResponse.success(shift)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer Shift not found")));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getShiftsByJob(@PathVariable Long jobId) {
        List<VolunteerShift> shifts = shiftService.getShiftsByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/job/{jobId}/upcoming")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getUpcomingShiftsByJob(@PathVariable Long jobId) {
        List<VolunteerShift> shifts = shiftService.getUpcomingShiftsByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getAvailableShifts() {
        List<VolunteerShift> shifts = shiftService.getAvailableShifts();
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/job/{jobId}/available")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getAvailableShiftsByJob(@PathVariable Long jobId) {
        List<VolunteerShift> shifts = shiftService.getAvailableShiftsByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getShiftsByCampaign(@PathVariable Long campaignId) {
        List<VolunteerShift> shifts = shiftService.getShiftsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/campaign/{campaignId}/available")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getAvailableShiftsByCampaign(@PathVariable Long campaignId) {
        List<VolunteerShift> shifts = shiftService.getAvailableShiftsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<VolunteerShift>>> getShiftsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<VolunteerShift> shifts = shiftService.getShiftsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VolunteerShift>> createShift(@Valid @RequestBody VolunteerShift shift) {
        try {
            // Get job ID from the nested volunteerJob object
            Long jobId = shift.getVolunteerJob() != null ? shift.getVolunteerJob().getId() : null;
            if (jobId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Volunteer Job is required"));
            }
            VolunteerShift created = shiftService.createShift(shift, jobId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Volunteer Shift created successfully", created));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VolunteerShift>> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerShift shift) {
        try {
            VolunteerShift updated = shiftService.updateShift(id, shift);
            return ResponseEntity.ok(ApiResponse.success("Volunteer Shift updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success("Volunteer Shift deleted successfully", null));
    }

    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<ApiResponse<Long>> getShiftCountByJob(@PathVariable Long jobId) {
        long count = shiftService.getShiftCountByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
