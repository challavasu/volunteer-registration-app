package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.model.Campaign;
import com.volunteer.registration.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Campaign>>> getAllCampaigns() {
        List<Campaign> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Campaign>>> getActiveCampaigns() {
        List<Campaign> campaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Campaign>> getCampaignById(@PathVariable Long id) {
        return campaignService.getCampaignById(id)
                .map(campaign -> ResponseEntity.ok(ApiResponse.success(campaign)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Campaign not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Campaign>> createCampaign(@Valid @RequestBody Campaign campaign) {
        Campaign created = campaignService.createCampaign(campaign);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campaign created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Campaign>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody Campaign campaign) {
        try {
            Campaign updated = campaignService.updateCampaign(id, campaign);
            return ResponseEntity.ok(ApiResponse.success("Campaign updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign deleted successfully", null));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCampaign(@PathVariable Long id) {
        try {
            campaignService.deactivateCampaign(id);
            return ResponseEntity.ok(ApiResponse.success("Campaign deactivated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Campaign>>> searchCampaigns(@RequestParam String name) {
        List<Campaign> campaigns = campaignService.searchCampaigns(name);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Campaign>>> getCampaignsByStatus(@PathVariable String status) {
        List<Campaign> campaigns = campaignService.getCampaignsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Campaign>>> getCampaignsByType(@PathVariable String type) {
        List<Campaign> campaigns = campaignService.getCampaignsByType(type);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<String>>> getAllCampaignTypes() {
        List<String> types = campaignService.getAllCampaignTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<String>>> getAllCampaignStatuses() {
        List<String> statuses = campaignService.getAllCampaignStatuses();
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<Campaign>>> getChildCampaigns(@PathVariable Long id) {
        List<Campaign> children = campaignService.getChildCampaigns(id);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<Campaign>> cloneCampaign(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("newName");
            String newStartDateStr = request.get("newStartDate");
            
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("New campaign name is required"));
            }
            
            LocalDate newStartDate = null;
            if (newStartDateStr != null && !newStartDateStr.isEmpty()) {
                newStartDate = LocalDate.parse(newStartDateStr);
            }
            
            Campaign cloned = campaignService.cloneCampaign(id, newName, newStartDate);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Campaign cloned successfully", cloned));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
