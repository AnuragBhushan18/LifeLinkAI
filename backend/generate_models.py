import os

base_path = r"c:\Users\Anurag_Bhushan\Desktop\LifeLinkAI\backend\src\main\java\com\lifelinkai\backend"

def create_file(subpath, content):
    full_path = os.path.join(base_path, subpath)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

models = {
    'Patient': '''package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "patients")
public class Patient {
    @Id private String id;
    private String userId;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String address;
    private String city;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private List<String> allergies;
    private List<String> currentMedications;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}''',
    'Doctor': '''package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "doctors")
public class Doctor {
    @Id private String id;
    private String userId;
    private String hospitalId;
    private String name;
    private String specialization;
    private String department;
    private String licenseNumber;
    private String phone;
    private Availability availability = Availability.AVAILABLE;
    private String shift;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}''',
    'Hospital': '''package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "hospitals")
public class Hospital {
    @Id private String id;
    private String name;
    private String registrationNumber;
    private String phone;
    private String email;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private OperationalStatus operationalStatus = OperationalStatus.ACTIVE;
    private Integer totalBeds = 0;
    private Integer availableBeds = 0;
    private Integer totalIcuBeds = 0;
    private Integer availableIcuBeds = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}''',
    'Ambulance': '''package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "ambulances")
public class Ambulance {
    @Id private String id;
    private String vehicleNumber;
    private String hospitalId;
    private String driverId;
    private AmbulanceType type;
    private List<String> equipment;
    private Double latitude;
    private Double longitude;
    private Availability availability = Availability.AVAILABLE;
    private AmbulanceStatus status = AmbulanceStatus.IDLE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}''',
    'Driver': '''package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "drivers")
public class Driver {
    @Id private String id;
    private String userId;
    private String licenseNumber;
    private String phone;
    private Integer experienceYears;
    private Availability availability = Availability.AVAILABLE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}'''
}

for k, v in models.items():
    create_file(f'model/{k}.java', v)
    create_file(f'repository/{k}Repository.java', f'''package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.{k};
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface {k}Repository extends MongoRepository<{k}, String> {{
    Optional<{k}> findByUserId(String userId);
}}''')

# Custom methods
create_file('repository/DoctorRepository.java', '''package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByUserId(String userId);
    List<Doctor> findByHospitalId(String hospitalId);
}''')

create_file('repository/AmbulanceRepository.java', '''package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.Ambulance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AmbulanceRepository extends MongoRepository<Ambulance, String> {
    Optional<Ambulance> findByVehicleNumber(String vehicleNumber);
    List<Ambulance> findByHospitalId(String hospitalId);
}''')
