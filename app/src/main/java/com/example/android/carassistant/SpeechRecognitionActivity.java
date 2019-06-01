package com.example.android.carassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class SpeechRecognitionActivity extends Activity implements RecognitionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }



    @Override
    public void onBeginningOfSpeech() {
        // implement
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // implement
    }

    @Override
    public void onEndOfSpeech() {
        // implement
    }

    @Override

    public void onError(int errorCode) {

//        switch (errorCode) {
//
//            case SpeechRecognizer.ERROR_AUDIO:
//
//                message = R.string.error_audio_error;
//
//                break;
//
//            case SpeechRecognizer.ERROR_CLIENT:
//
//                message = R.string.error_client;
//
//                break;
//
//            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
//
//                message = R.string.error_permission;
//
//                break;
//
//            case SpeechRecognizer.ERROR_NETWORK:
//
//                message = R.string.error_network;
//
//                break;
//
//            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
//
//                message = R.string.error_timeout;
//
//                break;
//
//            case SpeechRecognizer.ERROR_NO_MATCH:
//
//                message = R.string.error_no_match;
//
//                break;
//
//            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
//
//                message = R.string.error_busy;
//
//                break;
//
//            case SpeechRecognizer.ERROR_SERVER:
//
//                message = R.string.error_server;
//
//                break;
//
//            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
//
//                message = R.string.error_timeout;
//
//                break;
//
//            default:
//
//                message = R.string.error_understand;
//
//                break;
//        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        // implement
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        // implement
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        // implement
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // implement
    }

}
