package com.hostelfood.service;

import com.hostelfood.dto.auth.LoginRequestDTO;
import com.hostelfood.dto.auth.LoginResponseDTO;
import com.hostelfood.dto.auth.RegisterRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.EmailAlreadyExistsException;
import com.hostelfood.exception.InvalidCredentialsException;
import com.hostelfood.exception.StudentIdAlreadyExistsException;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.security.JwtTokenProvider;
import com.hostelfood.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleStudent;

    @BeforeEach
    void setUp() {
        sampleStudent = User.builder()
                .id(10L)
                .name("Test Student")
                .email("student@test.com")
                .studentId("ST001")
                .password("encoded_pwd")
                .role(Role.STUDENT)
                .hostel("Block A")
                .roomNumber("101")
                .build();
    }

    @Test
    @DisplayName("Should register new student successfully")
    void testRegisterUser_Success() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Test Student")
                .email("student@test.com")
                .password("plain_pwd")
                .studentId("ST001")
                .hostel("Block A")
                .roomNumber("101")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByStudentId(request.getStudentId())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pwd");
        when(userRepository.save(any(User.class))).thenReturn(sampleStudent);

        UserResponseDTO response = userService.register(request);

        assertNotNull(response);
        assertEquals("student@test.com", response.getEmail());
        assertEquals("ST001", response.getStudentId());
        assertEquals(Role.STUDENT, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
    void testRegisterUser_DuplicateEmail() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("student@test.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw StudentIdAlreadyExistsException when studentId is taken")
    void testRegisterUser_DuplicateStudentId() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("new@test.com")
                .studentId("ST001")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByStudentId(request.getStudentId())).thenReturn(true);

        assertThrows(StudentIdAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("student@test.com")
                .password("plain_pwd")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleStudent));
        when(passwordEncoder.matches(request.getPassword(), sampleStudent.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(sampleStudent)).thenReturn("mocked_jwt_token");

        LoginResponseDTO response = userService.login(request);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("student@test.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void testLogin_WrongPassword() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("student@test.com")
                .password("wrong_pwd")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleStudent));
        when(passwordEncoder.matches(request.getPassword(), sampleStudent.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
    }
}
