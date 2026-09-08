package com.hostelfood.service.impl;

import com.hostelfood.dto.auth.LoginRequestDTO;
import com.hostelfood.dto.auth.LoginResponseDTO;
import com.hostelfood.dto.auth.RegisterRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import com.hostelfood.dto.user.UserSummaryDTO;
import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.EmailAlreadyExistsException;
import com.hostelfood.exception.InvalidCredentialsException;
import com.hostelfood.exception.StudentIdAlreadyExistsException;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.security.JwtTokenProvider;
import com.hostelfood.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user);

        UserSummaryDTO userSummary = UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail().trim() + "' is already registered");
        }

        if (userRepository.existsByStudentId(request.getStudentId().trim())) {
            throw new StudentIdAlreadyExistsException("Student ID '" + request.getStudentId().trim() + "' is already registered");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .studentId(request.getStudentId().trim())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .hostel(request.getHostel().trim())
                .roomNumber(request.getRoomNumber().trim())
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .role(user.getRole())
                .hostel(user.getHostel())
                .roomNumber(user.getRoomNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
