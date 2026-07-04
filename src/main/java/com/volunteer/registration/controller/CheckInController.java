package com.volunteer.registration.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.model.Registration;
import com.volunteer.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final RegistrationRepository registrationRepository;

    /**
     * Check in a volunteer using their check-in code
     */
    @PostMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Registration>> checkInByCode(@PathVariable String code) {
        Optional<Registration> registration = registrationRepository.findByCheckInCode(code.toUpperCase());
        
        if (registration.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Invalid check-in code. Please verify and try again."));
        }
        
        Registration reg = registration.get();
        
        if (reg.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("This registration has been cancelled."));
        }
        
        if (reg.isCheckedIn()) {
            return ResponseEntity.ok(ApiResponse.success("Already checked in at " + 
                    reg.getCheckInTime().toLocalTime().toString(), reg));
        }
        
        reg.setCheckedIn(true);
        reg.setCheckInTime(LocalDateTime.now());
        reg.setStatus(Registration.RegistrationStatus.CONFIRMED);
        Registration saved = registrationRepository.save(reg);
        
        return ResponseEntity.ok(ApiResponse.success("Check-in successful! Welcome, " + 
                reg.getVolunteer().getFirstName() + "!", saved));
    }

    /**
     * Look up registration by check-in code (without checking in)
     */
    @GetMapping("/lookup/{code}")
    public ResponseEntity<ApiResponse<Registration>> lookupByCode(@PathVariable String code) {
        Optional<Registration> registration = registrationRepository.findByCheckInCode(code.toUpperCase());
        
        if (registration.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Invalid check-in code."));
        }
        
        return ResponseEntity.ok(ApiResponse.success(registration.get()));
    }

    /**
     * Get all pending check-ins for a campaign
     */
    @GetMapping("/campaign/{campaignId}/pending")
    public ResponseEntity<ApiResponse<List<Registration>>> getPendingCheckIns(@PathVariable Long campaignId) {
        List<Registration> pending = registrationRepository.findPendingCheckInsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    /**
     * Get all checked-in registrations for a campaign
     */
    @GetMapping("/campaign/{campaignId}/checked-in")
    public ResponseEntity<ApiResponse<List<Registration>>> getCheckedIn(@PathVariable Long campaignId) {
        List<Registration> checkedIn = registrationRepository.findCheckedInByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(checkedIn));
    }

    /**
     * Get registrations for a specific shift with check-in status
     */
    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<ApiResponse<List<Registration>>> getShiftRegistrations(@PathVariable Long shiftId) {
        List<Registration> registrations = registrationRepository.findByShiftIdWithDetails(shiftId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    /**
     * Generate QR code as base64 image for a check-in code
     */
    @GetMapping("/qr/{code}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getQRCode(@PathVariable String code) {
        try {
            String qrContent = code.toUpperCase();
            String base64Image = generateQRCodeBase64(qrContent, 200, 200);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "code", code.toUpperCase(),
                    "qrImage", "data:image/png;base64," + base64Image
            )));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate QR code"));
        }
    }

    /**
     * Generate QR code as PNG image
     */
    @GetMapping(value = "/qr/{code}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String code) {
        try {
            byte[] imageBytes = generateQRCodeBytes(code.toUpperCase(), 300, 300);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Undo check-in (for corrections)
     */
    @PostMapping("/undo/{code}")
    public ResponseEntity<ApiResponse<Registration>> undoCheckIn(@PathVariable String code) {
        Optional<Registration> registration = registrationRepository.findByCheckInCode(code.toUpperCase());
        
        if (registration.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Invalid check-in code."));
        }
        
        Registration reg = registration.get();
        
        if (!reg.isCheckedIn()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("This registration is not checked in."));
        }
        
        reg.setCheckedIn(false);
        reg.setCheckInTime(null);
        Registration saved = registrationRepository.save(reg);
        
        return ResponseEntity.ok(ApiResponse.success("Check-in undone successfully.", saved));
    }

    private String generateQRCodeBase64(String content, int width, int height) throws WriterException, IOException {
        byte[] imageBytes = generateQRCodeBytes(content, width, height);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private byte[] generateQRCodeBytes(String content, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
