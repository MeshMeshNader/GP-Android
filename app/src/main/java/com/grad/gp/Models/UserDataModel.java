package com.grad.gp.Models;

import java.util.ArrayList;
import java.util.Map;

public class UserDataModel {
    String UserID;
    String Name;
    String email;
    String phoneNumber;
    String password;
    Map<String, String> encodings;
    Map<String, String> ImagesURLs;
    Map<String, String> personsData;

    public UserDataModel() {
    }

    public UserDataModel(String userID, String name, String email, String phoneNumber, String password, Map<String, String> encodings, Map<String, String> imagesURLs, Map<String, String> personsData) {
        UserID = userID;
        Name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.encodings = encodings;
        ImagesURLs = imagesURLs;
        this.personsData = personsData;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getEncodings() {
        return encodings;
    }

    public void setEncodings(Map<String, String> encodings) {
        this.encodings = encodings;
    }

    public Map<String, String> getImagesURLs() {
        return ImagesURLs;
    }

    public void setImagesURLs(Map<String, String> imagesURLs) {
        ImagesURLs = imagesURLs;
    }

    public Map<String, String> getPersonsData() {
        return personsData;
    }

    public void setPersonsData(Map<String, String> personsData) {
        this.personsData = personsData;
    }
}




