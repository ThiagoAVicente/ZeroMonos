     package org.hw1.data;

     import org.springframework.data.jpa.repository.JpaRepository;
     import java.util.List;

     public interface ServiceStatusHistoryRepository extends JpaRepository<ServiceStatusHistory, Long> {
         List<ServiceStatusHistory> findByServiceRequestOrderByCreatedAtDesc(ServiceRequest serviceRequest);
     }
