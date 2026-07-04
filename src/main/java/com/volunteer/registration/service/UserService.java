package com.volunteer.registration.service;

import com.volunteer.registration.dto.UserDTO;
import com.volunteer.registration.model.User;
import com.volunteer.registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(UserDTO dto) {
        // Check if username already exists
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Validate password length
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.UserRole.valueOf(dto.getRole()));
        user.setActive(dto.isActive());

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is being changed to an existing username
        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
        }

        user.setUsername(dto.getUsername());

        // Only update password if provided and not empty
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (dto.getPassword().length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setRole(User.UserRole.valueOf(dto.getRole()));
        user.setActive(dto.isActive());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public User toggleActiveStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean usernameExistsExcludingId(String username, Long userId) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && !user.get().getId().equals(userId);
    }
}
