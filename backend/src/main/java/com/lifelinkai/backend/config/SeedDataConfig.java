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

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (hospitalRepository.count() == 0) {
                Hospital h = new Hospital();
                h.setName("City Hospital");
                h.setCity("New York");
                hospitalRepository.save(h);

                Doctor d = new Doctor();
                d.setName("Dr. Smith");
                d.setHospitalId(h.getId());
                doctorRepository.save(d);

                Patient p = new Patient();
                p.setCity("New York");
                patientRepository.save(p);

                Ambulance a = new Ambulance();
                a.setVehicleNumber("NY-1000");
                a.setHospitalId(h.getId());
                a.setType(AmbulanceType.BASIC);
                ambulanceRepository.save(a);

                Driver dr = new Driver();
                dr.setLicenseNumber("LIC12345");
                driverRepository.save(dr);
            }
        };
    }
}
