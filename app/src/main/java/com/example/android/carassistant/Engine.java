package com.example.android.carassistant;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Engine {
    private static List<String> commandKeywords = Arrays.asList("תתקשר", "התקשר", "צלצל", "תצלצל");
    private List<String> displayNames;
    private List<String> suggestions;

    public Engine(List<String> displayNames) {
        this.displayNames = displayNames;
        this.suggestions = new ArrayList<>();
        .
    }

    public boolean parseCommand(String command){
        String[] words = command.split("\\s+");
        if (words.length < 2){
            return false;
        }

        if (commandKeywords.contains(words[0])){
            String[] name;
            if (words[1].equals("אל") && words.length >= 3){
                name = Arrays.copyOfRange(words, 2, words.length);
            }
            else if (words[1].charAt(0) == 'ל'){
                name = Arrays.copyOfRange(words, 1, words.length);
                name[0] = words[1].substring(1, words[1].length());
            }
            else {
                return false;
            }
            suggestions = getSuggestions(name);
            return !suggestions.isEmpty();
        }

        return false;

    }

    private List<String> getSuggestions(String[] name){
        List<ScoredName> scoredNames = new ArrayList<>();
        for (String displayName:displayNames) {
            ScoredName scoredName = new ScoredName(displayName, name);
            if (scoredName.getScore() > 5) {
                scoredNames.add(scoredName);
            }
        }
        Collections.sort(scoredNames);
        List<String> suggestions = new ArrayList<>();
        for (ScoredName scoredName:scoredNames) {
            suggestions.add(scoredName.getName());
        }
        return suggestions;
    }

    private class ScoredName implements Comparable<ScoredName>{

        private String name;
        private double score;

        public ScoredName(String name, String[] comparedName) {
            this.name = name;
            this.score = calculateNameScore(comparedName);
        }

        private double calculateNameScore(String[] comparedNameArray){
            StringBuilder builder = new StringBuilder();
            for (String word:comparedNameArray){
                builder.append(word);
                builder.append(' ');
            }
            String comparedName = builder.toString();
            return similarity(comparedName, name);
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

        public String getName() {
            return name;
        }

        public double getScore() {
            return score;
        }
    }
}
