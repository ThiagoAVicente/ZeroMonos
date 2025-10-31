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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceRequestService {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ServiceStatusHistoryRepository serviceStatusHistoryRepository;

	private static final int MAX_REQUESTS_PER_USER = 5;

	public ServiceRequest createServiceRequest(User user, Municipality municipality, LocalDate requestedDate, LocalTime timeSlot, String description) {
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
            return null;
        }
        ServiceRequest r = serviceRequestRepository.save(request);
        addStatusHistoryEntry(r, Status.RECEIVED);
        return r;

    }

    public Optional<ServiceRequest> getServiceRequestByToken(String token) {
        return serviceRequestRepository.findByToken(token);
    }

    public void cancelServiceRequest(String token) throws Exception {
        // check if request exists
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> new Exception("Service request not found"));

        // update status to CANCELLED
        addStatusHistoryEntry(request, Status.CANCELLED);

    }

    public List<ServiceStatusHistory> getServiceStatusHistory(String token) throws Exception {
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> new Exception("Service request not found"));

        return serviceStatusHistoryRepository.findByServiceRequestOrderByCreatedAtDesc(request);

    }

    public void updateServiceRequestStatus(String token, Status newStatus) throws Exception {
        ServiceRequest request = serviceRequestRepository.findByToken(token)
            .orElseThrow(() -> new Exception("Service request not found"));

        addStatusHistoryEntry(request, newStatus);
    }

    public List<ServiceRequest> getServiceRequestsByMunicipality(Municipality municipality) {
        return serviceRequestRepository.findByMunicipality(municipality);
    }

    public List<ServiceRequest> getServiceRequestsByUser(User user) {
        // check if user exists
        return serviceRequestRepository.findByUserId(user.getId());
    }

    public void addStatusHistoryEntry(ServiceRequest request, Status status) {
        ServiceStatusHistory history = new ServiceStatusHistory();
        history.setServiceRequest(request);
        history.setStatus(status);
        serviceStatusHistoryRepository.save(history);
    }

    public boolean isAvailable(Municipality municipality,
        LocalDate date,
        LocalTime time_slot){
        // check if is a sunday
        if (date.getDayOfWeek().getValue() == 7) {
            return false;
        }

        // find all requests for the municipality at the given date
        List<ServiceRequest> requests = serviceRequestRepository.findByMunicipalityAndRequestedDate(municipality, date);
        // check if there is any conflict. each time slot has an expected duration of 1 hour
        LocalTime before_max = time_slot.minusHours(1);
        LocalTime after_max = time_slot.plusHours(1);
        for (ServiceRequest request : requests) {
            LocalTime existingTimeSlot = request.getTimeSlot();
            LocalTime existing_before_max = existingTimeSlot.minusHours(1);
            LocalTime existing_after_max = existingTimeSlot.plusHours(1);
            boolean isTimeSlotConflict = (time_slot.isAfter(existing_before_max) && time_slot.isBefore(existing_after_max));
            boolean isExistingSlotConflict = (existingTimeSlot.isAfter(before_max) && existingTimeSlot.isBefore(after_max));
            if (isTimeSlotConflict || isExistingSlotConflict){
                return false;
            }

        }
        return true;
    }

}
