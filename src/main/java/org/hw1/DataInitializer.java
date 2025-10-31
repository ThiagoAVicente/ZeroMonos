package org.hw1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.hw1.service.MunicipalityService;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MunicipalityService municipalityService;

    @Override
    public void run(String... args) throws Exception {
        municipalityService.fetchFromAPI();
    }
}
