package com.volunteer.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRegistrationDTO {

    @NotNull(message = "Shift ID is required")
    private Long shiftId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String volunteerEmail;

    private String notes;
}
