package org.hw1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.hw1.service.MunicipalityService;
import org.hw1.data.MunicipalityRepository;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.hw1.data.Municipality;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MunicipalityServiceTest {


    @Mock
    private MunicipalityRepository municipalityRepository;

    @InjectMocks
    private MunicipalityService municipalityService;

    @Test
    void testFetchMunicipalityFromApi() {
        when(municipalityRepository.count()).thenReturn(0L);
        Boolean result = municipalityService.fetchFromAPI();
        assertThat(result, is(true));
        verify(municipalityRepository, times(1)).saveAll(any());
    }

    @Test
    void testFetchMunicipalityFromApiWhenDataExists() {
        when(municipalityRepository.count()).thenReturn(10L);
        Boolean result = municipalityService.fetchFromAPI();
        assertThat(result, is(true));
        verify(municipalityRepository, never()).save(any());
    }

    @Test
    void getAllMunicipalitiesTest() {
        when(municipalityRepository.findAll()).thenReturn(
                List.of(new Municipality( "Lisbon"), new Municipality( "Porto"))
        );
        var municipalities = municipalityService.getAllMunicipalities();
        assertThat(municipalities, hasSize(2));
        assertThat(municipalities.get(0).getName(), is("Lisbon"));
        assertThat(municipalities.get(1).getName(), is("Porto"));
    }

    @Test
    void getMunicipalityByNameExistsTest() {
        Municipality lisbon = new Municipality("Lisbon");
        when(municipalityRepository.findByName("Lisbon")).thenReturn(Optional.of(lisbon));
        Optional<Municipality> result = municipalityService.getMunicipalityByName("Lisbon");
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getName(), is("Lisbon"));
    }

    @Test
    void getMunicipalityByNameNotExistsTest() {
        when(municipalityRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
        Optional<Municipality> result = municipalityService.getMunicipalityByName("Nonexistent");
        assertThat(result.isPresent(), is(false));
    }

}
