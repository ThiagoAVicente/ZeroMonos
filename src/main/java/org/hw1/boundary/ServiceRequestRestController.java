package org.hw1.boundary;

import org.hw1.service.ServiceRequestService;
import org.hw1.service.MunicipalityService;
import org.hw1.data.ServiceRequest;
import org.hw1.data.Municipality;
import org.hw1.data.Status;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/requests")
public class ServiceRequestRestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private MunicipalityService municipalityService;

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody ServiceRequest request) {
        ServiceRequest created = serviceRequestService.createServiceRequest(
                request.getUser(),
                request.getMunicipality(),
                request.getRequestedDate(),
                request.getTimeSlot(),
                request.getDescription()
        );
        if (created == null) {
            return ResponseEntity.status(409).body("Request conflict or unavailable slot");
        }
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getRequestByToken(@PathVariable String token) {
        Optional<ServiceRequest> req = serviceRequestService.getServiceRequestByToken(token);
        if (req.isPresent()) {
            return ResponseEntity.ok(req.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<?> cancelRequest(@PathVariable String token) {
        try {
            serviceRequestService.cancelServiceRequest(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getRequestsByMunicipality(@RequestParam String municipality) {
        if (municipality == null) {
            return ResponseEntity.ok(List.of());
        }
        Optional<Municipality> mun = municipalityService.getMunicipalityByName(municipality);
        if (mun.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<ServiceRequest> list = serviceRequestService.getServiceRequestsByMunicipality(mun.get());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{token}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable String token, @RequestBody Status status) {
        try {
            serviceRequestService.updateServiceRequestStatus(token, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{token}/history")
    public ResponseEntity<List<ServiceStatusHistory>> getServiceStatusHistory(@PathVariable String token) {
        try {
            List<ServiceStatusHistory> history = serviceRequestService.getServiceStatusHistory(token);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities() {
        List<Municipality> list = municipalityService.getAllMunicipalities();
        return ResponseEntity.ok(list);
    }
}
