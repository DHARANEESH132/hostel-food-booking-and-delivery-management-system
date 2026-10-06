package com.hostelfood.controller;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.user.CreateStudentRequestDTO;
import com.hostelfood.dto.user.UserResponseDTO;
import com.hostelfood.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createStudent(@Valid @RequestBody CreateStudentRequestDTO request) {
        UserResponseDTO response = userService.createStudentByAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getAllStudents(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllStudents(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
