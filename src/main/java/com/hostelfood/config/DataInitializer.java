package com.hostelfood.config;

import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import com.hostelfood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@hostel.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .studentId(null)
                    .hostel("Admin Block")
                    .roomNumber("001")
                    .build();

            userRepository.save(admin);
            log.info("Initialized default ADMIN user: admin@hostel.com");
        }
    }
}
