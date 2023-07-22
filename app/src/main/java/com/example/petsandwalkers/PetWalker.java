package com.example.petsandwalkers;

public class PetWalker {
    private int id;
    private String username;
    private String identity;
    private double latitude;
    private double longitude;
    private String serviceTimeRange;
    private String serviceLocation;
    private String price;
    private String phoneNumber;
    private String emailAddress;
    private String additionalInfo;

    public PetWalker(int id, String username, String identity, double latitude,
                     double longitude, String serviceTimeRange, String serviceLocation,String price,
                     String phoneNumber, String emailAddress, String additionalInfo) {
        this.id = id;
        this.username = username;
        this.identity = identity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.serviceTimeRange = serviceTimeRange;
        this.serviceLocation = serviceLocation;
        this.price = price;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.additionalInfo = additionalInfo;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getIdentity() {
        return identity;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getServiceTimeRange() {
        return serviceTimeRange;
    }

    public String getServiceLocation() {
        return serviceLocation;
    }
    public String getPrice() {
        return price;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}

