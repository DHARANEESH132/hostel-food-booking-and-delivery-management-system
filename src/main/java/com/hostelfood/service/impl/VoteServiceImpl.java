package com.hostelfood.service.impl;

import com.hostelfood.dto.vote.VoteRequestDTO;
import com.hostelfood.dto.vote.VoteResponseDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.entity.User;
import com.hostelfood.entity.Vote;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.exception.DuplicateVoteException;
import com.hostelfood.exception.InvalidFoodOptionException;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.exception.VoteNotFoundException;
import com.hostelfood.exception.VotingClosedException;
import com.hostelfood.repository.FoodOptionRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final MealRepository mealRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VoteResponseDTO submitVote(Long mealId, VoteRequestDTO request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Meal meal = getMealById(mealId);

        validateVotingWindow(meal);

        if (voteRepository.existsByUserIdAndMealId(user.getId(), meal.getId())) {
            throw new DuplicateVoteException("You have already voted for this meal. Use PUT /api/student/meals/"
                    + mealId + "/vote to change your vote.");
        }

        FoodOption foodOption = getAndValidateFoodOption(request.getFoodOptionId(), meal);

        Vote vote = Vote.builder()
                .user(user)
                .meal(meal)
                .foodOption(foodOption)
                .build();

        Vote savedVote = voteRepository.save(vote);
        return mapToResponseDTO(savedVote, "Vote submitted successfully");
    }

    @Override
    @Transactional
    public VoteResponseDTO updateVote(Long mealId, VoteRequestDTO request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Meal meal = getMealById(mealId);

        validateVotingWindow(meal);

        Vote vote = voteRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new VoteNotFoundException("No existing vote found for meal id: " + mealId));

        FoodOption foodOption = getAndValidateFoodOption(request.getFoodOptionId(), meal);

        vote.setFoodOption(foodOption);
        Vote updatedVote = voteRepository.save(vote);

        return mapToResponseDTO(updatedVote, "Vote updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public VoteResponseDTO getStudentVote(Long mealId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Meal meal = getMealById(mealId);

        Vote vote = voteRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new VoteNotFoundException("You have not voted for meal id: " + mealId));

        return mapToResponseDTO(vote, "Vote details retrieved successfully");
    }

    @Override
    @Transactional
    public void deleteVote(Long mealId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Meal meal = getMealById(mealId);

        validateVotingWindow(meal);

        Vote vote = voteRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new VoteNotFoundException("No vote found to delete for meal id: " + mealId));

        voteRepository.delete(vote);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private Meal getMealById(Long mealId) {
        return mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + mealId));
    }

    private void validateVotingWindow(Meal meal) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(meal.getVotingStartTime()) || now.isAfter(meal.getVotingEndTime())
                || meal.getStatus() == MealStatus.VOTING_CLOSED || meal.getStatus() == MealStatus.COMPLETED) {
            throw new VotingClosedException("Voting is not open for this meal. Voting window: "
                    + meal.getVotingStartTime() + " to " + meal.getVotingEndTime());
        }
    }

    private FoodOption getAndValidateFoodOption(Long foodOptionId, Meal meal) {
        FoodOption foodOption = foodOptionRepository.findById(foodOptionId)
                .orElseThrow(() -> new InvalidFoodOptionException("Food option not found with id: " + foodOptionId));

        if (foodOption.getMeal() == null || !foodOption.getMeal().getId().equals(meal.getId())) {
            throw new InvalidFoodOptionException("Food option with id " + foodOptionId
                    + " does not belong to meal id " + meal.getId());
        }

        return foodOption;
    }

    private VoteResponseDTO mapToResponseDTO(Vote vote, String message) {
        return VoteResponseDTO.builder()
                .id(vote.getId())
                .mealId(vote.getMeal().getId())
                .mealDate(vote.getMeal().getDate())
                .mealType(vote.getMeal().getMealType())
                .foodOptionId(vote.getFoodOption().getId())
                .foodName(vote.getFoodOption().getName())
                .message(message)
                .createdAt(vote.getCreatedAt())
                .updatedAt(vote.getUpdatedAt())
                .build();
    }
}
