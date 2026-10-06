package com.hostelfood.service.impl;

import com.hostelfood.dto.auth.LoginRequestDTO;
import com.hostelfood.dto.auth.LoginResponseDTO;
import com.hostelfood.dto.auth.RegisterRequestDTO;
import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.user.CreateStudentRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import com.hostelfood.dto.user.UserSummaryDTO;
import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.EmailAlreadyExistsException;
import com.hostelfood.exception.InvalidCredentialsException;
import com.hostelfood.exception.PublicRegistrationDisabledException;
import com.hostelfood.exception.StudentIdAlreadyExistsException;
import com.hostelfood.exception.StudentNotFoundException;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.security.JwtTokenProvider;
import com.hostelfood.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  UserServiceImpl implements UserService {

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
    public UserResponseDTO register(RegisterRequestDTO request) {
        throw new PublicRegistrationDisabledException(
                "Public self-registration is disabled. Please contact the hostel administrator/warden to register your account.");
    }

    @Override
    @Transactional
    public UserResponseDTO createStudentByAdmin(CreateStudentRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail().trim() + "' is already registered");
        }

        if (userRepository.existsByStudentId(request.getStudentId().trim())) {
            throw new StudentIdAlreadyExistsException("Student ID '" + request.getStudentId().trim() + "' is already registered");
        }

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = request.getStudentId().trim() + "@123";
        }

        User user = User.builder()
                .name(request.getName().trim())
                .studentId(request.getStudentId().trim())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(rawPassword.trim()))
                .role(Role.STUDENT)
                .hostel(request.getHostel().trim())
                .roomNumber(request.getRoomNumber().trim())
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserResponseDTO> getAllStudents(Pageable pageable) {
        Page<User> studentPage = userRepository.findByRole(Role.STUDENT, pageable);
        List<UserResponseDTO> content = studentPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<UserResponseDTO>builder()
                .content(content)
                .pageNumber(studentPage.getNumber())
                .pageSize(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .last(studentPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));

        if (user.getRole() != Role.STUDENT) {
            throw new StudentNotFoundException("User with ID: " + id + " is not a student");
        }

        userRepository.delete(user);
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
