package com.hostelfood.service.impl;

import com.hostelfood.dto.report.DateRangeReportDTO;
import com.hostelfood.dto.report.MealConsumptionReportDTO;
import com.hostelfood.dto.report.OptionConsumptionDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.FoodOptionRepository;
import com.hostelfood.repository.MealDeliveryRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final MealRepository mealRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final VoteRepository voteRepository;
    private final MealDeliveryRepository mealDeliveryRepository;

    @Override
    @Transactional(readOnly = true)
    public MealConsumptionReportDTO getMealConsumptionReport(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + mealId));

        return buildMealConsumptionReport(meal);
    }

    @Override
    @Transactional(readOnly = true)
    public DateRangeReportDTO getDateRangeReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();

        List<Meal> meals = mealRepository.findByDateBetweenOrderByDateAscVotingStartTimeAsc(startDate, endDate);

        long totalVotes = 0;
        long totalDelivered = 0;
        List<MealConsumptionReportDTO> mealReports = new ArrayList<>();

        for (Meal meal : meals) {
            MealConsumptionReportDTO report = buildMealConsumptionReport(meal);
            totalVotes += report.getTotalVoted();
            totalDelivered += report.getTotalDelivered();
            mealReports.add(report);
        }

        long totalUnclaimed = totalVotes - totalDelivered;
        double overallRate = 0.0;
        if (totalVotes > 0) {
            overallRate = BigDecimal.valueOf((totalDelivered * 100.0) / totalVotes)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return DateRangeReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalMeals(meals.size())
                .totalVotes(totalVotes)
                .totalDelivered(totalDelivered)
                .totalUnclaimed(totalUnclaimed)
                .overallConsumptionRatePercentage(overallRate)
                .mealReports(mealReports)
                .build();
    }

    private MealConsumptionReportDTO buildMealConsumptionReport(Meal meal) {
        long totalVoted = voteRepository.countByMealId(meal.getId());
        long totalDelivered = mealDeliveryRepository.countByMealId(meal.getId());
        long unclaimed = Math.max(0, totalVoted - totalDelivered);

        double rate = 0.0;
        if (totalVoted > 0) {
            rate = BigDecimal.valueOf((totalDelivered * 100.0) / totalVoted)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<FoodOption> options = foodOptionRepository.findByMealId(meal.getId());
        List<OptionConsumptionDTO> optionBreakdowns = new ArrayList<>();

        for (FoodOption option : options) {
            long optionVoted = voteRepository.countByMealIdAndFoodOptionId(meal.getId(), option.getId());
            long optionDelivered = mealDeliveryRepository.countByMealIdAndFoodOptionId(meal.getId(), option.getId());
            long optionUnclaimed = Math.max(0, optionVoted - optionDelivered);
            double wastePercent = 0.0;
            if (optionVoted > 0) {
                wastePercent = BigDecimal.valueOf((optionUnclaimed * 100.0) / optionVoted)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            optionBreakdowns.add(OptionConsumptionDTO.builder()
                    .foodOptionId(option.getId())
                    .foodName(option.getName())
                    .votedCount(optionVoted)
                    .deliveredCount(optionDelivered)
                    .unclaimedCount(optionUnclaimed)
                    .wastePercentage(wastePercent)
                    .build());
        }

        return MealConsumptionReportDTO.builder()
                .mealId(meal.getId())
                .mealDate(meal.getDate())
                .mealType(meal.getMealType())
                .totalVoted(totalVoted)
                .totalDelivered(totalDelivered)
                .unclaimedMeals(unclaimed)
                .consumptionRatePercentage(rate)
                .optionBreakdowns(optionBreakdowns)
                .build();
    }
}
