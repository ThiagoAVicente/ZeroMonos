package org.hw1.service;

import org.hw1.data.Status;
import org.hw1.data.Municipality;
import org.hw1.data.ServiceRequest;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.User;
import org.hw1.data.ServiceRequestRepository;
import org.hw1.data.ServiceStatusHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestService.class);

    private ServiceRequestRepository serviceRequestRepository;
    private ServiceStatusHistoryRepository serviceStatusHistoryRepository;
    @Autowired
    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 ServiceStatusHistoryRepository serviceStatusHistoryRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceStatusHistoryRepository = serviceStatusHistoryRepository;
    }

    static final String TOKENNOTFOUND = "Service request not found for token: {}";


    public ServiceRequest createServiceRequest(User user, Municipality municipality, LocalDate requestedDate, LocalTime timeSlot, String description) {
        logger.info("Creating service request for user: {}, municipality: {}, date: {}, timeSlot: {}",
            user != null ? user.getId() : null,
            municipality != null ? municipality.getName() : null,
            requestedDate,
            timeSlot);
        ServiceRequest request = new ServiceRequest();
        request.setUser(user);
        request.setMunicipality(municipality);
        request.setRequestedDate(requestedDate);
        request.setTimeSlot(timeSlot);
        request.setDescription(description);
        // generate unique token
        String token = java.util.UUID.randomUUID().toString();
        request.setToken(token);

        if (!isAvailable(municipality, requestedDate, timeSlot)) {
            logger.warn("Requested slot not available for municipality: {}, date: {}, timeSlot: {}",
                municipality != null ? municipality.getName() : null, requestedDate, timeSlot);
            return null;
        }
        ServiceRequest r = serviceRequestRepository.save(request);
        logger.info("Service request created with token: {}", token);
        addStatusHistoryEntry(r, Status.RECEIVED);
        return r;
    }

    public Optional<ServiceRequest> getServiceRequestByToken(String token) {
        logger.info("Fetching service request by token: {}", token);
        return serviceRequestRepository.findByToken(token);
    }

    public void cancelServiceRequest(String token) throws Exception {
        logger.info("Cancelling service request with token: {}", token);
        // check if request exists
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new Exception("Service request not found");
            });

        // update status to CANCELLED
        addStatusHistoryEntry(request, Status.CANCELLED);
        logger.info("Service request with token: {} cancelled", token);
    }

    public List<ServiceStatusHistory> getServiceStatusHistory(String token) throws Exception {
        logger.info("Fetching service status history for token: {}", token);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new Exception("Service request not found");
            });

        return serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);
    }

    public void updateServiceRequestStatus(String token, Status newStatus) throws Exception {
        logger.info("Updating status for service request token: {} to status: {}", token, newStatus);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new Exception("Service request not found");
            });

        addStatusHistoryEntry(request, newStatus);
        logger.info("Status updated for service request token: {} to status: {}", token, newStatus);
    }

    public List<ServiceRequest> getServiceRequestsByMunicipality(Municipality municipality) {
        logger.info("Fetching service requests for municipality: {}", municipality != null ? municipality.getName() : null);
        return serviceRequestRepository.findByMunicipality(municipality);
    }

    public List<ServiceRequest> getServiceRequestsByUser(User user) {
        logger.info("Fetching service requests for user: {}", user != null ? user.getId() : null);
        // check if user exists
        return serviceRequestRepository.findByUserId(user.getId());
    }

    public void addStatusHistoryEntry(ServiceRequest request, Status status) {
        logger.info("Adding status history entry for request token: {}, status: {}",
            request != null ? request.getToken() : null, status);
        ServiceStatusHistory history = new ServiceStatusHistory();
        history.setServiceRequest(request);
        history.setStatus(status);
        serviceStatusHistoryRepository.save(history);
    }

    public boolean isAvailable(Municipality municipality,
        LocalDate date,
        LocalTime timeSlot){
        logger.debug("Checking availability for municipality: {}, date: {}, time_slot: {}",
            municipality != null ? municipality.getName() : null, date, timeSlot);
        // check if is a sunday
        if (date.getDayOfWeek().getValue() == 7) {
            logger.info("Requested date is a Sunday, not available.");
            return false;
        }

        LocalTime serviceStart = LocalTime.of(9, 0);
        LocalTime serviceEnd = LocalTime.of(18, 0);
        if (timeSlot.isBefore(serviceStart) || !timeSlot.isBefore(serviceEnd)) {
            logger.info("Requested time slot {} is outside service hours (09:00-18:00), not available.", timeSlot);
            return false;
        }

        List<ServiceRequest> requests = serviceRequestRepository.findByMunicipalityAndRequestedDate(municipality, date);
        LocalTime beforeMax = timeSlot.minusHours(1);
        LocalTime afterMax = timeSlot.plusHours(1);
        for (ServiceRequest request : requests) {
            LocalTime existingTimeSlot = request.getTimeSlot();
            LocalTime existingBeforeMax = existingTimeSlot.minusHours(1);
            LocalTime existingAfterMax = existingTimeSlot.plusHours(1);
            boolean isTimeSlotConflict = (timeSlot.isAfter(existingBeforeMax) && timeSlot.isBefore(existingAfterMax));
            boolean isExistingSlotConflict = (existingTimeSlot.isAfter(beforeMax) && existingTimeSlot.isBefore(afterMax));
            if (isTimeSlotConflict || isExistingSlotConflict){
                logger.info("Time slot conflict detected for municipality: {}, date: {}, time_slot: {}",
                    municipality != null ? municipality.getName() : null, date, timeSlot);
                return false;
            }
        }
        logger.debug("Slot available for municipality: {}, date: {}, time_slot: {}",
            municipality != null ? municipality.getName() : null, date, timeSlot);
        return true;
    }

}
