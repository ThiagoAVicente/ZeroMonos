package org.hw1;

import org.hw1.data.Municipality;
import org.hw1.data.ServiceRequest;
import org.hw1.data.User;
import org.hw1.data.ServiceRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class ServiceRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Test
    void testFindByToken() {
        Municipality municipality = new Municipality("Lisbon");
        entityManager.persist(municipality);

        User user = new User();
        user.setName("John Doe");
        user.setPassword("password");
        user.setStaff(false);
        entityManager.persist(user);

        ServiceRequest request = new ServiceRequest();
        request.setToken("test-token-123");
        request.setUser(user);
        request.setMunicipality(municipality);
        request.setRequestedDate(LocalDate.now());
        request.setTimeSlot(LocalTime.of(10, 0));
        request.setDescription("Test request");
        entityManager.persist(request);
        entityManager.flush();

        Optional<ServiceRequest> found = serviceRequestRepository.findByToken("test-token-123");

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().getToken(), is("test-token-123"));

        Optional<ServiceRequest> notFound = serviceRequestRepository.findByToken("non-existent");

        assertThat(notFound.isPresent(), is(false));
    }

    @Test
    void testFindByMunicipality() {
        Municipality municipality1 = new Municipality("Lisbon");
        entityManager.persist(municipality1);

        Municipality municipality2 = new Municipality("Porto");
        entityManager.persist(municipality2);

        User user = new User();
        user.setName("Jane Doe");
        user.setPassword("password");
        user.setStaff(false);
        entityManager.persist(user);

        ServiceRequest request1 = new ServiceRequest();
        request1.setToken("token1");
        request1.setUser(user);
        request1.setMunicipality(municipality1);
        request1.setRequestedDate(LocalDate.now());
        request1.setTimeSlot(LocalTime.of(10, 0));
        request1.setDescription("Request 1");
        entityManager.persist(request1);

        ServiceRequest request2 = new ServiceRequest();
        request2.setToken("token2");
        request2.setUser(user);
        request2.setMunicipality(municipality1);
        request2.setRequestedDate(LocalDate.now());
        request2.setTimeSlot(LocalTime.of(11, 0));
        request2.setDescription("Request 2");
        entityManager.persist(request2);

        ServiceRequest request3 = new ServiceRequest();
        request3.setToken("token3");
        request3.setUser(user);
        request3.setMunicipality(municipality2);
        request3.setRequestedDate(LocalDate.now());
        request3.setTimeSlot(LocalTime.of(12, 0));
        request3.setDescription("Request 3");
        entityManager.persist(request3);

        entityManager.flush();

        List<ServiceRequest> requestsForLisbon = serviceRequestRepository.findByMunicipality(municipality1);

        assertThat(requestsForLisbon, hasSize(2));
        assertThat(requestsForLisbon.stream().map(ServiceRequest::getToken).toList(), containsInAnyOrder("token1", "token2"));

        List<ServiceRequest> requestsForPorto = serviceRequestRepository.findByMunicipality(municipality2);

        assertThat(requestsForPorto, hasSize(1));
        assertThat(requestsForPorto.get(0).getToken(), is("token3"));
    }
}
