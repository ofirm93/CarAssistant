package com.example.android.carassistant;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.android.carassistant.Constants.*;

public class ConversationFlow {
    private Activity context;
    private State state;
    private Request request;
    private Analysis analysis;
    private Response response;
    private TextToSpeech tts;

    public ConversationFlow(Activity context, IResolver engine, TextToSpeech tts) {
        this.context = context;
        this.state = new State();
        this.request = new Request();
        this.analysis = new Analysis(engine);
        this.response = new Response(tts);
        this.tts = tts;
    }

    public void initiate() {
        request.initiate();
        analysis.initiate(request, state);
        response.initiate(analysis, state);
    }

    public void request() {
        request.initiate();
    }

    public void analyze() {
        analysis.initiate(request, state);
    }

    public void respond() {
        response.initiate(analysis, state);
    }

    public void updateRequestResults(int resultCode, Intent data) {
        request.status = resultCode;
        request.data = data;
    }

    private class Request {
        private int status;
        private Intent data;

        public Request() {
            this.status = Activity.RESULT_CANCELED;
            this.data = null;
        }

        private void initiate() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, HEBREW_LOCALE_STR);
            intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{HEBREW_LOCALE_STR, "en-US"});

            try {
                context.startActivityForResult(intent, SPEECH_REQUEST_CODE);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(context.getApplicationContext(), "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Analysis {
        private IResolver<String[], Action> actionResolver;
        private Action action;

        public Analysis(IResolver<String[], Action> actionResolver) {
            this.actionResolver = actionResolver;
            this.action = null;
        }

        public Action getAction() {
            return action;
        }

        private void initiate(Request request, State state) { // read state to better understand the context of the request
            if (request.status == Activity.RESULT_OK && request.data != null) {
                ArrayList<String> text = request.data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String[] command = null;
                if (!text.isEmpty()){
                    command = text.get(0).split("\\s+");
                }
                analyze(command);
            }
        }

        private void analyze(String[] command) {
            if (command == null || command.length == 0) {
                action = SpeakAction.getMisunderstandingSpeakAction();
                return;
            }

            actionResolver.resolve(command);
            action = actionResolver.getResolved();

//            if (actionResolver != null) {
//                while (!actionResolver.isPrepared()) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                actionResolver.resolve(command);
//                action = actionResolver.getResolved();
//            }
        }

    }

    private class Response {
        private TextToSpeech tts;

        public Response(TextToSpeech tts) {
            this.tts = tts;
        }

        private void initiate(Analysis analysis, State state) {
            analysis.getAction().act(context, tts);

            // TODO change state to better reflect the current state for next flows
        }
    }

    private class State {

    }
}
