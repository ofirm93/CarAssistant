package com.example.android.carassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
import android.widget.ListView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    private static final int RESULT_SPEECH = 0;
    private static final String HEBREW_LOCALE_STR = "he";
    private static final Locale HEBREW_LOCALE = new Locale(HEBREW_LOCALE_STR);
    private FloatingActionButton recordingButton;
    private TextView messageBox;

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

    // Define a ListView object
    private ListView mContactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    private long mContactId;
    // The contact's LOOKUP_KEY
    private String mContactKey;
    // A content URI for the selected contact
    private Uri mContactUri;
    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter mCursorAdapter;

    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    private Engine engine;
    private TextToSpeech mTts;


    private LoaderCallbacks phonesLoaderCallbacks = new LoaderCallbacks() {
        @NonNull
        @Override
        public Loader onCreateLoader(int id, @Nullable Bundle args) {
            Uri contactsUri = Phone.CONTENT_URI;
            String[] PROJECTION = {
                    Phone.CONTACT_ID,
                    Phone.NUMBER
            };

            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            CursorLoader contactsLoader = new CursorLoader(MainActivity.this, contactsUri, PROJECTION, selection, selectionArgs, sortOrder);
            return contactsLoader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader loader, Object data) {
            Cursor cursor = (Cursor) data;
            Map<String, String> idToPhonesMap = getIdToPhoneMap(cursor);
            if (engine != null) {
                engine.setIdToPhoneMap(idToPhonesMap);
            }
        }

        private Map<String, String> getIdToPhoneMap(Cursor phonesCursor) {
            // TODO check the requested columns exist

            Map<String, String> idToPhonesMap = new HashMap<>();
            phonesCursor.moveToFirst();
            String[] colNmaes = phonesCursor.getColumnNames();
            int idColumnIndex = phonesCursor.getColumnIndex(Phone.CONTACT_ID);
            int phoneColumnIndex = phonesCursor.getColumnIndex(Phone.NUMBER);

            while (phonesCursor.moveToNext()) {
                String id = phonesCursor.getString(idColumnIndex);
                String phone = phonesCursor.getString(phoneColumnIndex);

                idToPhonesMap.put(id, phone);
            }

            phonesCursor.moveToFirst();
            return idToPhonesMap;
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {

        }
    };

    private LoaderCallbacks contactsLoaderCallbacks = new LoaderCallbacks() {
        @NonNull
        @Override
        public Loader onCreateLoader(int id, @Nullable Bundle args) {
            Uri contactsUri = Contacts.CONTENT_URI;
            @SuppressLint("InlinedApi")
            String[] PROJECTION = {
                    Contacts._ID,
                    Contacts.LOOKUP_KEY,
                    Contacts.DISPLAY_NAME_PRIMARY,
                    Contacts.HAS_PHONE_NUMBER
            };
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            CursorLoader contactsLoader = new CursorLoader(MainActivity.this, contactsUri, PROJECTION, selection, selectionArgs, sortOrder);
            return contactsLoader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader loader, Object data) {
            Cursor cursor = (Cursor) data;
            mCursorAdapter.swapCursor(cursor);
            List<Contact> contacts = getAllContacts(cursor);
            if (engine != null) {
                engine.setContacts(contacts);
            }
        }


        private List<Contact> getAllContacts(Cursor contactsCursor) {
            // TODO check the requested columns exist
            ArrayList<Contact> contacts = new ArrayList<>();
            contactsCursor.moveToFirst();
            int idColumnIndex = contactsCursor.getColumnIndex(Contacts._ID);
            int displayNameColumnIndex = contactsCursor.getColumnIndex(Contacts.DISPLAY_NAME);
            int hasPhoneNumberColumnIndex = contactsCursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER);

            while (contactsCursor.moveToNext()) {
                String id = contactsCursor.getString(idColumnIndex);
                String displayName = contactsCursor.getString(displayNameColumnIndex);
                boolean hasPhoneNumber = contactsCursor.getInt(hasPhoneNumberColumnIndex) != 0;

                contacts.add(new Contact(id, displayName, hasPhoneNumber, null));
            }
            contactsCursor.moveToFirst();
            return contacts;
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {

        }
    };

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
                rocognizeCommand();
            }
        });

        messageBox = (TextView) findViewById(R.id.textView);


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

        // instatiate engine
        engine = new Engine();

        // Initializes the loader
        getSupportLoaderManager().initLoader(0, null, contactsLoaderCallbacks);

        getSupportLoaderManager().initLoader(1, null, phonesLoaderCallbacks);

        mTts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTts.setLanguage(HEBREW_LOCALE);

                }
            }
        });
    }

    private void rocognizeCommand() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, HEBREW_LOCALE_STR);
        intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{HEBREW_LOCALE_STR, "en-US"});

        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String command = null;
                    if (!text.isEmpty()){
                        command = text.get(0);
                    }
                    analyze(command);
                }
                break;
            }
        }
    }

    private void analyze(String command) {
        if (command == null) {
            String output = "מצטערת. לא הבנתי.";
            respond(output);
            return;
        }
        if (engine != null) {
            while (!engine.isPrepared()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            engine.parseCommand(command);
            List<Contact> suggestions = engine.getSuggestions();
            if (suggestions.isEmpty()) {
                String output = "מצטערת. לא מצאתי איש קשר מתאים.";
                respond(output);
            }
            else {
                String output = "מתקשרת אל " + suggestions.get(0).getDisplayName();
                act(); // TODO define interface and abstract class Action
                respond(output);
            }
        }
    }

    private void act() {

    }

    private void respond(String output) {
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
        mTts.speak(output, TextToSpeech.QUEUE_FLUSH, null, "1");
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

}
