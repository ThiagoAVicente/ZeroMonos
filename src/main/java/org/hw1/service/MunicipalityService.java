package org.hw1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.hw1.data.MunicipalityRepository;
import org.hw1.data.Municipality;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class MunicipalityService {

    private static final Logger logger = LoggerFactory.getLogger(MunicipalityService.class);

    private MunicipalityRepository municipalityRepository;

    @Autowired
    public MunicipalityService(MunicipalityRepository municipalityRepository) {
        this.municipalityRepository = municipalityRepository;
    }

    public Boolean fetchFromAPI() {
        logger.info("Fetching municipalities from API...");
        // check if repo already has data
        if (municipalityRepository.count() > 0) {
            logger.info("Municipality repository already has data. Skipping fetch.");
            return true;
        }

        String url = "https://json.geoapi.pt/municipios";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                logger.debug("Received response from API: {}", responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> names = objectMapper.readValue(responseBody, new TypeReference<List<String>>(){});
                // Create municipality objects
                List<Municipality> municipalities = names.stream()
                    .map(Municipality::new)
                    .toList();

                municipalityRepository.saveAll(municipalities);
                logger.info("Saved {} municipalities to repository.", municipalities.size());

                return true;
            } else {
                logger.warn("Failed to fetch municipalities. Status code: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while fetching municipalities from API", e);
            return false;
        }
    }

    public List<Municipality> getAllMunicipalities() {
        logger.info("Fetching all municipalities from repository.");
        return municipalityRepository.findAll();
    }

    public Optional<Municipality> getMunicipalityByName(String name) {
        logger.info("Fetching municipality by name: {}", name);
        return municipalityRepository.findByName(name);
    }

}
