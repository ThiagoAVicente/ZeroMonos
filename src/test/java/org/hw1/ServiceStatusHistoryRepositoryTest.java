package org.hw1;

import org.hw1.data.ServiceStatusHistoryRepository;
import org.hw1.data.Status;
import org.hw1.data.Municipality;
import org.hw1.data.ServiceRequest;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
public class ServiceStatusHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceStatusHistoryRepository serviceStatusHistoryRepository;

    @Test
    public void testFindByServiceRequestOrderByChangedAtDesc() {
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

        ServiceStatusHistory history1 = new ServiceStatusHistory();
        history1.setServiceRequest(request);
        history1.setStatus(Status.RECEIVED);
        entityManager.persist(history1);

        ServiceStatusHistory history2 = new ServiceStatusHistory();
        history2.setServiceRequest(request);
        history2.setStatus(Status.ASSIGNED);
        entityManager.persist(history2);

        ServiceStatusHistory history3 = new ServiceStatusHistory();
        history3.setServiceRequest(request);
        history3.setStatus(Status.IN_PROGRESS);
        entityManager.persist(history3);

        entityManager.flush();

        List<ServiceStatusHistory> historyList = serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);

        assertThat(historyList, hasSize(3));
        assertThat(historyList.get(0).getStatus(), is(Status.IN_PROGRESS));
        assertThat(historyList.get(1).getStatus(), is(Status.ASSIGNED));
        assertThat(historyList.get(2).getStatus(), is(Status.RECEIVED));
    }
}
