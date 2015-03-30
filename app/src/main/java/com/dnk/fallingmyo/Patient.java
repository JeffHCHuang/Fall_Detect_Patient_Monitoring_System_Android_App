package com.dnk.fallingmyo;

import java.util.HashMap;

/**
 * Created by JeffreyHao-Chan on 2015-03-14.
 */
public class Patient {

    private String name;
    private String accountID;
    private String email;

    private String roomNumber;

    private boolean emergency;
    private Assistance assistanceRequired;

    public Patient(HashMap<String, Object> patientData) {
        this.name = (String) patientData.get("name");
        this.accountID = getAccountID(patientData);
        this.email = (String) patientData.get("email");
        this.roomNumber = (String) patientData.get("roomNumber");
        this.emergency = (boolean) patientData.get("emergency");
        this.assistanceRequired = Assistance.valueOf((String) patientData.get("assistanceRequired"));
    }

    public Patient(String name, String accountID, String email,
                   String roomNumber, boolean Emergency, Assistance assistanceRequired) {
        this.name = name;
        this.accountID = accountID;
        this.email = email;

        this.roomNumber = roomNumber;

        this.emergency = Emergency;
        this.assistanceRequired = assistanceRequired;
    }

    public static String getAccountID(HashMap<String, Object> patientData) {
        return (String) patientData.get("accountID");
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountID() {
        return this.accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public boolean verify(String id) {
        if (id.equals(this.accountID)) {
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return this.roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean getEmergency() {
        return this.emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public Assistance getAssistanceRequired() {
        return this.assistanceRequired;
    }

    public void setAssistanceRequired(Assistance assistanceRequired) {
        this.assistanceRequired = assistanceRequired;
    }
}

