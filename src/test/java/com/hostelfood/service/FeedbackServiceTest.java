package com.hostelfood.service;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.feedback.FeedbackRequestDTO;
import com.hostelfood.dto.feedback.FeedbackResponseDTO;
import com.hostelfood.dto.feedback.MealFeedbackSummaryDTO;
import com.hostelfood.entity.*;
import com.hostelfood.enums.FeedbackCategory;
import com.hostelfood.enums.MealType;
import com.hostelfood.enums.Role;
import com.hostelfood.exception.DuplicateFeedbackException;
import com.hostelfood.exception.FeedbackNotFoundException;
import com.hostelfood.exception.MealNotConsumedException;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.FeedbackRepository;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.service.impl.FeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private MealDeliveryRepository mealDeliveryRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private User sampleStudent;
    private Meal sampleMeal;
    private FoodOption sampleFoodOption;
    private MealDelivery sampleDelivery;
    private Feedback sampleFeedback;

    @BeforeEach
    void setUp() {
        sampleStudent = User.builder()
                .id(1L)
                .name("Student One")
                .email("student@test.com")
                .studentId("ST001")
                .role(Role.STUDENT)
                .build();

        sampleMeal = Meal.builder()
                .id(100L)
                .mealType(MealType.DINNER)
                .date(LocalDate.of(2026, 9, 15))
                .build();

        sampleFoodOption = FoodOption.builder()
                .id(10L)
                .meal(sampleMeal)
                .name("Paneer Butter Masala")
                .build();

        sampleDelivery = MealDelivery.builder()
                .id(50L)
                .user(sampleStudent)
                .meal(sampleMeal)
                .foodOption(sampleFoodOption)
                .deliveredAt(LocalDateTime.now())
                .build();

        sampleFeedback = Feedback.builder()
                .id(500L)
                .user(sampleStudent)
                .meal(sampleMeal)
                .foodOption(sampleFoodOption)
                .rating(5)
                .category(FeedbackCategory.TASTE)
                .comment("Delicious gravy and fresh paneer!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should submit feedback successfully when student has collected the meal")
    void testSubmitFeedback_Success() {
        FeedbackRequestDTO request = FeedbackRequestDTO.builder()
                .rating(5)
                .category(FeedbackCategory.TASTE)
                .comment("Delicious gravy and fresh paneer!")
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sampleStudent));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(false);
        when(mealDeliveryRepository.findByUserIdAndMealId(1L, 100L)).thenReturn(Optional.of(sampleDelivery));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(sampleFeedback);

        FeedbackResponseDTO response = feedbackService.submitFeedback(100L, request, "student@test.com");

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals(FeedbackCategory.TASTE, response.getCategory());
        assertEquals("Paneer Butter Masala", response.getFoodOptionName());
        assertEquals("ST001", response.getStudentId());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should throw MealNotConsumedException when student did not collect the meal")
    void testSubmitFeedback_MealNotConsumed() {
        FeedbackRequestDTO request = FeedbackRequestDTO.builder()
                .rating(4)
                .category(FeedbackCategory.GENERAL)
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sampleStudent));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(false);
        when(mealDeliveryRepository.findByUserIdAndMealId(1L, 100L)).thenReturn(Optional.empty());

        assertThrows(MealNotConsumedException.class, () ->
                feedbackService.submitFeedback(100L, request, "student@test.com"));

        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should throw DuplicateFeedbackException when student has already submitted feedback")
    void testSubmitFeedback_DuplicateFeedback() {
        FeedbackRequestDTO request = FeedbackRequestDTO.builder()
                .rating(3)
                .category(FeedbackCategory.QUANTITY)
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sampleStudent));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.existsByUserIdAndMealId(1L, 100L)).thenReturn(true);

        assertThrows(DuplicateFeedbackException.class, () ->
                feedbackService.submitFeedback(100L, request, "student@test.com"));

        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should retrieve student's own feedback for a meal")
    void testGetStudentFeedback_Success() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sampleStudent));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.findByUserIdAndMealId(1L, 100L)).thenReturn(Optional.of(sampleFeedback));

        FeedbackResponseDTO response = feedbackService.getStudentFeedback(100L, "student@test.com");

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Student One", response.getStudentName());
    }

    @Test
    @DisplayName("Should throw FeedbackNotFoundException when student has not submitted feedback")
    void testGetStudentFeedback_NotFound() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sampleStudent));
        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.findByUserIdAndMealId(1L, 100L)).thenReturn(Optional.empty());

        assertThrows(FeedbackNotFoundException.class, () ->
                feedbackService.getStudentFeedback(100L, "student@test.com"));
    }

    @Test
    @DisplayName("Should compute meal feedback summary with average rating and star distribution")
    void testGetMealFeedbackSummary_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Feedback> page = new PageImpl<>(List.of(sampleFeedback), pageable, 1);

        when(mealRepository.findById(100L)).thenReturn(Optional.of(sampleMeal));
        when(feedbackRepository.findByMealId(100L, pageable)).thenReturn(page);
        when(feedbackRepository.findByMealId(100L)).thenReturn(List.of(sampleFeedback));
        when(feedbackRepository.findAverageRatingByMealId(100L)).thenReturn(4.8);

        MealFeedbackSummaryDTO summary = feedbackService.getMealFeedbackSummary(100L, pageable);

        assertNotNull(summary);
        assertEquals(100L, summary.getMealId());
        assertEquals(MealType.DINNER, summary.getMealType());
        assertEquals(LocalDate.of(2026, 9, 15), summary.getMealDate());
        assertEquals(1, summary.getTotalFeedbacks());
        assertEquals(4.8, summary.getAverageRating());
        assertEquals(1L, summary.getStarDistribution().get(5));
        assertEquals(0L, summary.getStarDistribution().get(1));
        assertEquals(1L, summary.getCategoryDistribution().get(FeedbackCategory.TASTE));
        assertEquals(1, summary.getFeedbacks().getContent().size());
    }

    @Test
    @DisplayName("Should retrieve all feedbacks globally with pagination")
    void testGetAllFeedbacks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Feedback> page = new PageImpl<>(List.of(sampleFeedback), pageable, 1);

        when(feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        PagedResponseDTO<FeedbackResponseDTO> response = feedbackService.getAllFeedbacks(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }
}
