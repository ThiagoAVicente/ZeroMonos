package org.hw1.service;

import org.hw1.data.Status;
import org.hw1.data.Municipality;
import org.hw1.data.ServiceRequest;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.User;
import org.hw1.data.ServiceRequestRepository;
import org.hw1.data.ServiceStatusHistoryRepository;
import org.hw1.boundary.ResourceNotFoundException;
import org.hw1.boundary.InvalidStatusTransitionException;

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
            user.getId(),
            municipality.getName(),
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
            logger.warn("Requested slot not available for municipality: {}, date: {}, timeSlot: {}",municipality.getName(), requestedDate, timeSlot);
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

    public void cancelServiceRequest(String token) throws ResourceNotFoundException, InvalidStatusTransitionException {
        logger.info("Cancelling service request with token: {}", token);
        // check if request exists
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new ResourceNotFoundException("Service request not found");
            });

        // get current status
        List<ServiceStatusHistory> history = serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);
        Status currentStatus = history.get(0).getStatus();

        // only allow cancellation if status is RECEIVED or ASSIGNED
        if (currentStatus != Status.RECEIVED && currentStatus != Status.ASSIGNED) {
            logger.warn("Cannot cancel service request with token: {} - current status is: {}", token, currentStatus);
            throw new InvalidStatusTransitionException("Cannot cancel request. Current status is: " + currentStatus);
        }

        // update status to CANCELLED
        addStatusHistoryEntry(request, Status.CANCELLED);
        logger.info("Service request with token: {} cancelled", token);
    }

    public List<ServiceStatusHistory> getServiceStatusHistory(String token) throws ResourceNotFoundException {
        logger.info("Fetching service status history for token: {}", token);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new ResourceNotFoundException("Service request not found");
            });

        return serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);
    }

    public void updateServiceRequestStatus(String token, Status newStatus) throws ResourceNotFoundException, InvalidStatusTransitionException {
        logger.info("Updating status for service request token: {} to status: {}", token, newStatus);
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error(TOKENNOTFOUND, token);
                return new ResourceNotFoundException("Service request not found");
            });

        // Get current status from history
        List<ServiceStatusHistory> history = serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);
        if (!history.isEmpty()) {
            Status currentStatus = history.get(0).getStatus();

            // Validate status transition
            validateStatusTransition(currentStatus, newStatus, token);
        }

        addStatusHistoryEntry(request, newStatus);
        logger.info("Status updated for service request token: {} to status: {}", token, newStatus);
    }

    public List<ServiceRequest> getServiceRequestsByMunicipality(Municipality municipality) {
        logger.info("Fetching service requests for municipality: {}", municipality.getName());
        return serviceRequestRepository.findByMunicipality(municipality);
    }

    public List<ServiceRequest> getServiceRequestsByUser(User user) {
        logger.info("Fetching service requests for user: {}",user.getId());
        // check if user exists
        return serviceRequestRepository.findByUserId(user.getId());
    }

    public void addStatusHistoryEntry(ServiceRequest request, Status status) {
        logger.info("Adding status history entry for request token: {}, status: {}",request.getToken(), status);
        ServiceStatusHistory history = new ServiceStatusHistory();
        history.setServiceRequest(request);
        history.setStatus(status);
        serviceStatusHistoryRepository.save(history);
    }

    public boolean isAvailable(Municipality municipality,
        LocalDate date,
        LocalTime timeSlot){
        logger.debug("Checking availability for municipality: {}, date: {}, time_slot: {}",municipality.getName(), date, timeSlot);

        // check if date is in the past
        if (date.isBefore(LocalDate.now())) {
            logger.info("Requested date {} is in the past, not available.", date);
            return false;
        }

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


    private void validateStatusTransition(Status currentStatus, Status newStatus, String token) throws InvalidStatusTransitionException {
        // Define the order of statuses in the normal workflow
        List<Status> workflowOrder = List.of(Status.RECEIVED, Status.ASSIGNED, Status.IN_PROGRESS, Status.COMPLETED);

        // No transitions allowed after COMPLETED
        if (currentStatus == Status.COMPLETED) {
            logger.warn("Cannot change status from COMPLETED for token: {}", token);
            throw new InvalidStatusTransitionException("Cannot change status of a completed request");
        }

        // No transitions allowed after CANCELLED
        if (currentStatus == Status.CANCELLED) {
            logger.warn("Cannot change status from CANCELLED for token: {}", token);
            throw new InvalidStatusTransitionException("Cannot change status of a cancelled request");
        }

        // CANCELLED can only be set via the cancelServiceRequest method
        if (newStatus == Status.CANCELLED) {
            logger.warn("Cannot set status to CANCELLED directly for token: {}. Use cancelServiceRequest method.", token);
            throw new InvalidStatusTransitionException("Cannot set status to CANCELLED directly. Use the cancel endpoint.");
        }

        // Check if both statuses are in the normal workflow
        int currentIndex = workflowOrder.indexOf(currentStatus);
        int newIndex = workflowOrder.indexOf(newStatus);

        // Ensure we're not moving backward in the workflow
        if (newIndex <= currentIndex) {
            logger.warn("Cannot move backward from {} to {} for token: {}", currentStatus, newStatus, token);
            throw new InvalidStatusTransitionException("Cannot move backward from " + currentStatus + " to " + newStatus);
        }

        logger.debug("Status transition from {} to {} is valid for token: {}", currentStatus, newStatus, token);
    }

}
