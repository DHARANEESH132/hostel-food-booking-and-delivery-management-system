package com.hostelfood.service.impl;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.feedback.FeedbackRequestDTO;
import com.hostelfood.dto.feedback.FeedbackResponseDTO;
import com.hostelfood.dto.feedback.MealFeedbackSummaryDTO;
import com.hostelfood.entity.Feedback;
import com.hostelfood.entity.Meal;
import com.hostelfood.entity.MealDelivery;
import com.hostelfood.entity.User;
import com.hostelfood.enums.FeedbackCategory;
import com.hostelfood.exception.DuplicateFeedbackException;
import com.hostelfood.exception.FeedbackNotFoundException;
import com.hostelfood.exception.MealNotConsumedException;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.FeedbackRepository;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MealDeliveryRepository mealDeliveryRepository;
    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FeedbackResponseDTO submitFeedback(Long mealId, FeedbackRequestDTO request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Meal meal = getMealById(mealId);

        if (feedbackRepository.existsByUserIdAndMealId(user.getId(), meal.getId())) {
            throw new DuplicateFeedbackException("You have already submitted feedback for this meal");
        }

        MealDelivery delivery = mealDeliveryRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new MealNotConsumedException(
                        "You can only submit feedback for meals you have actually collected"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .meal(meal)
                .foodOption(delivery.getFoodOption())
                .rating(request.getRating())
                .category(request.getCategory())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return mapToResponseDTO(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponseDTO getStudentFeedback(Long mealId, String userEmail) {
        User user = getUserByEmail(userEmail);
        getMealById(mealId);

        Feedback feedback = feedbackRepository.findByUserIdAndMealId(user.getId(), mealId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for meal ID: " + mealId));

        return mapToResponseDTO(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public MealFeedbackSummaryDTO getMealFeedbackSummary(Long mealId, Pageable pageable) {
        Meal meal = getMealById(mealId);
        Page<Feedback> feedbackPage = feedbackRepository.findByMealId(mealId, pageable);
        List<Feedback> allFeedbacksForMeal = feedbackRepository.findByMealId(mealId);

        Map<Integer, Long> starDistribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            starDistribution.put(star, 0L);
        }
        for (Feedback f : allFeedbacksForMeal) {
            starDistribution.put(f.getRating(), starDistribution.getOrDefault(f.getRating(), 0L) + 1L);
        }

        Map<FeedbackCategory, Long> categoryDistribution = new LinkedHashMap<>();
        for (FeedbackCategory category : FeedbackCategory.values()) {
            categoryDistribution.put(category, 0L);
        }
        for (Feedback f : allFeedbacksForMeal) {
            categoryDistribution.put(f.getCategory(), categoryDistribution.getOrDefault(f.getCategory(), 0L) + 1L);
        }

        Double avg = feedbackRepository.findAverageRatingByMealId(mealId);
        Double averageRating = (avg != null) ? Math.round(avg * 10.0) / 10.0 : 0.0;

        List<FeedbackResponseDTO> content = feedbackPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<FeedbackResponseDTO> pagedFeedbacks = PagedResponseDTO.<FeedbackResponseDTO>builder()
                .content(content)
                .pageNumber(feedbackPage.getNumber())
                .pageSize(feedbackPage.getSize())
                .totalElements(feedbackPage.getTotalElements())
                .totalPages(feedbackPage.getTotalPages())
                .last(feedbackPage.isLast())
                .build();

        return MealFeedbackSummaryDTO.builder()
                .mealId(meal.getId())
                .mealType(meal.getMealType())
                .mealDate(meal.getDate())
                .totalFeedbacks(allFeedbacksForMeal.size())
                .averageRating(averageRating)
                .starDistribution(starDistribution)
                .categoryDistribution(categoryDistribution)
                .feedbacks(pagedFeedbacks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<FeedbackResponseDTO> getAllFeedbacks(Pageable pageable) {
        Page<Feedback> feedbackPage = feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<FeedbackResponseDTO> content = feedbackPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<FeedbackResponseDTO>builder()
                .content(content)
                .pageNumber(feedbackPage.getNumber())
                .pageSize(feedbackPage.getSize())
                .totalElements(feedbackPage.getTotalElements())
                .totalPages(feedbackPage.getTotalPages())
                .last(feedbackPage.isLast())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private Meal getMealById(Long mealId) {
        return mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with ID: " + mealId));
    }

    private FeedbackResponseDTO mapToResponseDTO(Feedback feedback) {
        return FeedbackResponseDTO.builder()
                .id(feedback.getId())
                .mealId(feedback.getMeal().getId())
                .mealType(feedback.getMeal().getMealType())
                .mealDate(feedback.getMeal().getDate())
                .foodOptionId(feedback.getFoodOption().getId())
                .foodOptionName(feedback.getFoodOption().getName())
                .userId(feedback.getUser().getId())
                .studentName(feedback.getUser().getName())
                .studentId(feedback.getUser().getStudentId())
                .rating(feedback.getRating())
                .category(feedback.getCategory())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
