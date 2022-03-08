package org.example.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class SurveyMember {
    private String address;
    private String address2;
    private String city;
    private String country;
    @EqualsAndHashCode.Exclude
    private String distance;
    private String email;
    private String fax;
    @EqualsAndHashCode.Exclude
    private String hours;
    private String id;
    @EqualsAndHashCode.Exclude
    private String lat;
    @EqualsAndHashCode.Exclude
    private String lng;
    // permanent link
    private String permalink;
    private String phone;
    private String state;
    private String store;
    private String url;
    private String zip;
}
