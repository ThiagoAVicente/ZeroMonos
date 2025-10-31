package org.hw1.boundary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateServiceRequestDTO {
    private String user;           // User name or ID (depending on your lookup logic)
    private String municipality;   // Municipality name
    private String requestedDate;  // ISO date string (e.g., "2025-11-01")
    private String timeSlot;       // Time string (e.g., "10:00")
    private String description;
}