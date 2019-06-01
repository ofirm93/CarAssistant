package com.example.android.carassistant;

import java.util.List;

public class Contact {
    private String id;
    private String displayName;
    private boolean hasPhoneNumber;
    private List<String> phoneNumbers;

    Contact(String id, String displayName, boolean hasPhoneNumber, List<String> phoneNumbers) {
        this.id = id;
        this.displayName = displayName;
        this.hasPhoneNumber = hasPhoneNumber;
        this.phoneNumbers = phoneNumbers;
    }

    String getId() {
        return id;
    }

    String getDisplayName() {
        return displayName;
    }

    public List<String> getPhoneNumbers() {
        if (hasPhoneNumber) {
            return phoneNumbers;
        }
        else {
            return null;
        }
    }

    boolean hasPhoneNumber() {
        return hasPhoneNumber;
    }

    void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
