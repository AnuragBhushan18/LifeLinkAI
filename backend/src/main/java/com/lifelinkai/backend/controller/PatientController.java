package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.Patient;
import com.lifelinkai.backend.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService service;

    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<Patient> create(@RequestBody Patient entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping
    @Operation(summary = "Get all Patients")
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Patient by ID")
    public ResponseEntity<Patient> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Patient")
    public ResponseEntity<Patient> update(@PathVariable String id, @RequestBody Patient entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Patient")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Patient by User ID")
    public ResponseEntity<Patient> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update Patient by User ID")
    public ResponseEntity<Patient> updateByUserId(@PathVariable String userId, @RequestBody Patient entity) {
        Patient existing = service.findByUserId(userId);
        entity.setId(existing.getId());
        entity.setUserId(userId);
        return ResponseEntity.ok(service.save(entity));
    }
}
