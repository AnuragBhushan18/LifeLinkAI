package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.BedCapacityDto;
import com.lifelinkai.backend.dto.BedDto;
import com.lifelinkai.backend.model.BedStatus;
import com.lifelinkai.backend.model.BedType;
import com.lifelinkai.backend.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BedController {
    private final BedService bedService;

    @PostMapping("/beds")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto createBed(@RequestBody BedDto bedDto) {
        return bedService.createBed(bedDto);
    }

    @GetMapping("/beds")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public List<BedDto> getBeds(
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) BedType type,
            @RequestParam(required = false) BedStatus status) {
        
        if (hospitalId != null && type != null) {
            return bedService.getBedsByHospitalAndType(hospitalId, type);
        } else if (hospitalId != null && status != null) {
            return bedService.getBedsByHospitalAndStatus(hospitalId, status);
        } else if (hospitalId != null) {
            return bedService.getBedsByHospital(hospitalId);
        }
        throw new IllegalArgumentException("hospitalId is required");
    }

    @GetMapping("/beds/{id}")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto getBedById(@PathVariable String id) {
        return bedService.getBedById(id);
    }

    @PatchMapping("/beds/{id}/status")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto updateBedStatus(@PathVariable String id, @RequestParam BedStatus status) {
        return bedService.updateBedStatus(id, status);
    }

    @PostMapping("/beds/{id}/reserve")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto reserveBed(@PathVariable String id) {
        return bedService.reserveBed(id);
    }

    @PostMapping("/beds/{id}/occupy")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto occupyBed(@PathVariable String id) {
        return bedService.occupyBed(id);
    }

    @PostMapping("/beds/{id}/release")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'ADMIN')")
    public BedDto releaseBed(@PathVariable String id) {
        return bedService.releaseBed(id);
    }

    @DeleteMapping("/beds/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void deleteBed(@PathVariable String id) {
        bedService.deleteBed(id);
    }

    @GetMapping("/hospitals/{id}/capacity")
    public BedCapacityDto getHospitalCapacity(@PathVariable String id) {
        return bedService.getBedCapacity(id);
    }
}