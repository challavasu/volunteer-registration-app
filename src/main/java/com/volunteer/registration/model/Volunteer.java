package com.volunteer.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "volunteers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^[\\d\\s\\-\\(\\)\\+]*$", message = "Phone number contains invalid characters")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    private String address;

    private String city;

    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    private String skills;

    @Column(length = 1000)
    private String interests;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Registration> registrations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
