package com.hostelfood.service.impl;

import com.hostelfood.dto.delivery.DeliveryConfirmRequestDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.delivery.QrValidationResponseDTO;
import com.hostelfood.dto.delivery.ScanQrRequestDTO;
import com.hostelfood.entity.MealDelivery;
import com.hostelfood.entity.MealToken;
import com.hostelfood.entity.User;
import com.hostelfood.enums.TokenStatus;
import com.hostelfood.exception.TokenAlreadyUsedException;
import com.hostelfood.exception.TokenExpiredException;
import com.hostelfood.exception.TokenNotFoundException;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealTokenRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final MealTokenRepository mealTokenRepository;
    private final MealDeliveryRepository mealDeliveryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public QrValidationResponseDTO scanAndValidateQr(ScanQrRequestDTO request) {
        MealToken token = mealTokenRepository.findByTokenCode(request.getTokenCode())
                .orElseThrow(() -> new TokenNotFoundException("Invalid QR code: Token not found with code: " + request.getTokenCode()));

        LocalDateTime now = LocalDateTime.now();
        if (token.getStatus() == TokenStatus.ACTIVE && now.isAfter(token.getExpiresAt())) {
            token.setStatus(TokenStatus.EXPIRED);
            mealTokenRepository.save(token);
        }

        boolean isValid = token.getStatus() == TokenStatus.ACTIVE;
        String message;

        if (token.getStatus() == TokenStatus.USED) {
            message = "Meal has already been claimed.";
        } else if (token.getStatus() == TokenStatus.EXPIRED) {
            message = "Token has expired.";
        } else if (token.getStatus() == TokenStatus.CANCELLED) {
            message = "Token has been cancelled.";
        } else if (now.isBefore(token.getMeal().getDeliveryStartTime())) {
            message = "Token is valid, but delivery window has not started yet.";
        } else {
            message = "Token is valid and eligible for meal distribution.";
        }

        return QrValidationResponseDTO.builder()
                .tokenCode(token.getTokenCode())
                .valid(isValid)
                .status(token.getStatus())
                .studentId(token.getUser().getId())
                .studentName(token.getUser().getName())
                .studentRegistrationNumber(token.getUser().getStudentId())
                .hostel(token.getUser().getHostel())
                .roomNumber(token.getUser().getRoomNumber())
                .mealId(token.getMeal().getId())
                .mealDate(token.getMeal().getDate())
                .mealType(token.getMeal().getMealType())
                .foodOptionId(token.getFoodOption().getId())
                .foodName(token.getFoodOption().getName())
                .deliveryStartTime(token.getMeal().getDeliveryStartTime())
                .deliveryEndTime(token.getMeal().getDeliveryEndTime())
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public DeliveryResponseDTO confirmDelivery(DeliveryConfirmRequestDTO request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found with email: " + adminEmail));

        MealToken token = mealTokenRepository.findByTokenCode(request.getTokenCode())
                .orElseThrow(() -> new TokenNotFoundException("Token not found with code: " + request.getTokenCode()));

        if (token.getStatus() == TokenStatus.USED || mealDeliveryRepository.existsByMealTokenId(token.getId())) {
            throw new TokenAlreadyUsedException("Cannot confirm delivery: This meal token has already been redeemed.");
        }

        if (token.getStatus() == TokenStatus.CANCELLED) {
            throw new TokenAlreadyUsedException("Cannot confirm delivery: This token was cancelled.");
        }

        if (token.getStatus() == TokenStatus.EXPIRED || LocalDateTime.now().isAfter(token.getExpiresAt())) {
            token.setStatus(TokenStatus.EXPIRED);
            mealTokenRepository.save(token);
            throw new TokenExpiredException("Cannot confirm delivery: This meal token has expired.");
        }

        // Mark token USED
        token.setStatus(TokenStatus.USED);
        mealTokenRepository.save(token);

        // Record Delivery
        MealDelivery delivery = MealDelivery.builder()
                .mealToken(token)
                .user(token.getUser())
                .meal(token.getMeal())
                .foodOption(token.getFoodOption())
                .deliveredBy(admin)
                .notes(request.getNotes())
                .build();

        MealDelivery savedDelivery = mealDeliveryRepository.save(delivery);

        return DeliveryResponseDTO.builder()
                .deliveryId(savedDelivery.getId())
                .tokenCode(token.getTokenCode())
                .studentId(token.getUser().getId())
                .studentName(token.getUser().getName())
                .studentRegistrationNumber(token.getUser().getStudentId())
                .hostel(token.getUser().getHostel())
                .roomNumber(token.getUser().getRoomNumber())
                .mealId(token.getMeal().getId())
                .mealDate(token.getMeal().getDate())
                .mealType(token.getMeal().getMealType())
                .foodOptionId(token.getFoodOption().getId())
                .foodName(token.getFoodOption().getName())
                .deliveredByAdmin(admin.getName())
                .deliveredAt(savedDelivery.getDeliveredAt())
                .notes(savedDelivery.getNotes())
                .message("Meal delivered successfully!")
                .build();
    }
}
