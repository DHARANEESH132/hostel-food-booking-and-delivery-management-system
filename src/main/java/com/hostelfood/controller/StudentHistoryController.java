package com.hostelfood.controller;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.user.StudentHistoryItemDTO;
import com.hostelfood.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/history")
@RequiredArgsConstructor
public class StudentHistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<PagedResponseDTO<StudentHistoryItemDTO>> getMyHistory(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(historyService.getStudentHistory(authentication.getName(), pageable));
    }
}
