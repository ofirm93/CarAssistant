package com.example.android.carassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.ResultDeliveryType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements LoaderCallbacks{

    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session speechSession;
    private Transaction recoTransaction;
    private Transaction ttsTransaction;
    private State state = State.IDLE;

    private FloatingActionButton recordingButton;
    private TextView messageBox;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    Contacts._ID,
                    Contacts.LOOKUP_KEY,
                    Contacts.DISPLAY_NAME_PRIMARY

            };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the CONTACT_KEY column
    private static final int CONTACT_KEY_INDEX = 1;

    // An adapter that binds the result Cursor to the ListView
//    private SimpleCursorAdapter mCursorAdapter;

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Contacts.DISPLAY_NAME_PRIMARY
    };

    private static final String SELECTION =
            Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
    // Defines a variable for the search string
    private String mSearchString = "%אופיר%";
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };

    // Define a ListView object
    ListView mContactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    long mContactId;
    // The contact's LOOKUP_KEY
    String mContactKey;
    // A content URI for the selected contact
    Uri mContactUri;
    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter mCursorAdapter;

    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
//            logs.append("\nonStartedRecording");
            messageBox.setText("\nonStartedRecording");

            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
            startAudioLevelPoll();
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
//            logs.append("\nonFinishedRecording");

            messageBox.setText("\nonFinishedRecording");

            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
            stopAudioLevelPoll();
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
//            logs.append("\nonRecognition: " +
            messageBox.setText(recognition.getText());
            synthesize(recognition.getText());
            //We have received a transcription of the users voice from the server.
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
//            logs.append("\nonSuccess");

//            messageBox.set
//
// Text("\nonFinishedRecording");

            //Notification of a successful transaction.
            setState(State.IDLE);
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
//            logs.append("\nonError: " + e.getMessage() + ". " + s);

//            messageBox.setText(e.getMessage() + " " + e.getCode());

            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };
    private List<String> displayNames;

    /**
     * Stop recording the user
     */
    private void stopRecording() {
        recoTransaction.stopRecording();
    }

    /**
     * Cancel the Reco transaction.
     * This will only cancel if we have not received a response from the server yet.
     */
    private void cancel() {
        recoTransaction.cancel();
        setState(State.IDLE);
    }

    /* Audio Level Polling */

    private Handler handler = new Handler();

    /**
     * Every 50 milliseconds we should update the volume meter in our UI.
     */
    private Runnable audioPoller = new Runnable() {
        @Override
        public void run() {
            float level = recoTransaction.getAudioLevel();
//            volumeBar.setProgress((int)level);
            handler.postDelayed(audioPoller, 50);
        }
    };

    /**
     * Start polling the users audio level.
     */
    private void startAudioLevelPoll() {
        audioPoller.run();
    }

    /**
     * Stop polling the users audio level.
     */
    private void stopAudioLevelPoll() {
        handler.removeCallbacks(audioPoller);
//        volumeBar.setProgress(0);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        Uri contactsUri = Contacts.CONTENT_URI;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        CursorLoader contactsLoader = new CursorLoader(this, contactsUri, PROJECTION, selection, selectionArgs, sortOrder);
        return contactsLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        mCursorAdapter.swapCursor(cursor);
        displayNames = getAllDisplayNames(cursor);



    }

    private List<String> getAllDisplayNames(Cursor contactsCursor){
        // TODO check there is a column named <Contacts.DISPLAY_NAME>
        ArrayList<String> displayNames = new ArrayList<>();
        contactsCursor.moveToFirst();
        while(contactsCursor.moveToNext()){
            String displayName = contactsCursor.getString(contactsCursor.getColumnIndex(Contacts.DISPLAY_NAME));
            displayNames.add(displayName);
        }
        contactsCursor.moveToFirst();
        return displayNames;
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }


    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING,
        PLAYING,
        PAUSED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setActionBar(toolbar);

        recordingButton = (FloatingActionButton) findViewById(R.id.recodingButton);
        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Listening", Snackbar.LENGTH_LONG).show();
                toggleReco();
            }
        });

        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);

        messageBox = (TextView) findViewById(R.id.textView);

        loadEarcons();

        setState(State.IDLE);


        // TODO

        // Gets the ListView from the View list of the parent activity
        mContactsList =
                (ListView) findViewById(R.id.contacts_list);
        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.contacts_list_item,
                null,
                FROM_COLUMNS,
                TO_IDS,
                0);
        // Sets the adapter for the ListView
        mContactsList.setAdapter(mCursorAdapter);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        // Initializes the loader
        getSupportLoaderManager().initLoader(0, null, this);

    }

    /**
     * Speak the text that is in the ttsText EditText, using the language in the language EditText.
     */
    private void synthesize(String textToSpeak) {
        //Setup our TTS transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setLanguage(new Language(Configuration.LANGUAGE_CODE));
        //options.setVoice(new Voice(Voice.SAMANTHA)); //optionally change the Voice of the speaker, but will use the default if omitted.

        //Start a TTS transaction
        ttsTransaction = speechSession.speakString(textToSpeak, options, new Transaction.Listener() {
            @Override
            public void onAudio(Transaction transaction, Audio audio) {
//                logs.append("\nonAudio");

                //The TTS audio has returned from the server, and has begun auto-playing.
                ttsTransaction = null;
//                toggleTTS.setText(getResources().getString(R.string.speak_string));
            }

            @Override
            public void onSuccess(Transaction transaction, String s) {
//                logs.append("\nonSuccess");

                //Notification of a successful transaction. Nothing to do here.
            }

            @Override
            public void onError(Transaction transaction, String s, TransactionException e) {
//                logs.append("\nonError: " + e.getMessage() + ". " + s);

                //Something went wrong. Check Configuration.java to ensure that your settings are correct.
                //The user could also be offline, so be sure to handle this case appropriately.

                ttsTransaction = null;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        switch (state) {
            case IDLE:
                // Nothing to do since there is no ongoing recognition
                break;
            case LISTENING:
                // End the ongoing recording
                stopRecording();
                break;
            case PROCESSING:
                // End the ongoing recording and cancel the server recognition
                // This cancel request will generate an internal onError callback even if the server
                // returns a successful recognition.
                cancel();
                break;
        }
        super.onPause();
    }


    /* Reco transactions */
    private void toggleReco() {
        switch (state) {
            case IDLE:
                recognize();
                break;
            case LISTENING:
                stopRecording();
                break;
            case PROCESSING:
                cancel();
                break;
        }
    }

    /**
     * Start listening to the user and streaming their voice to the server.
     */
    private void recognize() {
        //Setup our Reco transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setRecognitionType(resourceIDToRecoType());
        options.setDetection(resourceIDToDetectionType());
        options.setLanguage(new Language(Configuration.LANGUAGE_CODE.toString()));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        //Start listening
        recoTransaction = speechSession.recognize(options, recoListener);
    }

    /**
     * Set the state and update the button text.
     */
    private void setState(State newState) {
        state = newState;
//        switch (newState) {
//            case IDLE:
//                toggleReco.setText(getResources().getString(R.string.recognize));
//                break;
//            case LISTENING:
//                toggleReco.setText(getResources().getString(R.string.listening));
//                break;
//            case PROCESSING:
//                toggleReco.setText(getResources().getString(R.string.processing));
//                break;
//        }
    }

    /* Earcons */

    private void loadEarcons() {
        //Load all the earcons from disk
        startEarcon = new Audio(this, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

    /* Helpers */

    private RecognitionType resourceIDToRecoType() {
        return RecognitionType.DICTATION;
    }

    private DetectionType resourceIDToDetectionType() {
        return DetectionType.Short;
    }

}
