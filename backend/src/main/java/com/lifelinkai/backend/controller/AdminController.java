package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.service.*;
import com.lifelinkai.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final HospitalService hospitalService;
    private final AmbulanceService ambulanceService;
    private final DriverService driverService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<?> getUsers() { return ResponseEntity.ok(userRepository.findAll()); }

    @GetMapping("/patients")
    @Operation(summary = "Get all patients")
    public ResponseEntity<?> getPatients() { return ResponseEntity.ok(patientService.findAll()); }

    @GetMapping("/doctors")
    @Operation(summary = "Get all doctors")
    public ResponseEntity<?> getDoctors() { return ResponseEntity.ok(doctorService.findAll()); }

    @GetMapping("/hospitals")
    @Operation(summary = "Get all hospitals")
    public ResponseEntity<?> getHospitals() { return ResponseEntity.ok(hospitalService.findAll()); }

    @GetMapping("/ambulances")
    @Operation(summary = "Get all ambulances")
    public ResponseEntity<?> getAmbulances() { return ResponseEntity.ok(ambulanceService.findAll()); }

    @GetMapping("/drivers")
    @Operation(summary = "Get all drivers")
    public ResponseEntity<?> getDrivers() { return ResponseEntity.ok(driverService.findAll()); }
}
