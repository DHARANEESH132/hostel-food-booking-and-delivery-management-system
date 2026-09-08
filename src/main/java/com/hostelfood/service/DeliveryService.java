package com.hostelfood.service;

import com.hostelfood.dto.delivery.DeliveryConfirmRequestDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.delivery.QrValidationResponseDTO;
import com.hostelfood.dto.delivery.ScanQrRequestDTO;

public interface DeliveryService {

    QrValidationResponseDTO scanAndValidateQr(ScanQrRequestDTO request);

    DeliveryResponseDTO confirmDelivery(DeliveryConfirmRequestDTO request, String adminEmail);
}
