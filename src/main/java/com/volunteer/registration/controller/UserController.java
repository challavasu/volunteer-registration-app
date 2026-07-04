package com.volunteer.registration.controller;

import com.volunteer.registration.dto.ApiResponse;
import com.volunteer.registration.dto.UserDTO;
import com.volunteer.registration.model.User;
import com.volunteer.registration.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    private boolean isAdmin(HttpSession session) {
        User.UserRole userRole = (User.UserRole) session.getAttribute("userRole");
        return userRole == User.UserRole.ADMIN;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can access this endpoint"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.getAllUsers(pageable);

        List<Map<String, Object>> usersList = userPage.getContent().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", user.getRole().toString());
            map.put("active", user.isActive());
            map.put("createdAt", user.getCreatedAt());
            map.put("lastLogin", user.getLastLogin());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", usersList);
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalElements", userPage.getTotalElements());
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can access this endpoint"));
        }

        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(UserDTO.fromUser(user))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody UserDTO dto,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can create users"));
        }

        try {
            // Validate role
            try {
                User.UserRole.valueOf(dto.getRole());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid role. Must be ADMIN or LEAD"));
            }

            User user = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", UserDTO.fromUser(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can update users"));
        }

        try {
            // Validate role
            try {
                User.UserRole.valueOf(dto.getRole());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid role. Must be ADMIN or LEAD"));
            }

            User user = userService.updateUser(id, dto);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", UserDTO.fromUser(user)));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can delete users"));
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserDTO>> toggleActiveStatus(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can toggle user status"));
        }

        try {
            User user = userService.toggleActiveStatus(id);
            return ResponseEntity.ok(ApiResponse.success("User status updated", UserDTO.fromUser(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(
            @PathVariable String username,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only ADMIN users can check username availability"));
        }

        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
