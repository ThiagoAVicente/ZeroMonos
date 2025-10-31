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
import java.util.stream.Collectors;
@Service
public class MunicipalityService {

    @Autowired
    private MunicipalityRepository municipalityRepository;

    public Boolean fetchFromAPI() {

        // check if repo already has data
        if (municipalityRepository.count() > 0) {
            return true;
        }

        String url = "https://json.geoapi.pt/municipios";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // print the response body
                String responseBody = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> names = objectMapper.readValue(responseBody, new TypeReference<List<String>>(){});

                // Create municipality objects
                List<Municipality> municipalities = names.stream()
                    .map(Municipality::new)
                    .collect(Collectors.toList());

                municipalityRepository.saveAll(municipalities);

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<Municipality> getAllMunicipalities() {
        return municipalityRepository.findAll();
    }

    public Optional<Municipality> getMunicipalityByName(String name) {
        return municipalityRepository.findByName(name);
    }



}
