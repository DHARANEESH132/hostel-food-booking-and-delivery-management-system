package com.hostelfood.controller;

import com.hostelfood.dto.vote.VoteRequestDTO;
import com.hostelfood.dto.vote.VoteResponseDTO;
import com.hostelfood.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/meals/{mealId}/vote")
@RequiredArgsConstructor
public class StudentVoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<VoteResponseDTO> submitVote(
            @PathVariable Long mealId,
            @Valid @RequestBody VoteRequestDTO request,
            Authentication authentication) {
        VoteResponseDTO response = voteService.submitVote(mealId, request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<VoteResponseDTO> updateVote(
            @PathVariable Long mealId,
            @Valid @RequestBody VoteRequestDTO request,
            Authentication authentication) {
        VoteResponseDTO response = voteService.updateVote(mealId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<VoteResponseDTO> getStudentVote(
            @PathVariable Long mealId,
            Authentication authentication) {
        VoteResponseDTO response = voteService.getStudentVote(mealId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteVote(
            @PathVariable Long mealId,
            Authentication authentication) {
        voteService.deleteVote(mealId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
