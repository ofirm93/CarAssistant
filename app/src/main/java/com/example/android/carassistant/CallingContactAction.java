package com.example.android.carassistant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;

public class CallingContactAction extends SpeakAction {

    private static final List<String> commandKeywords = Arrays.asList("תתקשר", "התקשר", "צלצל", "תצלצל");

    private List<Contact> contacts;

    private CallingContactAction(List<Contact> contacts) {
        super(generateCallText(contacts.get(0).getDisplayName()));
        this.contacts = contacts;
    }

    @Override
    public void act(@NonNull Context context, @NonNull TextToSpeech tts) {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals(Constants.UTTERANCE_ID)) {
                    call(context);
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        super.act(context, tts);

    }

    private void call(@NonNull Context context) {
        Intent intent = null;
        boolean foundPhoneNumber = false;
        int contactIndex = 0;
        while (!foundPhoneNumber && contactIndex < contacts.size()) {
            Contact contact = contacts.get(contactIndex);
            if (contact.hasPhoneNumber()) {
//                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contacts.get(contactIndex).getPhoneNumbers().get(0)));
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "0507634678"));
                foundPhoneNumber = true;
            }
            contactIndex++;
        }

        if (foundPhoneNumber) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent);
            } else {
                //   request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, Constants.CALL_PHONE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    static boolean isActionMatching(String[] command){
        return (command.length >= 2 && commandKeywords.contains(command[0]));
    }

    private static String generateCallText(String contactName) {
        return "מִתְקַשֶּׁרֶת אֵל " + contactName;
    }

    static Action getCallingContactAction(List<Contact> contacts){
        return new CallingContactAction(contacts);
    }
}
