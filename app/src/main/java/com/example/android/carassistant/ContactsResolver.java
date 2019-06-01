package com.example.android.carassistant;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContactsResolver implements IResolver<String[], List<Contact>> {

    private static List<Contact> contacts = null;
    private static Map<String, List<String>> idToPhoneMap = null;
    private static boolean contactsArePrepared = false;
    private List<Contact> matchingContacts;


    public ContactsResolver() {
        this.matchingContacts = null;
    }

    @Override
    public void resolve(String[] name_range) {
        if (contacts == null || idToPhoneMap == null) {
            Log.i(Constants.TAG, "Couldn't resolve contacts because contacts or idToPhoneMap is not initialized yet.");
            Log.d(Constants.TAG, "contacts = " + contacts.toString());
            Log.d(Constants.TAG, "idToPhoneMap = " + idToPhoneMap.toString());
            this.matchingContacts = null;
        } else {
            ContactsResolver.prepareContacts();
            List<Contact> suggestions = evaluateSuggestions(name_range);
            if (!suggestions.isEmpty()) {
                matchingContacts = suggestions;
            }
        }
    }

    private static void prepareContacts() {
        if (!contactsArePrepared) {
            for (Contact contact : contacts) {
                contact.setPhoneNumbers(idToPhoneMap.get(contact.getId()));
            }
        }
        contactsArePrepared = true;
    }

    private List<Contact> evaluateSuggestions(String[] name){
        List<ScoredName> scoredNames = new ArrayList<>();
        for (Contact contact: contacts) {
            ScoredName scoredName = new ScoredName(contact, name);
            if (scoredName.getScore() > 0.5) {
                scoredNames.add(scoredName);
            }
        }
        Collections.sort(scoredNames);
        List<Contact> suggestions = new ArrayList<>();
        for (ScoredName scoredName:scoredNames) {
            suggestions.add(scoredName.getContact());
        }
        return suggestions;
    }

    private class ScoredName implements Comparable<ScoredName>{

        private Contact contact;
        private double score;

        public ScoredName(Contact contact, String[] comparedName) {
            this.contact = contact;
            this.score = calculateNameScore(comparedName);
        }

        private double calculateNameScore(String[] comparedNameArray){
            StringBuilder builder = new StringBuilder();
            for (String word:comparedNameArray){
                builder.append(word);
                builder.append(' ');
            }
            String comparedName = builder.toString();
            return similarity(comparedName, contact.getDisplayName());
        }

        private double similarity(String s1, String s2) {
            String longer = s1, shorter = s2;
            if (s1.length() < s2.length()) { // longer should always have greater length
                longer = s2; shorter = s1;
            }
            int longerLength = longer.length();
            if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
            return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
        }

        // Example implementation of the Levenshtein Edit Distance
        // See http://rosettacode.org/wiki/Levenshtein_distance#Java
        private int editDistance(String s1, String s2) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();

            int[] costs = new int[s2.length() + 1];
            for (int i = 0; i <= s1.length(); i++) {
                int lastValue = i;
                for (int j = 0; j <= s2.length(); j++) {
                    if (i == 0)
                        costs[j] = j;
                    else {
                        if (j > 0) {
                            int newValue = costs[j - 1];
                            if (s1.charAt(i - 1) != s2.charAt(j - 1))
                                newValue = Math.min(Math.min(newValue, lastValue),
                                        costs[j]) + 1;
                            costs[j - 1] = lastValue;
                            lastValue = newValue;
                        }
                    }
                }
                if (i > 0)
                    costs[s2.length()] = lastValue;
            }
            return costs[s2.length()];
        }

        @Override
        public int compareTo(@NonNull ScoredName other) {
            if (score - other.score > 0){
                return 1;
            }
            else if (score - other.score < 0){
                return -1;
            }
            return 0;
        }

        public Contact getContact() {
            return contact;
        }

        public double getScore() {
            return score;
        }
    }

    @Override
    public List<Contact> getResolved() {
        return matchingContacts;
    }

    public static void setContacts(List<Contact> contacts) {
        ContactsResolver.contacts = contacts;
    }

    public static void setIdToPhoneMap(Map<String, List<String>> idToPhoneMap) {
        ContactsResolver.idToPhoneMap = idToPhoneMap;
    }
}
