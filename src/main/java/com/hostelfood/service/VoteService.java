package com.hostelfood.service;

import com.hostelfood.dto.vote.VoteRequestDTO;
import com.hostelfood.dto.vote.VoteResponseDTO;

public interface VoteService {

    VoteResponseDTO submitVote(Long mealId, VoteRequestDTO request, String userEmail);

    VoteResponseDTO updateVote(Long mealId, VoteRequestDTO request, String userEmail);

    VoteResponseDTO getStudentVote(Long mealId, String userEmail);

    void deleteVote(Long mealId, String userEmail);
}
