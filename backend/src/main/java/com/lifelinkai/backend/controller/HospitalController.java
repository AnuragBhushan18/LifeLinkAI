package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.Hospital;
import com.lifelinkai.backend.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService service;

    @PostMapping
    @Operation(summary = "Create Hospital")
    public ResponseEntity<Hospital> create(@RequestBody Hospital entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping
    @Operation(summary = "Get all Hospitals")
    public ResponseEntity<List<Hospital>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Hospital by ID")
    public ResponseEntity<Hospital> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Hospital")
    public ResponseEntity<Hospital> update(@PathVariable String id, @RequestBody Hospital entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Hospital")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Hospital by User ID")
    public ResponseEntity<Hospital> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update Hospital by User ID")
    public ResponseEntity<Hospital> updateByUserId(@PathVariable String userId, @RequestBody Hospital entity) {
        Hospital existing = service.findByUserId(userId);
        entity.setId(existing.getId());
        entity.setUserId(userId);
        return ResponseEntity.ok(service.save(entity));
    }
}
