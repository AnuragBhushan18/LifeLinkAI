package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.BloodBankDto;
import com.lifelinkai.backend.dto.BloodInventoryDto;
import com.lifelinkai.backend.dto.BloodRequestDto;
import com.lifelinkai.backend.dto.MedicineDto;
import com.lifelinkai.backend.dto.PharmacyDto;
import com.lifelinkai.backend.model.BloodGroup;
import com.lifelinkai.backend.model.BloodRequestStatus;
import com.lifelinkai.backend.service.BloodBankService;
import com.lifelinkai.backend.service.PharmacyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class HealthcareResourcesTest {

    @Autowired
    private BloodBankService bloodBankService;
    
    @Autowired
    private PharmacyService pharmacyService;

    @Test
    public void testBloodBankFlow() {
        BloodBankDto bb = new BloodBankDto();
        bb.setName("Test Blood Bank");
        bb = bloodBankService.createBloodBank(bb);
        
        BloodInventoryDto inv = bloodBankService.updateInventory(bb.getId(), BloodGroup.O_PLUS, 100);
        assertEquals(100, inv.getUnitsAvailable());
        
        BloodRequestDto req = new BloodRequestDto();
        req.setHospitalId("h1");
        req.setBloodBankId(bb.getId());
        req.setBloodGroup(BloodGroup.O_PLUS);
        req.setUnitsRequested(20);
        req = bloodBankService.createBloodRequest(req);
        
        req = bloodBankService.processBloodRequest(req.getId(), BloodRequestStatus.FULFILLED);
        assertEquals(BloodRequestStatus.FULFILLED, req.getStatus());
        
        // Check deduction
        BloodInventoryDto updatedInv = bloodBankService.getInventoryByBloodBank(bb.getId()).get(0);
        assertEquals(80, updatedInv.getUnitsAvailable());
        
        // Reject negative stock
        final String bbId = bb.getId();
        assertThrows(IllegalArgumentException.class, () -> {
            bloodBankService.updateInventory(bbId, BloodGroup.A_PLUS, -10);
        });
    }

    @Test
    public void testPharmacyFlow() {
        PharmacyDto p = new PharmacyDto();
        p.setName("Test Pharmacy");
        p = pharmacyService.createPharmacy(p);
        
        MedicineDto m = new MedicineDto();
        m.setPharmacyId(p.getId());
        m.setName("Paracetamol");
        m.setQuantity(50);
        m.setUnitPrice(10.0);
        m.setLowStockThreshold(10);
        m = pharmacyService.createMedicine(m);
        
        assertEquals(50, m.getQuantity());
        
        // Low stock detection
        m.setQuantity(5);
        pharmacyService.updateMedicine(m.getId(), m);
        
        final String mId = m.getId();
        assertTrue(pharmacyService.getLowStockMedicines().stream().anyMatch(med -> med.getId().equals(mId)));
        
        // Reject negative quantity
        m.setQuantity(-5);
        final MedicineDto finalM = m;
        assertThrows(IllegalArgumentException.class, () -> pharmacyService.updateMedicine(mId, finalM));
    }
}