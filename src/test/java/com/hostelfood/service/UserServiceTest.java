package com.hostelfood.service;

import com.hostelfood.dto.auth.LoginRequestDTO;
import com.hostelfood.dto.auth.LoginResponseDTO;
import com.hostelfood.dto.auth.RegisterRequestDTO;
import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.user.CreateStudentRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.EmailAlreadyExistsException;
import com.hostelfood.exception.InvalidCredentialsException;
import com.hostelfood.exception.PublicRegistrationDisabledException;
import com.hostelfood.exception.StudentIdAlreadyExistsException;
import com.hostelfood.exception.StudentNotFoundException;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.security.JwtTokenProvider;
import com.hostelfood.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
    @DisplayName("Should throw PublicRegistrationDisabledException when public self-register is invoked")
    void testRegister_PublicDisabled() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Outsider")
                .email("outsider@gmail.com")
                .password("pwd123")
                .studentId("ST999")
                .hostel("Block A")
                .roomNumber("101")
                .build();

        assertThrows(PublicRegistrationDisabledException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should onboard new student by admin successfully with explicit password")
    void testCreateStudentByAdmin_WithExplicitPassword() {
        CreateStudentRequestDTO request = CreateStudentRequestDTO.builder()
                .name("Test Student")
                .email("student@test.com")
                .studentId("ST001")
                .hostel("Block A")
                .roomNumber("101")
                .password("custom_pwd")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByStudentId(request.getStudentId())).thenReturn(false);
        when(passwordEncoder.encode("custom_pwd")).thenReturn("encoded_custom_pwd");
        when(userRepository.save(any(User.class))).thenReturn(sampleStudent);

        UserResponseDTO response = userService.createStudentByAdmin(request);

        assertNotNull(response);
        assertEquals("student@test.com", response.getEmail());
        assertEquals("ST001", response.getStudentId());
        assertEquals(Role.STUDENT, response.getRole());
        verify(passwordEncoder).encode("custom_pwd");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should onboard new student by admin with default password (<studentId>@123) when password is omitted")
    void testCreateStudentByAdmin_WithDefaultPassword() {
        CreateStudentRequestDTO request = CreateStudentRequestDTO.builder()
                .name("Test Student")
                .email("student@test.com")
                .studentId("ST001")
                .hostel("Block A")
                .roomNumber("101")
                .password(null)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByStudentId(request.getStudentId())).thenReturn(false);
        when(passwordEncoder.encode("ST001@123")).thenReturn("encoded_default_pwd");
        when(userRepository.save(any(User.class))).thenReturn(sampleStudent);

        UserResponseDTO response = userService.createStudentByAdmin(request);

        assertNotNull(response);
        verify(passwordEncoder).encode("ST001@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when creating student with duplicate email")
    void testCreateStudentByAdmin_DuplicateEmail() {
        CreateStudentRequestDTO request = CreateStudentRequestDTO.builder()
                .email("student@test.com")
                .studentId("ST002")
                .name("New Student")
                .hostel("Block B")
                .roomNumber("201")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createStudentByAdmin(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw StudentIdAlreadyExistsException when creating student with duplicate studentId")
    void testCreateStudentByAdmin_DuplicateStudentId() {
        CreateStudentRequestDTO request = CreateStudentRequestDTO.builder()
                .email("new@test.com")
                .studentId("ST001")
                .name("New Student")
                .hostel("Block B")
                .roomNumber("201")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByStudentId(request.getStudentId())).thenReturn(true);

        assertThrows(StudentIdAlreadyExistsException.class, () -> userService.createStudentByAdmin(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should retrieve paginated list of students")
    void testGetAllStudents_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(sampleStudent), pageable, 1);

        when(userRepository.findByRole(Role.STUDENT, pageable)).thenReturn(page);

        PagedResponseDTO<UserResponseDTO> result = userService.getAllStudents(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("ST001", result.getContent().get(0).getStudentId());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("Should delete student successfully")
    void testDeleteStudent_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));

        userService.deleteStudent(10L);

        verify(userRepository, times(1)).delete(sampleStudent);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student ID does not exist")
    void testDeleteStudent_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> userService.deleteStudent(999L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when user to delete is not a student")
    void testDeleteStudent_NotAStudent() {
        User adminUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(StudentNotFoundException.class, () -> userService.deleteStudent(1L));
        verify(userRepository, never()).delete(any(User.class));
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
