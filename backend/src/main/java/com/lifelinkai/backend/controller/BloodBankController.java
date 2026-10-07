package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.BloodBankDto;
import com.lifelinkai.backend.dto.BloodInventoryDto;
import com.lifelinkai.backend.dto.BloodRequestDto;
import com.lifelinkai.backend.model.BloodGroup;
import com.lifelinkai.backend.model.BloodRequestStatus;
import com.lifelinkai.backend.service.BloodBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BloodBankController {
    private final BloodBankService service;

    @PostMapping("/blood-banks")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public BloodBankDto create(@RequestBody BloodBankDto dto) {
        return service.createBloodBank(dto);
    }

    @PutMapping("/blood-banks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BLOOD_BANK')")
    public BloodBankDto update(@PathVariable String id, @RequestBody BloodBankDto dto) {
        return service.updateBloodBank(id, dto);
    }

    @GetMapping("/blood-banks")
    public List<BloodBankDto> getAll() {
        return service.getAllBloodBanks();
    }

    @GetMapping("/blood-banks/{id}")
    public BloodBankDto getById(@PathVariable String id) {
        return service.getBloodBankById(id);
    }

    @PatchMapping("/blood-inventory/{bloodBankId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BLOOD_BANK')")
    public BloodInventoryDto updateInventory(
            @PathVariable String bloodBankId,
            @RequestParam BloodGroup bloodGroup,
            @RequestParam int units) {
        return service.updateInventory(bloodBankId, bloodGroup, units);
    }

    @GetMapping("/blood-inventory/{bloodBankId}")
    public List<BloodInventoryDto> getInventory(@PathVariable String bloodBankId) {
        return service.getInventoryByBloodBank(bloodBankId);
    }

    @PostMapping("/blood-requests")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF')")
    public BloodRequestDto createRequest(@RequestBody BloodRequestDto dto) {
        return service.createBloodRequest(dto);
    }

    @GetMapping("/blood-requests/{id}")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'BLOOD_BANK', 'ADMIN')")
    public BloodRequestDto getRequestById(@PathVariable String id) {
        return service.getBloodRequestById(id);
    }

    @GetMapping("/blood-requests")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'BLOOD_BANK', 'ADMIN')")
    public List<BloodRequestDto> getRequests(
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) String bloodBankId) {
        if (hospitalId != null) {
            return service.getRequestsByHospital(hospitalId);
        } else if (bloodBankId != null) {
            return service.getRequestsByBloodBank(bloodBankId);
        }
        throw new IllegalArgumentException("Must provide hospitalId or bloodBankId");
    }

    @PatchMapping("/blood-requests/{id}/status")
    @PreAuthorize("hasAnyRole('BLOOD_BANK')")
    public BloodRequestDto processRequest(@PathVariable String id, @RequestParam BloodRequestStatus status) {
        return service.processBloodRequest(id, status);
    }
}