package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DjiStore {
    private String city;
    private String name;
    private String address;
    private String openHours;
    private String phone;
    private String email;
}
