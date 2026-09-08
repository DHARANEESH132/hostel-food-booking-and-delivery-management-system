package com.hostelfood.controller;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminHistoryController {

    private final HistoryService historyService;

    @GetMapping("/deliveries")
    public ResponseEntity<PagedResponseDTO<DeliveryResponseDTO>> getAllDeliveries(
            @PageableDefault(size = 15, sort = "deliveredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(historyService.getAllDeliveries(pageable));
    }

    @GetMapping("/meals/{mealId}/deliveries")
    public ResponseEntity<PagedResponseDTO<DeliveryResponseDTO>> getDeliveriesForMeal(
            @PathVariable Long mealId,
            @PageableDefault(size = 15, sort = "deliveredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(historyService.getDeliveriesByMeal(mealId, pageable));
    }
}
