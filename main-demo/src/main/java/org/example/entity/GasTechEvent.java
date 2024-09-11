package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GasTechEvent {
    private String companyName;
    private String standNumber;
    private String countryName;
    private String address;
    private String websiteLink;
    private String description;
}
