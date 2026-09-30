package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.Doctor;
import com.lifelinkai.backend.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService service;

    @PostMapping
    @Operation(summary = "Create Doctor")
    public ResponseEntity<Doctor> create(@RequestBody Doctor entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping
    @Operation(summary = "Get all Doctors")
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Doctor by ID")
    public ResponseEntity<Doctor> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Doctor")
    public ResponseEntity<Doctor> update(@PathVariable String id, @RequestBody Doctor entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Doctor")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Doctor by User ID")
    public ResponseEntity<Doctor> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update Doctor by User ID")
    public ResponseEntity<Doctor> updateByUserId(@PathVariable String userId, @RequestBody Doctor entity) {
        Doctor existing = service.findByUserId(userId);
        entity.setId(existing.getId());
        entity.setUserId(userId);
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/user/{userId}/status")
    @Operation(summary = "Update Doctor Status by User ID")
    public ResponseEntity<Doctor> updateStatusByUserId(@PathVariable String userId, @RequestBody java.util.Map<String, String> payload) {
        Doctor existing = service.findByUserId(userId);
        if (payload.containsKey("status")) {
            existing.setAvailability(com.lifelinkai.backend.model.Availability.valueOf(payload.get("status")));
        }
        return ResponseEntity.ok(service.save(existing));
    }
}
