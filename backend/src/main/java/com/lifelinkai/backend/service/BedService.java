package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.BedCapacityDto;
import com.lifelinkai.backend.dto.BedDto;
import com.lifelinkai.backend.model.Bed;
import com.lifelinkai.backend.model.BedStatus;
import com.lifelinkai.backend.model.BedType;
import com.lifelinkai.backend.repository.BedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;

    public BedDto createBed(BedDto bedDto) {
        Optional<Bed> existingBed = bedRepository.findByHospitalIdAndBedNumber(bedDto.getHospitalId(), bedDto.getBedNumber());
        if (existingBed.isPresent()) {
            throw new IllegalArgumentException("Bed number already exists in this hospital");
        }
        
        Bed bed = new Bed();
        bed.setHospitalId(bedDto.getHospitalId());
        bed.setBedNumber(bedDto.getBedNumber());
        bed.setType(bedDto.getType() != null ? bedDto.getType() : BedType.GENERAL);
        bed.setStatus(bedDto.getStatus() != null ? bedDto.getStatus() : BedStatus.AVAILABLE);
        bed.setFloor(bedDto.getFloor());
        bed.setDepartment(bedDto.getDepartment());
        
        Bed saved = bedRepository.save(bed);
        return mapToDto(saved);
    }

    public List<BedDto> getBedsByHospital(String hospitalId) {
        return bedRepository.findByHospitalId(hospitalId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<BedDto> getBedsByHospitalAndType(String hospitalId, BedType type) {
        return bedRepository.findByHospitalIdAndType(hospitalId, type).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<BedDto> getBedsByHospitalAndStatus(String hospitalId, BedStatus status) {
        return bedRepository.findByHospitalIdAndStatus(hospitalId, status).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public BedDto getBedById(String id) {
        return mapToDto(bedRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bed not found")));
    }

    public BedCapacityDto getBedCapacity(String hospitalId) {
        BedCapacityDto capacity = new BedCapacityDto();
        capacity.setTotalBeds(bedRepository.countByHospitalId(hospitalId));
        capacity.setAvailableBeds(bedRepository.countByHospitalIdAndStatus(hospitalId, BedStatus.AVAILABLE));
        capacity.setOccupiedBeds(bedRepository.countByHospitalIdAndStatus(hospitalId, BedStatus.OCCUPIED));
        capacity.setReservedBeds(bedRepository.countByHospitalIdAndStatus(hospitalId, BedStatus.RESERVED));
        capacity.setMaintenanceBeds(bedRepository.countByHospitalIdAndStatus(hospitalId, BedStatus.MAINTENANCE));
        
        capacity.setTotalIcuBeds(bedRepository.countByHospitalIdAndType(hospitalId, BedType.ICU));
        capacity.setAvailableIcuBeds(bedRepository.countByHospitalIdAndTypeAndStatus(hospitalId, BedType.ICU, BedStatus.AVAILABLE));
        capacity.setOccupiedIcuBeds(bedRepository.countByHospitalIdAndTypeAndStatus(hospitalId, BedType.ICU, BedStatus.OCCUPIED));
        return capacity;
    }

    public BedDto updateBedStatus(String id, BedStatus status) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        bed.setStatus(status);
        return mapToDto(bedRepository.save(bed));
    }

    public BedDto reserveBed(String id) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Only available beds can be reserved");
        }
        bed.setStatus(BedStatus.RESERVED);
        return mapToDto(bedRepository.save(bed));
    }

    public BedDto occupyBed(String id) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        if (bed.getStatus() == BedStatus.OCCUPIED || bed.getStatus() == BedStatus.MAINTENANCE) {
            throw new IllegalArgumentException("Bed cannot be occupied");
        }
        bed.setStatus(BedStatus.OCCUPIED);
        return mapToDto(bedRepository.save(bed));
    }

    public BedDto releaseBed(String id) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        bed.setStatus(BedStatus.AVAILABLE);
        return mapToDto(bedRepository.save(bed));
    }
    
    public void deleteBed(String id) {
        bedRepository.deleteById(id);
    }

    private BedDto mapToDto(Bed bed) {
        BedDto dto = new BedDto();
        dto.setId(bed.getId());
        dto.setHospitalId(bed.getHospitalId());
        dto.setBedNumber(bed.getBedNumber());
        dto.setType(bed.getType());
        dto.setStatus(bed.getStatus());
        dto.setFloor(bed.getFloor());
        dto.setDepartment(bed.getDepartment());
        dto.setCreatedAt(bed.getCreatedAt());
        dto.setUpdatedAt(bed.getUpdatedAt());
        return dto;
    }
}