package org.kucherenkoos.carsharingservice;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class CarSharingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarSharingServiceApplication.class, args);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String raw = "admin123";
        String encoded = "$2a$10$3EFAfTCinhHxmdyFwhZSrO0v8ZDen58nc0Sxy097oJnKE369Dvrou"; // з БД

        System.out.println(encoder.matches(raw, encoded));
    }
}
