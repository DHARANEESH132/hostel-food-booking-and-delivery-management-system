package com.hostelfood.service;

import com.hostelfood.dto.auth.LoginRequestDTO;
import com.hostelfood.dto.auth.LoginResponseDTO;
import com.hostelfood.dto.auth.RegisterRequestDTO;
import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.user.CreateStudentRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDTO register(RegisterRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    UserResponseDTO createStudentByAdmin(CreateStudentRequestDTO request);

    PagedResponseDTO<UserResponseDTO> getAllStudents(Pageable pageable);

    void deleteStudent(Long id);
}
