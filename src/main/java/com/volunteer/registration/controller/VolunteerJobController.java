package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.model.VolunteerJob;
import com.volunteer.registration.service.VolunteerJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VolunteerJobController {

    private final VolunteerJobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getAllJobs() {
        List<VolunteerJob> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getActiveJobs() {
        List<VolunteerJob> jobs = jobService.getActiveJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getAvailableJobs() {
        List<VolunteerJob> jobs = jobService.getAvailableJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getJobsByCampaign(@PathVariable Long campaignId) {
        List<VolunteerJob> jobs = jobService.getJobsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/campaign/{campaignId}/available")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getAvailableJobsByCampaign(@PathVariable Long campaignId) {
        List<VolunteerJob> jobs = jobService.getAvailableJobsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VolunteerJob>> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Volunteer Job not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VolunteerJob>> createJob(@Valid @RequestBody VolunteerJob job) {
        try {
            // Get campaign ID from the nested campaign object
            Long campaignId = job.getCampaign() != null ? job.getCampaign().getId() : null;
            if (campaignId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Campaign is required"));
            }
            VolunteerJob created = jobService.createJob(job, campaignId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Volunteer Job created successfully", created));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VolunteerJob>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerJob job) {
        try {
            VolunteerJob updated = jobService.updateJob(id, job);
            return ResponseEntity.ok(ApiResponse.success("Volunteer Job updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Volunteer Job deleted successfully", null));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateJob(@PathVariable Long id) {
        try {
            jobService.deactivateJob(id);
            return ResponseEntity.ok(ApiResponse.success("Volunteer Job deactivated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> searchJobs(@RequestParam String name) {
        List<VolunteerJob> jobs = jobService.searchJobs(name);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/ongoing")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getOngoingJobs() {
        List<VolunteerJob> jobs = jobService.getOngoingJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/team-lead/{teamLead}")
    public ResponseEntity<ApiResponse<List<VolunteerJob>>> getJobsByTeamLead(@PathVariable String teamLead) {
        List<VolunteerJob> jobs = jobService.getJobsByTeamLead(teamLead);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<VolunteerJob>> cloneJob(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("newName");
            
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("New job name is required"));
            }
            
            VolunteerJob cloned = jobService.cloneJob(id, newName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Job cloned successfully with all shifts", cloned));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
