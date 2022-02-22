package org.example.entity;

public class Partner {
    // doc/metaFields/companyName
    private String companyName;

    // doc/description
    private String description;

    // doc/metaFields/specialty
    private String specialities;

    // doc/metaFields/solution-type
    private String solutionType;

    // doc/metaFields/partner-tier
    private String partnerTier;

    // doc/metaFields/services
    private String services;

    // doc/metaFields/business-classification
    private String businessClassification;

    // doc/metaFields/location
    private String location;

    private String contact;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecialities() {
        return specialities;
    }

    public void setSpecialities(String specialities) {
        this.specialities = specialities;
    }

    public String getSolutionType() {
        return solutionType;
    }

    public void setSolutionType(String solutionType) {
        this.solutionType = solutionType;
    }

    public String getPartnerTier() {
        return partnerTier;
    }

    public void setPartnerTier(String partnerTier) {
        this.partnerTier = partnerTier;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getBusinessClassification() {
        return businessClassification;
    }

    public void setBusinessClassification(String businessClassification) {
        this.businessClassification = businessClassification;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
