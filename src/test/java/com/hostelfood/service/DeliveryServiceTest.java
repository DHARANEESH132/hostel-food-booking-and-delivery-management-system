package com.hostelfood.service;

import com.hostelfood.dto.delivery.DeliveryConfirmRequestDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.delivery.QrValidationResponseDTO;
import com.hostelfood.dto.delivery.ScanQrRequestDTO;
import com.hostelfood.entity.*;
import com.hostelfood.enums.MealType;
import com.hostelfood.enums.Role;
import com.hostelfood.enums.TokenStatus;
import com.hostelfood.exception.TokenAlreadyUsedException;
import com.hostelfood.exception.TokenExpiredException;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealTokenRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.service.impl.DeliveryServiceImpl;
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
class DeliveryServiceTest {

    @Mock
    private MealTokenRepository mealTokenRepository;

    @Mock
    private MealDeliveryRepository mealDeliveryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private User admin;
    private User student;
    private Meal meal;
    private FoodOption foodOption;
    private MealToken activeToken;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).name("Admin User").email("admin@hostel.com").role(Role.ADMIN).build();
        student = User.builder().id(2L).name("Student User").email("student@test.com").role(Role.STUDENT).studentId("ST01").build();

        meal = Meal.builder()
                .id(10L)
                .date(LocalDate.now())
                .mealType(MealType.DINNER)
                .deliveryStartTime(LocalDateTime.now().minusHours(1))
                .deliveryEndTime(LocalDateTime.now().plusHours(2))
                .build();

        foodOption = FoodOption.builder().id(20L).name("Fried Rice").meal(meal).build();

        activeToken = MealToken.builder()
                .id(300L)
                .tokenCode("MT-TEST-123")
                .user(student)
                .meal(meal)
                .foodOption(foodOption)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .build();
    }

    @Test
    @DisplayName("Should validate active QR code successfully")
    void testScanAndValidateQr_Active() {
        ScanQrRequestDTO request = ScanQrRequestDTO.builder().tokenCode("MT-TEST-123").build();
        when(mealTokenRepository.findByTokenCode("MT-TEST-123")).thenReturn(Optional.of(activeToken));

        QrValidationResponseDTO response = deliveryService.scanAndValidateQr(request);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(TokenStatus.ACTIVE, response.getStatus());
        assertEquals("Fried Rice", response.getFoodName());
        assertEquals("Student User", response.getStudentName());
    }

    @Test
    @DisplayName("Should confirm meal delivery and transition token status to USED")
    void testConfirmDelivery_Success() {
        DeliveryConfirmRequestDTO request = DeliveryConfirmRequestDTO.builder()
                .tokenCode("MT-TEST-123")
                .notes("Handed over meal plate")
                .build();

        when(userRepository.findByEmail("admin@hostel.com")).thenReturn(Optional.of(admin));
        when(mealTokenRepository.findByTokenCode("MT-TEST-123")).thenReturn(Optional.of(activeToken));
        when(mealDeliveryRepository.existsByMealTokenId(300L)).thenReturn(false);

        MealDelivery savedDelivery = MealDelivery.builder()
                .id(500L)
                .mealToken(activeToken)
                .user(student)
                .meal(meal)
                .foodOption(foodOption)
                .deliveredBy(admin)
                .deliveredAt(LocalDateTime.now())
                .notes("Handed over meal plate")
                .build();

        when(mealDeliveryRepository.save(any(MealDelivery.class))).thenReturn(savedDelivery);

        DeliveryResponseDTO response = deliveryService.confirmDelivery(request, "admin@hostel.com");

        assertNotNull(response);
        assertEquals(TokenStatus.USED, activeToken.getStatus());
        assertEquals("Fried Rice", response.getFoodName());
        assertEquals("Admin User", response.getDeliveredByAdmin());
        verify(mealTokenRepository, times(1)).save(activeToken);
        verify(mealDeliveryRepository, times(1)).save(any(MealDelivery.class));
    }

    @Test
    @DisplayName("Should throw TokenAlreadyUsedException when token is already USED")
    void testConfirmDelivery_AlreadyUsed() {
        activeToken.setStatus(TokenStatus.USED);

        DeliveryConfirmRequestDTO request = DeliveryConfirmRequestDTO.builder()
                .tokenCode("MT-TEST-123")
                .build();

        when(userRepository.findByEmail("admin@hostel.com")).thenReturn(Optional.of(admin));
        when(mealTokenRepository.findByTokenCode("MT-TEST-123")).thenReturn(Optional.of(activeToken));

        assertThrows(TokenAlreadyUsedException.class, () -> deliveryService.confirmDelivery(request, "admin@hostel.com"));
        verify(mealDeliveryRepository, never()).save(any(MealDelivery.class));
    }

    @Test
    @DisplayName("Should throw TokenExpiredException when delivery deadline has passed")
    void testConfirmDelivery_Expired() {
        activeToken.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        DeliveryConfirmRequestDTO request = DeliveryConfirmRequestDTO.builder()
                .tokenCode("MT-TEST-123")
                .build();

        when(userRepository.findByEmail("admin@hostel.com")).thenReturn(Optional.of(admin));
        when(mealTokenRepository.findByTokenCode("MT-TEST-123")).thenReturn(Optional.of(activeToken));

        assertThrows(TokenExpiredException.class, () -> deliveryService.confirmDelivery(request, "admin@hostel.com"));
        assertEquals(TokenStatus.EXPIRED, activeToken.getStatus());
    }
}
