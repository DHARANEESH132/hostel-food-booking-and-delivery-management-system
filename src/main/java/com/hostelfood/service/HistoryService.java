package com.hostelfood.service;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.user.StudentHistoryItemDTO;
import org.springframework.data.domain.Pageable;

public interface HistoryService {

    PagedResponseDTO<StudentHistoryItemDTO> getStudentHistory(String studentEmail, Pageable pageable);

    PagedResponseDTO<DeliveryResponseDTO> getAllDeliveries(Pageable pageable);

    PagedResponseDTO<DeliveryResponseDTO> getDeliveriesByMeal(Long mealId, Pageable pageable);
}
