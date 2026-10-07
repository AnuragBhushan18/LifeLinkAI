package com.lifelinkai.backend.config;

import com.lifelinkai.backend.model.*;
import com.lifelinkai.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Seed Admin User
            if (!userRepository.existsByEmail("admin@lifelink.com")) {
                User admin = new User("System Admin", "admin@lifelink.com", passwordEncoder.encode("password123"), "555-0100", Role.ADMIN);
                userRepository.save(admin);
            }

            // Seed Patient User
            User patientUser = userRepository.findByEmail("patient@lifelink.com").orElseGet(() -> {
                User p = new User("Jane Doe", "patient@lifelink.com", passwordEncoder.encode("password123"), "555-0101", Role.PATIENT);
                return userRepository.save(p);
            });

            // Seed Hospital User & Hospital
            User hospitalUser = userRepository.findByEmail("hospital@lifelink.com").orElseGet(() -> {
                User h = new User("City General Hospital", "hospital@lifelink.com", passwordEncoder.encode("password123"), "555-0102", Role.HOSPITAL_STAFF);
                return userRepository.save(h);
            });

            Hospital hospital = hospitalRepository.findAll().stream().findFirst().orElseGet(() -> {
                Hospital h = new Hospital();
                h.setName("City Hospital");
                h.setCity("New York");
                h.setLatitude(40.7306);
                h.setLongitude(-73.9352);
                h.setTotalBeds(50);
                h.setAvailableBeds(20);
                h.setTotalIcuBeds(10);
                h.setAvailableIcuBeds(5);
                h.setOperationalStatus(OperationalStatus.ACTIVE);
                h.setUserId(hospitalUser.getId());
                return hospitalRepository.save(h);
            });
            if (hospital.getUserId() == null) {
                hospital.setUserId(hospitalUser.getId());
                hospitalRepository.save(hospital);
            }

            // Seed Driver User & Driver
            User driverUser = userRepository.findByEmail("driver@lifelink.com").orElseGet(() -> {
                User d = new User("Dave Driver", "driver@lifelink.com", passwordEncoder.encode("password123"), "555-0103", Role.AMBULANCE_DRIVER);
                return userRepository.save(d);
            });

            Driver driver = driverRepository.findByUserId(driverUser.getId()).orElseGet(() -> {
                Driver dr = new Driver();
                dr.setLicenseNumber("LIC12345");
                dr.setExperienceYears(6);
                dr.setAvailability(Availability.AVAILABLE);
                dr.setUserId(driverUser.getId());
                return driverRepository.save(dr);
            });

            // Ensure Patient record exists
            if (patientRepository.findByUserId(patientUser.getId()).isEmpty()) {
                Patient p = new Patient();
                p.setCity("New York");
                p.setUserId(patientUser.getId());
                patientRepository.save(p);
            }

            // Seed Rocket Patient User
            User rocketUser = userRepository.findByEmail("rocket@gmail.com").orElseGet(() -> {
                User r = new User("Rocket", "rocket@gmail.com", passwordEncoder.encode("password123"), "555-9999", Role.PATIENT);
                return userRepository.save(r);
            });
            if (patientRepository.findByUserId(rocketUser.getId()).isEmpty()) {
                Patient rp = new Patient();
                rp.setCity("New York");
                rp.setUserId(rocketUser.getId());
                patientRepository.save(rp);
            }

            // Seed Ambulances
            if (ambulanceRepository.count() == 0) {
                Ambulance a = new Ambulance();
                a.setVehicleNumber("NY-1000");
                a.setHospitalId(hospital.getId());
                a.setDriverId(driver.getId());
                a.setType(AmbulanceType.ICU);
                a.setStatus(AmbulanceStatus.IDLE);
                a.setLatitude(40.7128);
                a.setLongitude(-74.0060);
                ambulanceRepository.save(a);

                Ambulance a2 = new Ambulance();
                a2.setVehicleNumber("NY-1001");
                a2.setHospitalId(hospital.getId());
                a2.setDriverId(driver.getId());
                a2.setType(AmbulanceType.BASIC);
                a2.setStatus(AmbulanceStatus.IDLE);
                a2.setLatitude(40.7135);
                a2.setLongitude(-74.0040);
                ambulanceRepository.save(a2);
            } else {
                for (Ambulance amb : ambulanceRepository.findAll()) {
                    if (amb.getDriverId() == null) {
                        amb.setDriverId(driver.getId());
                        ambulanceRepository.save(amb);
                    }
                }
            }
        };
    }
}
