package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.Ambulance;
import com.lifelinkai.backend.service.AmbulanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
@RequiredArgsConstructor
public class AmbulanceController {
    private final AmbulanceService service;

    @PostMapping
    @Operation(summary = "Create Ambulance")
    public ResponseEntity<Ambulance> create(@RequestBody Ambulance entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping
    @Operation(summary = "Get all Ambulances")
    public ResponseEntity<List<Ambulance>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Ambulance by ID")
    public ResponseEntity<Ambulance> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Ambulance")
    public ResponseEntity<Ambulance> update(@PathVariable String id, @RequestBody Ambulance entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    private final com.lifelinkai.backend.service.AmbulanceLocationService ambulanceLocationService;

    @PostMapping("/location")
    @Operation(summary = "Update Driver Ambulance Location")
    public ResponseEntity<com.lifelinkai.backend.dto.AmbulanceLocationResponse> updateLocation(
            @jakarta.validation.Valid @RequestBody com.lifelinkai.backend.dto.AmbulanceLocationRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.lifelinkai.backend.security.CustomUserDetails userDetails) {
        return ResponseEntity.ok(ambulanceLocationService.updateDriverLocation(userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Ambulance")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
