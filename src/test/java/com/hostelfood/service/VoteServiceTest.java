package com.hostelfood.service;

import com.hostelfood.dto.vote.VoteRequestDTO;
import com.hostelfood.dto.vote.VoteResponseDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.entity.User;
import com.hostelfood.entity.Vote;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.MealType;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.DuplicateVoteException;
import com.hostelfood.exception.InvalidFoodOptionException;
import com.hostelfood.exception.VotingClosedException;
import com.hostelfood.repository.FoodOptionRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.impl.VoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private FoodOptionRepository foodOptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoteServiceImpl voteService;

    private User student;
    private Meal activeMeal;
    private FoodOption foodOption;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .name("Student Arun")
                .email("arun@example.com")
                .role(Role.STUDENT)
                .build();

        activeMeal = Meal.builder()
                .id(100L)
                .date(LocalDate.now())
                .mealType(MealType.LUNCH)
                .votingStartTime(LocalDateTime.now().minusHours(1))
                .votingEndTime(LocalDateTime.now().plusHours(2))
                .deliveryStartTime(LocalDateTime.now().plusHours(3))
                .deliveryEndTime(LocalDateTime.now().plusHours(4))
                .status(MealStatus.VOTING_OPEN)
                .build();

        foodOption = FoodOption.builder()
                .id(200L)
                .name("Veg Biryani")
                .meal(activeMeal)
                .build();
    }

    @Test
    @DisplayName("Should submit vote successfully when window is open and option is valid")
    void testSubmitVote_Success() {
        VoteRequestDTO request = VoteRequestDTO.builder().foodOptionId(200L).build();

        when(userRepository.findByEmail("arun@example.com")).thenReturn(Optional.of(student));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(activeMeal));
        when(voteRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(false);
        when(foodOptionRepository.findById(200L)).thenReturn(Optional.of(foodOption));

        Vote savedVote = Vote.builder()
                .id(50L)
                .user(student)
                .meal(activeMeal)
                .foodOption(foodOption)
                .build();

        when(voteRepository.save(any(Vote.class))).thenReturn(savedVote);

        VoteResponseDTO response = voteService.submitVote(100L, request, "arun@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getMealId());
        assertEquals("Veg Biryani", response.getFoodName());
        verify(voteRepository, times(1)).save(any(Vote.class));
    }

    @Test
    @DisplayName("Should throw DuplicateVoteException when user already voted")
    void testSubmitVote_Duplicate() {
        VoteRequestDTO request = VoteRequestDTO.builder().foodOptionId(200L).build();

        when(userRepository.findByEmail("arun@example.com")).thenReturn(Optional.of(student));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(activeMeal));
        when(voteRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(true);

        assertThrows(DuplicateVoteException.class, () -> voteService.submitVote(100L, request, "arun@example.com"));
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    @DisplayName("Should throw VotingClosedException when voting window has ended")
    void testSubmitVote_VotingClosed() {
        activeMeal.setVotingEndTime(LocalDateTime.now().minusMinutes(5));
        activeMeal.setStatus(MealStatus.VOTING_CLOSED);

        VoteRequestDTO request = VoteRequestDTO.builder().foodOptionId(200L).build();

        when(userRepository.findByEmail("arun@example.com")).thenReturn(Optional.of(student));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(activeMeal));

        assertThrows(VotingClosedException.class, () -> voteService.submitVote(100L, request, "arun@example.com"));
    }

    @Test
    @DisplayName("Should throw InvalidFoodOptionException when food option belongs to another meal")
    void testSubmitVote_InvalidFoodOption() {
        Meal otherMeal = Meal.builder().id(999L).build();
        FoodOption otherOption = FoodOption.builder().id(200L).meal(otherMeal).name("Pizza").build();

        VoteRequestDTO request = VoteRequestDTO.builder().foodOptionId(200L).build();

        when(userRepository.findByEmail("arun@example.com")).thenReturn(Optional.of(student));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(activeMeal));
        when(voteRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(false);
        when(foodOptionRepository.findById(200L)).thenReturn(Optional.of(otherOption));

        assertThrows(InvalidFoodOptionException.class, () -> voteService.submitVote(100L, request, "arun@example.com"));
    }
}
