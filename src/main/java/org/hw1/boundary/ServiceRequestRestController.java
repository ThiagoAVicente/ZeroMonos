package org.hw1.boundary;

import org.hw1.service.ServiceRequestService;
import org.hw1.service.UserService;
import org.hw1.service.MunicipalityService;
import org.hw1.data.ServiceRequest;
import org.hw1.data.Municipality;
import org.hw1.data.Status;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.User;
import org.hw1.boundary.dto.CreateServiceRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/requests")
@Tag(name = "Service Requests")
public class ServiceRequestRestController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ServiceRequestRestController.class);

    private ServiceRequestService serviceRequestService;
    private UserService userService;
    private MunicipalityService municipalityService;

    @Autowired
    public ServiceRequestRestController(ServiceRequestService serviceRequestService, UserService userService, MunicipalityService municipalityService) {
        this.serviceRequestService = serviceRequestService;
        this.userService = userService;
        this.municipalityService = municipalityService;
    }

    @PostMapping
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ServiceRequest> createRequest(@RequestBody CreateServiceRequestDTO request) {
        logger.info("Received createRequest for user: {}, municipality: {}", request.getUser(), request.getMunicipality());

        User u = request.getUser() != null
            ? userService.getUserByName(request.getUser()).orElse(null)
            : null;
        if (u != null) {
            logger.info("User found: {}", u.getName());
        } else {
            logger.warn("User not found: {}", request.getUser());
        }
        Municipality m = request.getMunicipality() != null
            ? municipalityService.getMunicipalityByName(request.getMunicipality()).orElse(null)
            : null;
        if (m != null) {
            logger.info("Municipality found: {}", m.getName());
        }else {
            logger.warn("Municipality not found: {}", request.getMunicipality());
        }
        if (u == null || m == null) {
            logger.warn("Invalid user or municipality. User: {}, Municipality: {}", request.getUser(), request.getMunicipality());
            return ResponseEntity.badRequest().build();
        }

        LocalDate requestedDate = null;
        LocalTime timeSlot = null;
        try {
            requestedDate = request.getRequestedDate() != null ? LocalDate.parse(request.getRequestedDate()) : null;
            timeSlot = request.getTimeSlot() != null ? LocalTime.parse(request.getTimeSlot()) : null;
        } catch (Exception e) {
            logger.warn("Invalid date or time format. Date: {}, Time: {}", request.getRequestedDate(), request.getTimeSlot());
            return ResponseEntity.badRequest().build();
        }

        ServiceRequest created = serviceRequestService.createServiceRequest(
            u,
            m,
            requestedDate,
            timeSlot,
            request.getDescription()
        );
        if (created == null) {
            logger.warn("Request conflict or unavailable slot for user: {}, municipality: {}", request.getUser(), request.getMunicipality());
            return ResponseEntity.status(409).build();
        }
        logger.info("ServiceRequest created successfully with token: {}", created.getToken());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{token}")
    @Operation(summary = "Get request by token")
    public ResponseEntity<ServiceRequest> getRequestByToken(@PathVariable String token) {
        token = token.replaceAll("[\n\r]", "_");
        logger.info("Received getRequestByToken for token: {}", token);
        Optional<ServiceRequest> req = serviceRequestService.getServiceRequestByToken(token);
        if (req.isPresent()) {
            logger.info("ServiceRequest found for token: {}", token);
            return ResponseEntity.ok(req.get());
        }
        logger.warn("ServiceRequest not found for token: {}", token);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{token}")
    @Operation(summary = "Cancel a service request by token")
    public ResponseEntity<Void> cancelRequest(@PathVariable String token) {
        try {
            token = token.replaceAll("[\n\r]", "_");
            logger.info("Received cancel Request for token: {}", token);
            serviceRequestService.cancelServiceRequest(token);
            logger.info("ServiceRequest cancelled for token: {}", token);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Request not found for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidStatusTransitionException e) {
            logger.error("Cannot cancel request with token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get requests by municipality")
    public ResponseEntity<List<ServiceRequest>> getRequestsByMunicipality(@RequestParam String municipality) {
        municipality = municipality.replaceAll("[\n\r]", "_");
        logger.info("Received getRequestsByMunicipality for municipality: {}", municipality);
        if (municipality == null) {
            logger.warn("Municipality parameter is null");
            return ResponseEntity.ok(List.of());
        }
        Optional<Municipality> mun = municipalityService.getMunicipalityByName(municipality);
        if (mun.isEmpty()) {
            logger.warn("Municipality not found: {}", municipality);
            return ResponseEntity.notFound().build();
        }
        List<ServiceRequest> list = serviceRequestService.getServiceRequestsByMunicipality(mun.get());
        logger.info("Found {} requests for municipality: {}", list.size(), municipality);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{token}/status")
    @Operation(summary = "Update the status of a service request")
    public ResponseEntity<Void> updateRequestStatus(@PathVariable String token, @RequestBody Status status) {
        token = token.replaceAll("[\n\r]", "_");
        logger.info("Received updateRequestStatus for token: {}, status: {}", token, status);
        try {
            serviceRequestService.updateServiceRequestStatus(token, status);
            logger.info("ServiceRequest status updated for token: {}", token);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Request not found for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidStatusTransitionException e) {
            logger.error("Invalid status transition for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{token}/history")
    @Operation(summary = "Get the status history of a service request")
    public ResponseEntity<List<ServiceStatusHistory>> getServiceStatusHistory(@PathVariable String token) {
        token = token.replaceAll("[\n\r]", "_");
        logger.info("Received getServiceStatusHistory for token: {}", token);
        try {
            List<ServiceStatusHistory> history = serviceRequestService.getServiceStatusHistory(token);
            logger.info("ServiceStatusHistory retrieved for token: {}", token);
            return ResponseEntity.ok(history);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to retrieve history for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/municipalities")
    @Operation(summary = "Get all municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities() {
        logger.info("Received getMunicipalities request");
        List<Municipality> list = municipalityService.getAllMunicipalities();
        logger.info("Found {} municipalities", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get requests by user")
    public ResponseEntity<List<ServiceRequest>> getRequestsByUser(@PathVariable String username) {
        username = username.replaceAll("[\n\r]", "_");
        logger.info("Received getRequestsByUser for username: {}", username);
        Optional<User> user = userService.getUserByName(username);
        if (user.isEmpty()) {
            logger.warn("User not found: {}", username);
            return ResponseEntity.notFound().build();
        }
        List<ServiceRequest> list = serviceRequestService.getServiceRequestsByUser(user.get());
        logger.info("Found {} requests for user: {}", list.size(), username);
        return ResponseEntity.ok(list);
    }
}
