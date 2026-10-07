package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.MedicineDto;
import com.lifelinkai.backend.dto.PharmacyDto;
import com.lifelinkai.backend.model.Medicine;
import com.lifelinkai.backend.model.OperationalStatus;
import com.lifelinkai.backend.model.Pharmacy;
import com.lifelinkai.backend.repository.MedicineRepository;
import com.lifelinkai.backend.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    public PharmacyDto createPharmacy(PharmacyDto dto) {
        Pharmacy p = new Pharmacy();
        p.setName(dto.getName());
        p.setPhone(dto.getPhone());
        p.setEmail(dto.getEmail());
        p.setAddress(dto.getAddress());
        p.setCity(dto.getCity());
        p.setOperationalStatus(dto.getOperationalStatus() != null ? dto.getOperationalStatus() : OperationalStatus.ACTIVE);
        return mapToDto(pharmacyRepository.save(p));
    }

    public PharmacyDto getPharmacyById(String id) {
        return mapToDto(pharmacyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pharmacy not found")));
    }

    public List<PharmacyDto> getAllPharmacies() {
        return pharmacyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public MedicineDto createMedicine(MedicineDto dto) {
        if (dto.getQuantity() < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (dto.getUnitPrice() < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        Medicine m = new Medicine();
        m.setPharmacyId(dto.getPharmacyId());
        m.setName(dto.getName());
        m.setCategory(dto.getCategory());
        m.setQuantity(dto.getQuantity());
        m.setUnitPrice(dto.getUnitPrice());
        m.setExpiryDate(dto.getExpiryDate());
        m.setDescription(dto.getDescription());
        m.setLowStockThreshold(dto.getLowStockThreshold() > 0 ? dto.getLowStockThreshold() : 10);
        return mapToDto(medicineRepository.save(m));
    }

    public MedicineDto updateMedicine(String id, MedicineDto dto) {
        if (dto.getQuantity() < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        Medicine m = medicineRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        m.setName(dto.getName());
        m.setCategory(dto.getCategory());
        m.setQuantity(dto.getQuantity());
        m.setUnitPrice(dto.getUnitPrice());
        m.setExpiryDate(dto.getExpiryDate());
        m.setDescription(dto.getDescription());
        m.setLowStockThreshold(dto.getLowStockThreshold());
        return mapToDto(medicineRepository.save(m));
    }

    public void deleteMedicine(String id) {
        medicineRepository.deleteById(id);
    }

    public MedicineDto getMedicineById(String id) {
        return mapToDto(medicineRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Medicine not found")));
    }

    public List<MedicineDto> getMedicinesByPharmacy(String pharmacyId) {
        return medicineRepository.findByPharmacyId(pharmacyId).stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public List<MedicineDto> searchMedicines(String name, String category) {
        List<Medicine> meds;
        if (name != null && !name.trim().isEmpty()) {
            meds = medicineRepository.findByNameContainingIgnoreCase(name);
        } else if (category != null && !category.trim().isEmpty()) {
            meds = medicineRepository.findByCategoryIgnoreCase(category);
        } else {
            meds = medicineRepository.findAll();
        }
        
        // Filter out expired medicines for user search
        return meds.stream()
            .filter(m -> m.getExpiryDate() == null || m.getExpiryDate().isAfter(LocalDateTime.now()))
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public List<MedicineDto> getLowStockMedicines() {
        return medicineRepository.findAll().stream()
            .filter(m -> m.getQuantity() <= m.getLowStockThreshold())
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private PharmacyDto mapToDto(Pharmacy p) {
        PharmacyDto dto = new PharmacyDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPhone(p.getPhone());
        dto.setEmail(p.getEmail());
        dto.setAddress(p.getAddress());
        dto.setCity(p.getCity());
        dto.setOperationalStatus(p.getOperationalStatus());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private MedicineDto mapToDto(Medicine m) {
        MedicineDto dto = new MedicineDto();
        dto.setId(m.getId());
        dto.setPharmacyId(m.getPharmacyId());
        dto.setName(m.getName());
        dto.setCategory(m.getCategory());
        dto.setQuantity(m.getQuantity());
        dto.setUnitPrice(m.getUnitPrice());
        dto.setExpiryDate(m.getExpiryDate());
        dto.setDescription(m.getDescription());
        dto.setLowStockThreshold(m.getLowStockThreshold());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }
}