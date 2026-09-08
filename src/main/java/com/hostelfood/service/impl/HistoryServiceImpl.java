package com.hostelfood.service.impl;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.user.StudentHistoryItemDTO;
import com.hostelfood.entity.MealDelivery;
import com.hostelfood.entity.MealToken;
import com.hostelfood.entity.User;
import com.hostelfood.entity.Vote;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.MealTokenRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final VoteRepository voteRepository;
    private final MealTokenRepository mealTokenRepository;
    private final MealDeliveryRepository mealDeliveryRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<StudentHistoryItemDTO> getStudentHistory(String studentEmail, Pageable pageable) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with email: " + studentEmail));

        Page<Vote> votesPage = voteRepository.findByUserId(student.getId(), pageable);

        List<StudentHistoryItemDTO> items = votesPage.getContent().stream().map(vote -> {
            Optional<MealToken> tokenOpt = mealTokenRepository.findByUserIdAndMealId(student.getId(), vote.getMeal().getId());
            boolean isDelivered = false;
            java.time.LocalDateTime deliveredAt = null;

            if (tokenOpt.isPresent()) {
                Optional<MealDelivery> deliveryOpt = mealDeliveryRepository.findByMealTokenId(tokenOpt.get().getId());
                if (deliveryOpt.isPresent()) {
                    isDelivered = true;
                    deliveredAt = deliveryOpt.get().getDeliveredAt();
                }
            }

            return StudentHistoryItemDTO.builder()
                    .mealId(vote.getMeal().getId())
                    .mealDate(vote.getMeal().getDate())
                    .mealType(vote.getMeal().getMealType())
                    .foodOptionId(vote.getFoodOption().getId())
                    .foodName(vote.getFoodOption().getName())
                    .votedAt(vote.getCreatedAt())
                    .tokenStatus(tokenOpt.map(MealToken::getStatus).orElse(null))
                    .tokenCode(tokenOpt.map(MealToken::getTokenCode).orElse(null))
                    .delivered(isDelivered)
                    .deliveredAt(deliveredAt)
                    .build();
        }).collect(Collectors.toList());

        return PagedResponseDTO.<StudentHistoryItemDTO>builder()
                .content(items)
                .pageNumber(votesPage.getNumber())
                .pageSize(votesPage.getSize())
                .totalElements(votesPage.getTotalElements())
                .totalPages(votesPage.getTotalPages())
                .last(votesPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DeliveryResponseDTO> getAllDeliveries(Pageable pageable) {
        Page<MealDelivery> page = mealDeliveryRepository.findAllByOrderByDeliveredAtDesc(pageable);
        return mapToDeliveryPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DeliveryResponseDTO> getDeliveriesByMeal(Long mealId, Pageable pageable) {
        if (!mealRepository.existsById(mealId)) {
            throw new MealNotFoundException("Meal not found with id: " + mealId);
        }
        Page<MealDelivery> page = mealDeliveryRepository.findByMealId(mealId, pageable);
        return mapToDeliveryPagedResponse(page);
    }

    private PagedResponseDTO<DeliveryResponseDTO> mapToDeliveryPagedResponse(Page<MealDelivery> page) {
        List<DeliveryResponseDTO> items = page.getContent().stream().map(delivery -> DeliveryResponseDTO.builder()
                .deliveryId(delivery.getId())
                .tokenCode(delivery.getMealToken().getTokenCode())
                .studentId(delivery.getUser().getId())
                .studentName(delivery.getUser().getName())
                .studentRegistrationNumber(delivery.getUser().getStudentId())
                .hostel(delivery.getUser().getHostel())
                .roomNumber(delivery.getUser().getRoomNumber())
                .mealId(delivery.getMeal().getId())
                .mealDate(delivery.getMeal().getDate())
                .mealType(delivery.getMeal().getMealType())
                .foodOptionId(delivery.getFoodOption().getId())
                .foodName(delivery.getFoodOption().getName())
                .deliveredByAdmin(delivery.getDeliveredBy().getName())
                .deliveredAt(delivery.getDeliveredAt())
                .notes(delivery.getNotes())
                .message("Delivery record")
                .build()
        ).collect(Collectors.toList());

        return PagedResponseDTO.<DeliveryResponseDTO>builder()
                .content(items)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
