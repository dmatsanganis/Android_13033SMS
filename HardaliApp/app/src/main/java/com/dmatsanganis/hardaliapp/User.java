package com.dmatsanganis.hardaliapp;

public class User {

    public String fullname, email, address, reasonNumber, timestamp;
    public Double latitude, longitude;


    // Default Constructor.
    public User(){


    }

    // User object with 3 variables.
    public User(String fullname, String email, String address){
        this.fullname = fullname;
        this.email = email;
        this.address = address;
    }

    // User object with 4 variables.
    public User(String reasonNumber, Double latitude, Double longitude, String timestamp){
        this.reasonNumber = reasonNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // Getters and Setters.
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReasonNumber() {
        return reasonNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setReasonNumber(String reasonNumber) {
        this.reasonNumber = reasonNumber;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
