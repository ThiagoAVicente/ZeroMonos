package org.hw1;

import org.hw1.data.User;
import org.hw1.boundary.ServiceRequestRestController;
import org.hw1.boundary.ResourceNotFoundException;
import org.hw1.boundary.InvalidStatusTransitionException;
import org.hw1.data.Municipality;
import org.hw1.data.ServiceRequest;
import org.hw1.data.ServiceStatusHistory;
import org.hw1.data.Status;
import org.hw1.service.MunicipalityService;
import org.hw1.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.hw1.service.UserService;

@WebMvcTest(ServiceRequestRestController.class)
class ServiceRequestRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceRequestService serviceRequestService;

    @MockBean
    private MunicipalityService municipalityService;

    @MockBean
    private UserService userService;

    @Test
    void createServiceRequest_Success() throws Exception {
        User u = new User();
        Municipality m = new Municipality();

        when(userService.getUserByName(any())).thenReturn(Optional.of(u));
        when(municipalityService.getMunicipalityByName(any()))
                .thenReturn(Optional.of(m));

        ServiceRequest req = new ServiceRequest();
        when(serviceRequestService.createServiceRequest(any(), any(), any(), any(), anyString()))
                .thenReturn(req);

        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user\":\"validUser\",\"municipality\":\"Lisbon\",\"requestedDate\":\"2024-06-01\",\"timeSlot\":\"10:00\",\"description\":\"Colchão velho\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void createServiceRequest_Conflict() throws Exception {
        User u = new User();
        Municipality m = new Municipality();

        when(userService.getUserByName(any())).thenReturn(Optional.of(u));
        when(municipalityService.getMunicipalityByName(any()))
                .thenReturn(Optional.of(m));
        when(serviceRequestService.createServiceRequest(any(), any(), any(), any(), anyString()))
                .thenReturn(null);

        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user\":\"validUser\",\"municipality\":\"Lisbon\",\"requestedDate\":\"2024-06-01\",\"timeSlot\":\"10:00\",\"description\":\"Colchão velho\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getServiceRequestByToken_Found() throws Exception {
        ServiceRequest req = new ServiceRequest();
        when(serviceRequestService.getServiceRequestByToken("abc123")).thenReturn(Optional.of(req));

        mockMvc.perform(get("/requests/abc123"))
                .andExpect(status().isOk());
    }

    @Test
    void getServiceRequestByToken_NotFound() throws Exception {
        when(serviceRequestService.getServiceRequestByToken("notfound")).thenReturn(Optional.empty());

        mockMvc.perform(get("/requests/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelServiceRequest_Success() throws Exception {
        // Não lança exceção
        mockMvc.perform(delete("/requests/abc123"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelServiceRequest_NotFound() throws Exception {
        // Simula exceção
        Mockito.doThrow(new ResourceNotFoundException("Service request not found"))
                .when(serviceRequestService).cancelServiceRequest("notfound");

        mockMvc.perform(delete("/requests/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestsByMunicipality_ReturnsList() throws Exception {
        Municipality mun = new Municipality();
        when(municipalityService.getMunicipalityByName("Lisbon")).thenReturn(Optional.of(mun));

        mockMvc.perform(get("/requests?municipality=Lisbon"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestsByMunicipality_NotFound() throws Exception {
        when(municipalityService.getMunicipalityByName("Unknown")).thenReturn(Optional.empty());
        mockMvc.perform(get("/requests?municipality=Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateServiceRequestStatus_Success() throws Exception {
        mockMvc.perform(put("/requests/abc123/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"ASSIGNED\""))
                .andExpect(status().isOk());
    }

    @Test
    void updateServiceRequestStatus_NotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Service request not found"))
                .when(serviceRequestService).updateServiceRequestStatus(eq("notfound"), any(Status.class));

        mockMvc.perform(put("/requests/notfound/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"ASSIGNED\""))
                .andExpect(status().isNotFound());
    }

    @Test
    void getServiceStatusHistory_Success() throws Exception {
        when(serviceRequestService.getServiceStatusHistory("abc123")).thenReturn(List.of(new ServiceStatusHistory()));

        mockMvc.perform(get("/requests/abc123/history"))
                .andExpect(status().isOk());
    }

    @Test
    void getServiceStatusHistory_NotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Service request not found"))
                        .when(serviceRequestService).getServiceStatusHistory("notfound");
        mockMvc.perform(get("/requests/notfound/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMunicipalities_ReturnsList() throws Exception {
        when(municipalityService.getAllMunicipalities()).thenReturn(List.of(new Municipality()));

        mockMvc.perform(get("/requests/municipalities"))
                .andExpect(status().isOk());
    }
}
