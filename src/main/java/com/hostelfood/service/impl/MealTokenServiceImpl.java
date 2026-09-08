package com.hostelfood.service.impl;

import com.hostelfood.dto.qr.MealTokenResponseDTO;
import com.hostelfood.entity.Meal;
import com.hostelfood.entity.MealToken;
import com.hostelfood.entity.User;
import com.hostelfood.entity.Vote;
import com.hostelfood.enums.TokenStatus;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.exception.NoVoteFoundForTokenException;
import com.hostelfood.exception.TokenExpiredException;
import com.hostelfood.exception.TokenNotFoundException;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.MealTokenRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.MealTokenService;
import com.hostelfood.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealTokenServiceImpl implements MealTokenService {

    private final MealTokenRepository mealTokenRepository;
    private final MealRepository mealRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MealTokenResponseDTO generateOrGetToken(Long mealId, String studentEmail) {
        User user = getUserByEmail(studentEmail);
        Meal meal = getMealById(mealId);

        Optional<MealToken> existingToken = mealTokenRepository.findByUserIdAndMealId(user.getId(), meal.getId());
        if (existingToken.isPresent()) {
            MealToken token = existingToken.get();
            checkAndExpireToken(token);
            return mapToDTO(token);
        }

        Vote vote = voteRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new NoVoteFoundForTokenException(
                        "Cannot generate meal token: No confirmed vote found for meal id: " + mealId));

        if (LocalDateTime.now().isAfter(meal.getDeliveryEndTime())) {
            throw new TokenExpiredException("Cannot generate meal token: Delivery window has ended for meal id: " + mealId);
        }

        String tokenCode = "MT-" + UUID.randomUUID();
        String qrBase64 = QrCodeGenerator.generateQrCodeBase64(tokenCode, 250, 250);

        MealToken newToken = MealToken.builder()
                .tokenCode(tokenCode)
                .user(user)
                .meal(meal)
                .foodOption(vote.getFoodOption())
                .qrCode(qrBase64)
                .status(TokenStatus.ACTIVE)
                .expiresAt(meal.getDeliveryEndTime())
                .build();

        MealToken savedToken = mealTokenRepository.save(newToken);
        return mapToDTO(savedToken);
    }

    @Override
    @Transactional(readOnly = true)
    public MealTokenResponseDTO getStudentMealToken(Long mealId, String studentEmail) {
        User user = getUserByEmail(studentEmail);
        Meal meal = getMealById(mealId);

        MealToken token = mealTokenRepository.findByUserIdAndMealId(user.getId(), meal.getId())
                .orElseThrow(() -> new TokenNotFoundException("No meal token found for meal id: " + mealId));

        checkAndExpireToken(token);
        return mapToDTO(token);
    }

    private void checkAndExpireToken(MealToken token) {
        if (token.getStatus() == TokenStatus.ACTIVE && LocalDateTime.now().isAfter(token.getExpiresAt())) {
            token.setStatus(TokenStatus.EXPIRED);
            mealTokenRepository.save(token);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private Meal getMealById(Long mealId) {
        return mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + mealId));
    }

    private MealTokenResponseDTO mapToDTO(MealToken token) {
        return MealTokenResponseDTO.builder()
                .id(token.getId())
                .tokenCode(token.getTokenCode())
                .qrCodeBase64(token.getQrCode())
                .mealId(token.getMeal().getId())
                .mealDate(token.getMeal().getDate())
                .mealType(token.getMeal().getMealType())
                .foodOptionId(token.getFoodOption().getId())
                .foodName(token.getFoodOption().getName())
                .studentId(token.getUser().getId())
                .studentName(token.getUser().getName())
                .studentRegistrationNumber(token.getUser().getStudentId())
                .hostel(token.getUser().getHostel())
                .roomNumber(token.getUser().getRoomNumber())
                .status(token.getStatus())
                .expiresAt(token.getExpiresAt())
                .createdAt(token.getCreatedAt())
                .build();
    }
}
