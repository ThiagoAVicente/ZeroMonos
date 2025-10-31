package org.hw1.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Optional<ServiceRequest> findByToken(String token);
    List<ServiceRequest> findByMunicipality(Municipality municipality);
    List<ServiceRequest> findByUserId(Long userId);
    List<ServiceRequest> findByMunicipalityAndRequestedDate(Municipality municipality, LocalDate requestedDate);
}
