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

@RestController
@RequestMapping("/requests")
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
    public ResponseEntity<?> createRequest(@RequestBody CreateServiceRequestDTO request) {
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
            return ResponseEntity.badRequest().body("Invalid user or municipality");
        }

        LocalDate requestedDate = null;
        LocalTime timeSlot = null;
        try {
            requestedDate = request.getRequestedDate() != null ? LocalDate.parse(request.getRequestedDate()) : null;
            timeSlot = request.getTimeSlot() != null ? LocalTime.parse(request.getTimeSlot()) : null;
        } catch (Exception e) {
            logger.warn("Invalid date or time format. Date: {}, Time: {}", request.getRequestedDate(), request.getTimeSlot());
            return ResponseEntity.badRequest().body("Invalid date or time format");
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
            return ResponseEntity.status(409).body("Request conflict or unavailable slot");
        }
        logger.info("ServiceRequest created successfully with token: {}", created.getToken());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getRequestByToken(@PathVariable String token) {
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
    public ResponseEntity<?> cancelRequest(@PathVariable String token) {
        logger.info("Received cancelRequest for token: {}", token);
        try {
            serviceRequestService.cancelServiceRequest(token);
            logger.info("ServiceRequest cancelled for token: {}", token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to cancel ServiceRequest for token: {}. Error: {}", token, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Cannot cancel")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getRequestsByMunicipality(@RequestParam String municipality) {
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
    public ResponseEntity<?> updateRequestStatus(@PathVariable String token, @RequestBody Status status) {
        logger.info("Received updateRequestStatus for token: {}, status: {}", token, status);
        try {
            serviceRequestService.updateServiceRequestStatus(token, status);
            logger.info("ServiceRequest status updated for token: {}", token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to update status for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{token}/history")
    public ResponseEntity<List<ServiceStatusHistory>> getServiceStatusHistory(@PathVariable String token) {
        logger.info("Received getServiceStatusHistory for token: {}", token);
        try {
            List<ServiceStatusHistory> history = serviceRequestService.getServiceStatusHistory(token);
            logger.info("Found {} status history entries for token: {}", history.size(), token);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Failed to get status history for token: {}. Error: {}", token, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities() {
        logger.info("Received getMunicipalities request");
        List<Municipality> list = municipalityService.getAllMunicipalities();
        logger.info("Found {} municipalities", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByUser(@PathVariable String username) {
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
