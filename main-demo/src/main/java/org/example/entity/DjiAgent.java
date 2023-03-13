package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class DjiAgent {
    private String continent;
    private String country;
    private String state;
    private String city;
    private String name;
    private String website;
    private String address;
    private String phone;
    private String email;
}
