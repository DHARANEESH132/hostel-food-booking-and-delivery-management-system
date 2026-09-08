package com.hostelfood.service.impl;

import com.hostelfood.dto.dashboard.AdminDashboardDTO;
import com.hostelfood.dto.dashboard.StudentDashboardDTO;
import com.hostelfood.dto.meal.MealResponseDTO;
import com.hostelfood.entity.User;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.Role;
import com.hostelfood.enums.TokenStatus;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.MealTokenRepository;
import com.hostelfood.repository.UserRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.DashboardService;
import com.hostelfood.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final VoteRepository voteRepository;
    private final MealTokenRepository mealTokenRepository;
    private final MealDeliveryRepository mealDeliveryRepository;
    private final MealService mealService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboard() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalMeals = mealRepository.count();
        long todayMeals = mealRepository.countByDate(LocalDate.now());
        long totalVotes = voteRepository.count();
        long totalDelivered = mealDeliveryRepository.count();
        long pendingDeliveries = mealTokenRepository.countByStatus(TokenStatus.ACTIVE);

        List<MealResponseDTO> allMeals = mealService.getAllMeals();
        List<MealResponseDTO> activeVoting = allMeals.stream()
                .filter(m -> m.getStatus() == MealStatus.VOTING_OPEN)
                .collect(Collectors.toList());

        List<MealResponseDTO> activeDelivery = allMeals.stream()
                .filter(m -> m.getStatus() == MealStatus.DELIVERY_OPEN)
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalStudents(totalStudents)
                .totalMealsScheduled(totalMeals)
                .todayMealsCount(todayMeals)
                .totalVotesCast(totalVotes)
                .totalMealsDelivered(totalDelivered)
                .pendingDeliveriesCount(pendingDeliveries)
                .activeVotingMeals(activeVoting)
                .activeDeliveryMeals(activeDelivery)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardDTO getStudentDashboard(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with email: " + studentEmail));

        long totalVotes = voteRepository.countByUserId(student.getId());
        long totalDelivered = mealDeliveryRepository.countByUserId(student.getId());
        long activeTokens = mealTokenRepository.countByUserIdAndStatus(student.getId(), TokenStatus.ACTIVE);
        long todayMeals = mealRepository.countByDate(LocalDate.now());

        return StudentDashboardDTO.builder()
                .studentName(student.getName())
                .studentRegistrationNumber(student.getStudentId())
                .email(student.getEmail())
                .hostel(student.getHostel())
                .roomNumber(student.getRoomNumber())
                .totalVotesCast(totalVotes)
                .totalMealsReceived(totalDelivered)
                .activeTokensCount(activeTokens)
                .todayMealsCount(todayMeals)
                .build();
    }
}
