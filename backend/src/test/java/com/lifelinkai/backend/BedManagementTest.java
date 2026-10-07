package com.lifelinkai.backend;

import com.lifelinkai.backend.dto.BedDto;
import com.lifelinkai.backend.model.BedStatus;
import com.lifelinkai.backend.model.BedType;
import com.lifelinkai.backend.service.BedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BedManagementTest {

    @Autowired
    private BedService bedService;

    @Test
    public void testBedManagementLifecycle() {
        String hospitalId = "hospital-123";
        BedDto newBed = new BedDto();
        newBed.setHospitalId(hospitalId);
        newBed.setBedNumber("B-101");
        newBed.setType(BedType.ICU);
        newBed.setFloor("1");
        newBed.setDepartment("Cardiology");
        
        BedDto created = bedService.createBed(newBed);
        assertNotNull(created.getId());
        assertEquals(BedStatus.AVAILABLE, created.getStatus());
        
        // Duplicate bed number
        BedDto duplicateBed = new BedDto();
        duplicateBed.setHospitalId(hospitalId);
        duplicateBed.setBedNumber("B-101");
        assertThrows(IllegalArgumentException.class, () -> bedService.createBed(duplicateBed));
        
        // Reserve bed
        BedDto reserved = bedService.reserveBed(created.getId());
        assertEquals(BedStatus.RESERVED, reserved.getStatus());
        
        // Prevent double reservation
        assertThrows(IllegalArgumentException.class, () -> bedService.reserveBed(created.getId()));
        
        // Occupy bed
        BedDto occupied = bedService.occupyBed(created.getId());
        assertEquals(BedStatus.OCCUPIED, occupied.getStatus());
        
        // Release bed
        BedDto released = bedService.releaseBed(created.getId());
        assertEquals(BedStatus.AVAILABLE, released.getStatus());
    }
}