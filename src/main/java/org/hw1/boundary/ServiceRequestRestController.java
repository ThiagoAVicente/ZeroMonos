package org.hw1.boundary;

import org.hw1.service.ServiceRequestService;
import org.hw1.service.MunicipalityService;
import org.hw1.data.ServiceRequest;
import org.hw1.data.Municipality;
import org.hw1.data.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class ServiceRequestRestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private MunicipalityService municipalityService;

    // Criar nova solicitação de recolha
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody ServiceRequest request) {
        // TODO: Implementar
        return null;
    }

    // Consultar detalhes de uma reserva (por token)
    @GetMapping("/{token}")
    public ResponseEntity<?> getRequestByToken(@PathVariable String token) {
        // TODO: Implementar
        return null;
    }

    // Cancelar uma reserva
    @DeleteMapping("/{token}")
    public ResponseEntity<?> cancelRequest(@PathVariable String token) {
        // TODO: Implementar
        return null;
    }

    // Listar solicitações por município (Staff)
    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getRequestsByMunicipality(@RequestParam(required = false) String municipality) {
        // TODO: Implementar
        return null;
    }

    // Atualizar status de uma solicitação (Staff)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long id, @RequestBody Status status) {
        // TODO: Implementar
        return null;
    }

    // Listar municípios válidos
    @GetMapping("/municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities() {
        // TODO: Implementar
        return null;
    }
}
