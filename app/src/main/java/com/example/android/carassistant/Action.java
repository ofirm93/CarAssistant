package com.example.android.carassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public interface Action {
    void act(Context context, TextToSpeech tts);

    static boolean isActionMatching(String[] command) {
        return false;
    }
}
