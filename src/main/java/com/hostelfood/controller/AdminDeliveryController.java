package com.hostelfood.controller;

import com.hostelfood.dto.delivery.DeliveryConfirmRequestDTO;
import com.hostelfood.dto.delivery.DeliveryResponseDTO;
import com.hostelfood.dto.delivery.QrValidationResponseDTO;
import com.hostelfood.dto.delivery.ScanQrRequestDTO;
import com.hostelfood.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/delivery")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/scan")
    public ResponseEntity<QrValidationResponseDTO> scanQrCode(@Valid @RequestBody ScanQrRequestDTO request) {
        return ResponseEntity.ok(deliveryService.scanAndValidateQr(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<DeliveryResponseDTO> confirmDelivery(
            @Valid @RequestBody DeliveryConfirmRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(deliveryService.confirmDelivery(request, authentication.getName()));
    }
}
