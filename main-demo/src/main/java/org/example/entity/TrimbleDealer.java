package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TrimbleDealer {
    private String name;
    // json: address, address2, city
    private String address;
    private String country;
    private String email;
    private String link;
    private String phone;
    private String priorityName;
}