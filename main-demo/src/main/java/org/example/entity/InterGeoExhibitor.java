package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InterGeoExhibitor {
    private String country;
    private String city;
    private String street;

    private String title;
    List<Stand> stands;

    private String email;
    private String mailContactPerson;
    private String description;
    private String phone;
    private String website;
    private String zipCode;

    public String stands2Str() {
        if (stands == null || stands.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stands.size() - 1; i++) {
            sb.append(stands.get(i));
            sb.append(System.getProperty("line.separator"));
        }
        sb.append(stands.get(stands.size() - 1));
        return sb.toString();
    }

    public static class Stand {
        public Stand(String id, String hall, String booth) {
            this.id = id;
            this.hall = hall;
            this.booth = booth;
        }

        String id;
        String hall;
        String booth;

        @Override
        public String toString() {
            return String.format("Hall: %s, Booth: %s", hall, booth);
        }
    }
}
