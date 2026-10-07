package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.MedicineDto;
import com.lifelinkai.backend.dto.PharmacyDto;
import com.lifelinkai.backend.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PharmacyController {
    private final PharmacyService service;

    @PostMapping("/pharmacies")
    @PreAuthorize("hasRole('ADMIN')")
    public PharmacyDto createPharmacy(@RequestBody PharmacyDto dto) {
        return service.createPharmacy(dto);
    }

    @GetMapping("/pharmacies")
    public List<PharmacyDto> getAllPharmacies() {
        return service.getAllPharmacies();
    }

    @GetMapping("/pharmacies/{id}")
    public PharmacyDto getPharmacyById(@PathVariable String id) {
        return service.getPharmacyById(id);
    }
    
    // Put /pharmacies/{id} could be added if needed

    @PostMapping("/medicines")
    @PreAuthorize("hasAnyRole('PHARMACY', 'ADMIN')")
    public MedicineDto createMedicine(@RequestBody MedicineDto dto) {
        return service.createMedicine(dto);
    }

    @PutMapping("/medicines/{id}")
    @PreAuthorize("hasAnyRole('PHARMACY', 'ADMIN')")
    public MedicineDto updateMedicine(@PathVariable String id, @RequestBody MedicineDto dto) {
        return service.updateMedicine(id, dto);
    }

    @DeleteMapping("/medicines/{id}")
    @PreAuthorize("hasAnyRole('PHARMACY', 'ADMIN')")
    public void deleteMedicine(@PathVariable String id) {
        service.deleteMedicine(id);
    }

    @GetMapping("/medicines/{id}")
    public MedicineDto getMedicineById(@PathVariable String id) {
        return service.getMedicineById(id);
    }

    @GetMapping("/medicines")
    public List<MedicineDto> getMedicines(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        return service.searchMedicines(search, category);
    }

    @GetMapping("/pharmacies/{pharmacyId}/medicines")
    public List<MedicineDto> getMedicinesByPharmacy(@PathVariable String pharmacyId) {
        return service.getMedicinesByPharmacy(pharmacyId);
    }

    @GetMapping("/medicines/low-stock")
    @PreAuthorize("hasAnyRole('PHARMACY', 'ADMIN')")
    public List<MedicineDto> getLowStock() {
        return service.getLowStockMedicines();
    }
}