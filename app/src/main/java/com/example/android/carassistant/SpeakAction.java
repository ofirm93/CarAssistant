package com.example.android.carassistant;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class SpeakAction implements Action {
    private String text;
    private boolean isCaptioned;

    public SpeakAction(String text) {
        this(text, true);
    }

    public SpeakAction(String text, boolean isCaptioned) {
        this.text = text;
        this.isCaptioned = isCaptioned;
    }

    @Override
    public void act(@NonNull Context context, @NonNull TextToSpeech tts) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, Constants.UTTERANCE_ID);
        if (isCaptioned) {
            Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public static Action getMisunderstandingSpeakAction(){
        return new SpeakAction("מצטערת. לא הבנתי.");
    }

    public static Action getCantFindContactSpeakAction(){
        return new SpeakAction("מצטערת. לא מצאתי איש קשר מתאים.");
    }

}
