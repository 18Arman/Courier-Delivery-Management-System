package com.smartcourier.auth.config;

import com.smartcourier.auth.entity.Role;
import com.smartcourier.auth.entity.User;
import com.smartcourier.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seedAdmin(
            @Value("${app.bootstrap.admin.email}") String adminEmail,
            @Value("${app.bootstrap.admin.password}") String adminPassword,
            @Value("${app.bootstrap.admin.full-name}") String adminFullName,
            @Value("${app.bootstrap.admin.phone-number}") String adminPhoneNumber
    ) {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User user = new User();
            user.setEmail(adminEmail);
            user.setFullName(adminFullName);
            user.setPhoneNumber(adminPhoneNumber);
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.getRoles().add(Role.ROLE_ADMIN);
            userRepository.save(user);
        };
    }
}
