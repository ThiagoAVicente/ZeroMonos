package org.hw1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.hw1.service.MunicipalityService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MunicipalityService municipalityService;

    @Autowired
    public DataInitializer(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @Override
    public void run(String... args) throws Exception {
        municipalityService.fetchFromAPI();
    }
}
