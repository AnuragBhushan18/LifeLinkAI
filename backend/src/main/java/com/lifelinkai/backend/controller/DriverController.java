package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.Driver;
import com.lifelinkai.backend.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService service;

    @PostMapping
    @Operation(summary = "Create Driver")
    public ResponseEntity<Driver> create(@RequestBody Driver entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @GetMapping
    @Operation(summary = "Get all Drivers")
    public ResponseEntity<List<Driver>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Driver by ID")
    public ResponseEntity<Driver> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Driver")
    public ResponseEntity<Driver> update(@PathVariable String id, @RequestBody Driver entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Driver")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Driver by User ID")
    public ResponseEntity<Driver> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update Driver by User ID")
    public ResponseEntity<Driver> updateByUserId(@PathVariable String userId, @RequestBody Driver entity) {
        Driver existing = service.findByUserId(userId);
        entity.setId(existing.getId());
        entity.setUserId(userId);
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/user/{userId}/status")
    @Operation(summary = "Update Driver Status by User ID")
    public ResponseEntity<Driver> updateStatusByUserId(@PathVariable String userId, @RequestBody java.util.Map<String, String> payload) {
        Driver existing = service.findByUserId(userId);
        if (payload.containsKey("status")) {
            existing.setAvailability(com.lifelinkai.backend.model.Availability.valueOf(payload.get("status")));
        }
        return ResponseEntity.ok(service.save(existing));
    }
}
