package com.example.android.carassistant;

public class Contact {
    private String id;
    private String displayName;
    private boolean hasPhoneNumber;
    private String phoneNumber;

    public Contact(String id, String displayName, boolean hasPhoneNumber, String phoneNumber) {
        this.id = id;
        this.displayName = displayName;
        this.hasPhoneNumber = hasPhoneNumber;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        if (hasPhoneNumber) {
            return phoneNumber;
        }
        else {
            return null;
        }
    }

    public boolean hasPhoneNumber() {
        return hasPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
