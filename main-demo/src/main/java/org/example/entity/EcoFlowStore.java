package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EcoFlowStore {
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String phone;
    private String type;
    private String webLink;
}