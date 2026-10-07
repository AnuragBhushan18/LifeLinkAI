package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.BloodBankDto;
import com.lifelinkai.backend.dto.BloodInventoryDto;
import com.lifelinkai.backend.dto.BloodRequestDto;
import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.BloodBankRepository;
import com.lifelinkai.backend.repository.BloodInventoryRepository;
import com.lifelinkai.backend.repository.BloodRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BloodBankService {
    private final BloodBankRepository bloodBankRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final BloodRequestRepository requestRepository;

    public BloodBankDto createBloodBank(BloodBankDto dto) {
        BloodBank bb = new BloodBank();
        bb.setName(dto.getName());
        bb.setPhone(dto.getPhone());
        bb.setEmail(dto.getEmail());
        bb.setAddress(dto.getAddress());
        bb.setCity(dto.getCity());
        bb.setLatitude(dto.getLatitude());
        bb.setLongitude(dto.getLongitude());
        bb.setOperationalStatus(dto.getOperationalStatus() != null ? dto.getOperationalStatus() : OperationalStatus.ACTIVE);
        return mapBbToDto(bloodBankRepository.save(bb));
    }
    
    public BloodBankDto updateBloodBank(String id, BloodBankDto dto) {
        BloodBank bb = bloodBankRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blood bank not found"));
        bb.setName(dto.getName());
        bb.setPhone(dto.getPhone());
        bb.setEmail(dto.getEmail());
        bb.setAddress(dto.getAddress());
        bb.setCity(dto.getCity());
        bb.setLatitude(dto.getLatitude());
        bb.setLongitude(dto.getLongitude());
        bb.setOperationalStatus(dto.getOperationalStatus());
        return mapBbToDto(bloodBankRepository.save(bb));
    }

    public BloodBankDto getBloodBankById(String id) {
        return mapBbToDto(bloodBankRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blood bank not found")));
    }

    public List<BloodBankDto> getAllBloodBanks() {
        return bloodBankRepository.findAll().stream().map(this::mapBbToDto).collect(Collectors.toList());
    }

    public BloodInventoryDto updateInventory(String bloodBankId, BloodGroup group, int units) {
        if (units < 0) throw new IllegalArgumentException("Units cannot be negative");
        Optional<BloodInventory> invOpt = inventoryRepository.findByBloodBankIdAndBloodGroup(bloodBankId, group);
        BloodInventory inv = invOpt.orElseGet(() -> {
            BloodInventory newInv = new BloodInventory();
            newInv.setBloodBankId(bloodBankId);
            newInv.setBloodGroup(group);
            return newInv;
        });
        inv.setUnitsAvailable(units);
        return mapInvToDto(inventoryRepository.save(inv));
    }

    public List<BloodInventoryDto> getInventoryByBloodBank(String bloodBankId) {
        return inventoryRepository.findByBloodBankId(bloodBankId).stream().map(this::mapInvToDto).collect(Collectors.toList());
    }

    public BloodRequestDto createBloodRequest(BloodRequestDto dto) {
        if (dto.getUnitsRequested() <= 0) throw new IllegalArgumentException("Units requested must be positive");
        BloodRequest req = new BloodRequest();
        req.setHospitalId(dto.getHospitalId());
        req.setBloodBankId(dto.getBloodBankId());
        req.setEmergencyId(dto.getEmergencyId());
        req.setRequestedBy(dto.getRequestedBy());
        req.setBloodGroup(dto.getBloodGroup());
        req.setUnitsRequested(dto.getUnitsRequested());
        req.setPriority(dto.getPriority() != null ? dto.getPriority() : RequestPriority.NORMAL);
        req.setStatus(BloodRequestStatus.PENDING);
        return mapReqToDto(requestRepository.save(req));
    }

    public BloodRequestDto getBloodRequestById(String id) {
        return mapReqToDto(requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Blood request not found")));
    }
    
    public List<BloodRequestDto> getRequestsByHospital(String hospitalId) {
        return requestRepository.findByHospitalId(hospitalId).stream().map(this::mapReqToDto).collect(Collectors.toList());
    }

    public List<BloodRequestDto> getRequestsByBloodBank(String bloodBankId) {
        return requestRepository.findByBloodBankId(bloodBankId).stream().map(this::mapReqToDto).collect(Collectors.toList());
    }

    public BloodRequestDto processBloodRequest(String id, BloodRequestStatus status) {
        BloodRequest req = requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (req.getStatus() == BloodRequestStatus.FULFILLED || req.getStatus() == BloodRequestStatus.REJECTED || req.getStatus() == BloodRequestStatus.CANCELLED) {
            throw new IllegalArgumentException("Request is already in a terminal state");
        }
        
        if (status == BloodRequestStatus.FULFILLED || status == BloodRequestStatus.PARTIALLY_FULFILLED) {
            // Check inventory
            BloodInventory inv = inventoryRepository.findByBloodBankIdAndBloodGroup(req.getBloodBankId(), req.getBloodGroup())
                    .orElseThrow(() -> new IllegalArgumentException("No inventory found for this blood group"));
            
            if (status == BloodRequestStatus.FULFILLED) {
                if (inv.getUnitsAvailable() < req.getUnitsRequested()) {
                    throw new IllegalArgumentException("Not enough units available for full fulfillment");
                }
                inv.setUnitsAvailable(inv.getUnitsAvailable() - req.getUnitsRequested());
            } else {
                // PARTIALLY_FULFILLED -> let's say they provide whatever is available up to requested.
                // Normally you'd want to specify how many units were provided. For simplicity, we just deduct what we have or requested.
                // In a real system, there would be a "unitsProvided" field.
                int toDeduct = Math.min(inv.getUnitsAvailable(), req.getUnitsRequested());
                inv.setUnitsAvailable(inv.getUnitsAvailable() - toDeduct);
            }
            inventoryRepository.save(inv);
        }
        
        req.setStatus(status);
        return mapReqToDto(requestRepository.save(req));
    }

    private BloodBankDto mapBbToDto(BloodBank bb) {
        BloodBankDto dto = new BloodBankDto();
        dto.setId(bb.getId());
        dto.setName(bb.getName());
        dto.setPhone(bb.getPhone());
        dto.setEmail(bb.getEmail());
        dto.setAddress(bb.getAddress());
        dto.setCity(bb.getCity());
        dto.setLatitude(bb.getLatitude());
        dto.setLongitude(bb.getLongitude());
        dto.setOperationalStatus(bb.getOperationalStatus());
        dto.setCreatedAt(bb.getCreatedAt());
        dto.setUpdatedAt(bb.getUpdatedAt());
        return dto;
    }

    private BloodInventoryDto mapInvToDto(BloodInventory inv) {
        BloodInventoryDto dto = new BloodInventoryDto();
        dto.setId(inv.getId());
        dto.setBloodBankId(inv.getBloodBankId());
        dto.setBloodGroup(inv.getBloodGroup());
        dto.setUnitsAvailable(inv.getUnitsAvailable());
        dto.setUpdatedAt(inv.getUpdatedAt());
        return dto;
    }

    private BloodRequestDto mapReqToDto(BloodRequest req) {
        BloodRequestDto dto = new BloodRequestDto();
        dto.setId(req.getId());
        dto.setHospitalId(req.getHospitalId());
        dto.setBloodBankId(req.getBloodBankId());
        dto.setEmergencyId(req.getEmergencyId());
        dto.setRequestedBy(req.getRequestedBy());
        dto.setBloodGroup(req.getBloodGroup());
        dto.setUnitsRequested(req.getUnitsRequested());
        dto.setPriority(req.getPriority());
        dto.setStatus(req.getStatus());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setUpdatedAt(req.getUpdatedAt());
        return dto;
    }
}