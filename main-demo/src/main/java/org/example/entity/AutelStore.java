package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutelStore {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
}
