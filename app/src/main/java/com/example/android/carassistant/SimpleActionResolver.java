package com.example.android.carassistant;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleActionResolver implements IResolver<String[], Action> {

    private List<Contact> contacts;
    private Map<String, String> idToPhoneMap;
    private Action action;

    public SimpleActionResolver() {
        this.contacts = null;
        this.idToPhoneMap = null;
        this.action = null;
    }

    public Action getResolved() {
        return action;
    }

    public void resolve(String[] command) {
        if (CallingContactAction.isActionMatching(command)) {
            Log.i(Constants.TAG, "Matched to calling contact action.");
            String[] name_range;
            if (command[1].equals("אל") && command.length >= 3) {
                name_range = Arrays.copyOfRange(command, 2, command.length);
            } else if (command[1].charAt(0) == 'ל') {
                name_range = Arrays.copyOfRange(command, 1, command.length);
                name_range[0] = command[1].substring(1);
            } else {
                action = SpeakAction.getMisunderstandingSpeakAction();
                return;
            }

            Log.i(Constants.TAG, "The name range is: " + TextUtils.join(" ", name_range));

            ContactsResolver contactsResolver = new ContactsResolver();
            contactsResolver.resolve(name_range);

            Log.i(Constants.TAG, "Successfully resolved contact.");

            List<Contact> matchingContacts = contactsResolver.getResolved();
            if (matchingContacts == null || matchingContacts.size() == 0) {
                Log.i(Constants.TAG, "No matching contact found.");
                action = SpeakAction.getCantFindContactSpeakAction();
            } else {
                Log.i(Constants.TAG, "The first matching contact is: " + matchingContacts.get(0).getDisplayName());
                action = CallingContactAction.getCallingContactAction(matchingContacts);
            }

//            String[] name = {};
//            if (command[1].equals("אל") && command.length >= 3) {
//                name_range = Arrays.copyOfRange(command, 2, command.length);
//            } else if (command[1].charAt(0) == 'ל') {
//                name_range = Arrays.copyOfRange(command, 1, command.length);
//                name_range[0] = command[1].substring(1, command[1].length());
//            } else {
//                action = SpeakAction.getMisunderstandingSpeakAction();
//                return;
//            }
//
//            List<Contact> suggestions = evaluateSuggestions(name_range);
//            if (!suggestions.isEmpty()) {
//                action = CallingContactAction.getCallingContactAction(suggestions);
//            } else {
//                action = SpeakAction.getCantFindContactSpeakAction();
//            }
//            return;
        } else {
            Log.i(Constants.TAG, "Couldn't find matching action.");
            action = SpeakAction.getMisunderstandingSpeakAction();
        }
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

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setIdToPhoneMap(Map<String, String> idToPhoneMap) {
        this.idToPhoneMap = idToPhoneMap;
    }

    public boolean isPrepared() {
        if (contacts != null && idToPhoneMap != null) {
            fillContactsData();
            return true;
        }
        else {
            return false;
        }
    }

    public void prepare() {
        if (contacts != null && idToPhoneMap != null) {
            fillContactsData();
        }
    }

    private void fillContactsData() {

    }
}
