package org.hw1;

import org.hw1.data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.security.MessageDigest;

import org.hw1.data.ServiceRequestRepository;
import org.hw1.data.ServiceStatusHistoryRepository;
import org.hw1.service.ServiceRequestService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ServiceStatusHistoryRepository serviceStatusHistoryRepository;

    @InjectMocks
    private ServiceRequestService service;

    static private User user;
    static private Municipality municipality;
    static private ServiceRequest request;

    @BeforeAll
    static void initAll() throws Exception {
        user = new User();
        user.setName("Jhon doe");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest("password".getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        user.setPassword(sb.toString());
        user.setStaff(false);

        municipality = new Municipality();
        municipality.setName("Lisbon");

        request = new ServiceRequest();
        request.setToken("test-token");
        request.setUser(user);
        request.setMunicipality(municipality);
        request.setRequestedDate(LocalDate.now().plusDays(1));
        request.setTimeSlot(LocalTime.of(10, 0));
        request.setDescription("Test");
    }

    @BeforeEach
    void setUp() throws Exception {

        lenient().when(serviceRequestRepository.save(any(ServiceRequest.class)))
            .thenAnswer(invocation -> {
                ServiceRequest req = invocation.getArgument(0);
                return req;
            });
    }

    @Test
    void testCreateServiceRequest() {
        ServiceRequest created = service.createServiceRequest(user, municipality, LocalDate.now().plusDays(1),
            LocalTime.of(10, 0), "Test");

        assertThat(created, notNullValue());
        assertThat(created.getUser(), is(user));
        assertThat(created.getMunicipality(), is(municipality));
        assertThat(created.getDescription(), is("Test"));
        assertThat(created.getRequestedDate(), is(LocalDate.now().plusDays(1)));
        assertThat(created.getTimeSlot(), is(LocalTime.of(10, 0)));
    }

    @Test
    void testCreateServiceRequestNotAvailableSunday() {
        LocalDate sunday = LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
        if (!sunday.isAfter(LocalDate.now())) {
            sunday = sunday.plusWeeks(1);
        }
        boolean available = service.isAvailable(municipality, sunday, LocalTime.of(10, 0));
        assertThat(available, is(false));

        ServiceRequest created = service.createServiceRequest(user, municipality, sunday, LocalTime.of(10, 0), "Test on Sunday");
        assertThat(created, nullValue());
    }

    @Test
    void testCreateServiceRequestNotAvailableTimeConflictBefore() {
        ServiceRequest existing = new ServiceRequest();
        existing.setToken("conflict-token");
        existing.setUser(user);
        existing.setMunicipality(municipality);
        existing.setRequestedDate(LocalDate.now().plusDays(2));
        existing.setTimeSlot(LocalTime.of(10, 0));
        existing.setDescription("Existing");

        when(serviceRequestRepository.findByMunicipalityAndRequestedDate(eq(municipality), any(LocalDate.class)))
            .thenReturn(Arrays.asList(existing));

        boolean available = service.isAvailable(municipality, LocalDate.now().plusDays(2), LocalTime.of(9, 30));
        assertThat(available, is(false));

        ServiceRequest created = service.createServiceRequest(user, municipality, LocalDate.now().plusDays(2), LocalTime.of(9, 30), "Conflict before");
        assertThat(created, nullValue());
    }

    @Test
    void testCreateServiceRequestNotAvailableTimeConflictAfter() {
        ServiceRequest existing = new ServiceRequest();
        existing.setToken("conflict-token2");
        existing.setUser(user);
        existing.setMunicipality(municipality);
        existing.setRequestedDate(LocalDate.now().plusDays(3));
        existing.setTimeSlot(LocalTime.of(10, 0));
        existing.setDescription("Existing");

        when(serviceRequestRepository.findByMunicipalityAndRequestedDate(eq(municipality), any(LocalDate.class)))
            .thenReturn(Arrays.asList(existing));

        boolean available = service.isAvailable(municipality, LocalDate.now().plusDays(3), LocalTime.of(10, 30));
        assertThat(available, is(false));

        ServiceRequest created = service.createServiceRequest(user, municipality, LocalDate.now().plusDays(3), LocalTime.of(10, 30), "Conflict after");
        assertThat(created, nullValue());
    }

    @Test
    void testCreateServiceRequestNotAvailableExactSameTime() {
        ServiceRequest existing = new ServiceRequest();
        existing.setToken("conflict-token3");
        existing.setUser(user);
        existing.setMunicipality(municipality);
        existing.setRequestedDate(LocalDate.now().plusDays(4));
        existing.setTimeSlot(LocalTime.of(10, 0));
        existing.setDescription("Existing");

        when(serviceRequestRepository.findByMunicipalityAndRequestedDate(eq(municipality), any(LocalDate.class)))
            .thenReturn(Arrays.asList(existing));

        boolean available = service.isAvailable(municipality, LocalDate.now().plusDays(4), LocalTime.of(10, 0));
        assertThat(available, is(false));

        ServiceRequest created = service.createServiceRequest(user, municipality, LocalDate.now().plusDays(4), LocalTime.of(10, 0), "Conflict exact");
        assertThat(created, nullValue());
    }

    @Test
    void testGetServiceRequestByToken() {
        when(serviceRequestRepository.findByToken("test-token")).thenReturn(Optional.of(request));
        Optional<ServiceRequest> found = service.getServiceRequestByToken("test-token");
        assertThat(found.isPresent(), is(true));
        assertThat(found.get(), is(request));
    }

    @Test
    void testCancelServiceRequest() throws Exception {
        when(serviceRequestRepository.findByToken("test-token")).thenReturn(Optional.of(request));
        assertDoesNotThrow(() -> service.cancelServiceRequest("test-token"));
    }

    @Test
    void testGetServiceStatusHistory() throws Exception {
        ServiceStatusHistory history1 = new ServiceStatusHistory();
        history1.setServiceRequest(request);
        history1.setStatus(Status.RECEIVED);
        history1.setCreatedAt(LocalDateTime.now().minusDays(1));

        ServiceStatusHistory history2 = new ServiceStatusHistory();
        history2.setServiceRequest(request);
        history2.setStatus(Status.ASSIGNED);
        history2.setCreatedAt(LocalDateTime.now());

        when(serviceRequestRepository.findByToken("test-token")).thenReturn(Optional.of(request));
        when(serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request))
            .thenReturn(Arrays.asList(history2, history1));

        assertDoesNotThrow(() -> service.getServiceStatusHistory("test-token"));
        List<ServiceStatusHistory> histories = service.getServiceStatusHistory("test-token");
        assertThat(histories, notNullValue());
        assertThat(histories, hasItems(history1, history2));
    }

    @Test
    void testUpdateServiceRequestStatus() throws Exception {
        when(serviceRequestRepository.findByToken("test-token")).thenReturn(Optional.of(request));
        when(serviceStatusHistoryRepository.save(any(ServiceStatusHistory.class))).thenReturn(new ServiceStatusHistory());
        assertDoesNotThrow(() -> service.updateServiceRequestStatus("test-token", Status.COMPLETED));
    }

    @Test
    void testGetServiceRequestsByMunicipality() {
        when(serviceRequestRepository.findByMunicipality(municipality)).thenReturn(Arrays.asList(request));
        List<ServiceRequest> result = service.getServiceRequestsByMunicipality(municipality);
        assertThat(result, notNullValue());
        assertThat(result, is(not(empty())));
        assertThat(result.get(0).getMunicipality(), is(municipality));
    }

    @Test
    void testGetServiceRequestsByUser() {
        when(serviceRequestRepository.findByUserId(user.getId())).thenReturn(Arrays.asList(request));
        List<ServiceRequest> result = service.getServiceRequestsByUser(user);
        assertThat(result, notNullValue());
        assertThat(result, is(not(empty())));
        assertThat(result.get(0).getUser(), is(user));
        assertThat(result.get(0).getDescription(), is("Test"));
        assertThat(result.get(0).getRequestedDate(), is(LocalDate.now().plusDays(1)));
        assertThat(result.get(0).getTimeSlot(), is(LocalTime.of(10, 0)));
        assertThat(result.get(0).getToken(), is("test-token"));
    }
    @Test
    void testAddStatusHistoryEntry() {
        service.addStatusHistoryEntry(request, Status.RECEIVED);
        verify(serviceStatusHistoryRepository).save(any(ServiceStatusHistory.class));
    }

    @Test
    void testIsAvailable() {
        when(serviceRequestRepository.findByMunicipality(municipality)).thenReturn(Arrays.asList());
        boolean available = service.isAvailable(municipality, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        assertThat(available, is(true));
    }
}
