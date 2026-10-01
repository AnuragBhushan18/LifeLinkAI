import os

base_path = r"c:\Users\Anurag_Bhushan\Desktop\LifeLinkAI\backend\src\main\java\com\lifelinkai\backend"

def create_file(subpath, content):
    full_path = os.path.join(base_path, subpath)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

entities = ['Patient', 'Doctor', 'Hospital', 'Ambulance', 'Driver']

# Services
for e in entities:
    e_low = e.lower()
    create_file(f'service/{e}Service.java', f'''package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.{e};
import com.lifelinkai.backend.repository.{e}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class {e}Service {{
    private final {e}Repository repository;

    public {e} save({e} entity) {{ return repository.save(entity); }}
    public List<{e}> findAll() {{ return repository.findAll(); }}
    public {e} findById(String id) {{ return repository.findById(id).orElseThrow(() -> new RuntimeException("{e} not found")); }}
    public void deleteById(String id) {{ repository.deleteById(id); }}
}}''')

# Controllers
for e in entities:
    e_low = e.lower()
    create_file(f'controller/{e}Controller.java', f'''package com.lifelinkai.backend.controller;
import com.lifelinkai.backend.model.{e};
import com.lifelinkai.backend.service.{e}Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/{e_low}s")
@RequiredArgsConstructor
public class {e}Controller {{
    private final {e}Service service;

    @PostMapping
    @Operation(summary = "Create {e}")
    public ResponseEntity<{e}> create(@RequestBody {e} entity) {{
        return ResponseEntity.ok(service.save(entity));
    }}

    @GetMapping
    @Operation(summary = "Get all {e}s")
    public ResponseEntity<List<{e}>> getAll() {{
        return ResponseEntity.ok(service.findAll());
    }}

    @GetMapping("/{{id}}")
    @Operation(summary = "Get {e} by ID")
    public ResponseEntity<{e}> getById(@PathVariable String id) {{
        return ResponseEntity.ok(service.findById(id));
    }}

    @PutMapping("/{{id}}")
    @Operation(summary = "Update {e}")
    public ResponseEntity<{e}> update(@PathVariable String id, @RequestBody {e} entity) {{
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }}

    @DeleteMapping("/{{id}}")
    @Operation(summary = "Delete {e}")
    public ResponseEntity<Void> delete(@PathVariable String id) {{
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }}
}}''')

# Admin Controller
create_file('controller/AdminController.java', '''package com.lifelinkai.backend.controller;
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
}''')
