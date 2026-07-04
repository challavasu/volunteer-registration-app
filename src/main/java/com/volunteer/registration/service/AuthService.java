package com.volunteer.registration.service;

import com.volunteer.registration.model.User;
import com.volunteer.registration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndActive(username, true);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public User createUser(String username, String password, User.UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
