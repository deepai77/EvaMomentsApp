package com.cindura.evamomentsapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.adapter.ShowCommandsAdapter;
import com.cindura.evamomentsapp.helper.Config;

import java.util.ArrayList;
import java.util.List;

//Activity which shows Show Commands page
public class ShowCommandsActivity extends AppCompatActivity implements
        RecognitionListener {
    private TextView userQueryTextView;
    private boolean callOnce;
    private Handler handlerMicrophone;
    private Runnable mRunnableMicrophone;
    private String listeningText="Say a command...";
    private String processingText="";
    private ImageView listening,progressBar;
    private TextView statusText;
    private int microphoneOnCount;
    private List<String> voiceCommands, evaResponses;
    private RecyclerView recyclerView;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private ImageView backButton;
    private Handler myHandler;
    private Runnable myRunnable;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String delayNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_commands);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recyclerViewTasks);
        userQueryTextView=findViewById(R.id.userQuery);

        listening = findViewById(R.id.listening);
        progressBar=findViewById(R.id.progress_loader);
        statusText = findViewById(R.id.statusText);
        voiceCommands = new ArrayList<>();
        evaResponses = new ArrayList<>();

        voiceCommands.add("Voice Command");
        voiceCommands.add("Show Commands");
        voiceCommands.add("List Shows");
        voiceCommands.add("Create Show");
        voiceCommands.add("Show Settings");
        voiceCommands.add("Quit");
        voiceCommands.add("Cancel");
        voiceCommands.add("Eva Community");
        voiceCommands.add("Play <Show name>");
        voiceCommands.add("Edit <Show name>");
        voiceCommands.add("Delete <Show name>");

        evaResponses.add("Eva Response");
        evaResponses.add("Voice commands supported by Eva");
        evaResponses.add("Displays the list of shows");
        evaResponses.add("Guide the user to create a show");
        evaResponses.add("Eva Settings and App Configuration");
        evaResponses.add("Closes the App");
        evaResponses.add("Cancels the current operation");
        evaResponses.add("Navigates to Eva Community site");
        evaResponses.add("Plays the show");
        evaResponses.add("Edits the show");
        evaResponses.add("Deletes the show");

        recyclerView.setLayoutManager(new LinearLayoutManager(ShowCommandsActivity.this, LinearLayoutManager.VERTICAL, false));

        ShowCommandsAdapter showCommandsAdapter = new ShowCommandsAdapter(voiceCommands, evaResponses, ShowCommandsActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(showCommandsAdapter);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        delayNavigation = pref.getString("navigationDelay", "");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        myHandler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        };
        myHandler.postDelayed(myRunnable, Long.parseLong(delayNavigation) * 1000);

        //Check if microphone on
        handlerMicrophone = new Handler();
        mRunnableMicrophone = new Runnable() {
            @Override
            public void run() {
                if (microphoneOnCount > 1) {
                    System.out.println("microphone turned on...");
                    microphoneOnCount = 0;
                    userQueryTextView.setVisibility(View.VISIBLE);
                    Animation pulse = AnimationUtils.loadAnimation(ShowCommandsActivity.this, R.anim.pulse);
                    listening.startAnimation(pulse);
                    ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(ShowCommandsActivity.this, R.color.green)));
                    listening.setVisibility(View.VISIBLE);
                    statusText.setText(listeningText);
                    resetSpeechRecognizer();
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }  else
                    microphoneOnCount++;
                handlerMicrophone.postDelayed(this, 5L * 1000L);
            }
        };
        handlerMicrophone.post(mRunnableMicrophone);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        myHandler.removeCallbacks(myRunnable);
        myHandler.postDelayed(myRunnable, Long.parseLong(delayNavigation) * 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }
        myHandler.removeCallbacks(myRunnable);
    }

    private void resetSpeechRecognizer() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i("Speech Recognizer", "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if (SpeechRecognizer.isRecognitionAvailable(this))
            mSpeechRecognizer.setRecognitionListener(this);
        else
            finishActivity();
    }

    private void setRecogniserIntent() {
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        callOnce = true;
        Log.d("Speech ", "onBeginingOfSpeech");
        microphoneOnCount = 0;
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Speech ", "onEndOfSpeech");
        mSpeechRecognizer.stopListening();
    }

    @Override
    public void onError(int error) {
        callOnce = true;
        String errorMessage = getErrorText(error);
        Log.d("Speech Show commands", "onError: " + errorMessage);
        resetSpeechRecognizer();
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                  if(mSpeechRecognizer != null)
                    mSpeechRecognizer.stopListening();
                message = "Recognition Service busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onResults(Bundle results) {
        if (callOnce) {
            callOnce = false;
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String userQuery = matches.get(0);
            if (!userQuery.equals(" ") && !userQuery.equals("")) {
                for (int i = 0; i < matches.size(); i++) {
                    if (matches.get(i).equalsIgnoreCase("Eva Community") || matches.get(i).equalsIgnoreCase("Quit") || matches.get(i).equalsIgnoreCase("Close") ||
                            matches.get(i).equalsIgnoreCase("Exit") || matches.get(i).equalsIgnoreCase("Cancel") || matches.get(i).equalsIgnoreCase("Settings") || matches.get(i).equalsIgnoreCase("create show") ||
                            matches.get(i).equalsIgnoreCase("create shows") ||
                            matches.get(i).equalsIgnoreCase("show settings") || matches.get(i).equalsIgnoreCase("show commands") ||
                            matches.get(i).equalsIgnoreCase("list shows") ||
                            matches.get(i).equalsIgnoreCase("list show") || matches.get(i).equalsIgnoreCase("list") ||
                            matches.get(i).equalsIgnoreCase("yes") || matches.get(i).equalsIgnoreCase("no") || matches.get(i).equalsIgnoreCase("help")) {
                        userQuery = matches.get(i);
                        break;
                    }
                }
                userQueryTextView.setText(userQuery);
                System.out.println("user query: " + userQuery);
                if(userQuery.equalsIgnoreCase("Eva Community")){
                    boolean isNetworkAvailable= isOffline(ShowCommandsActivity.this);
                    if(isNetworkAvailable)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SITE_URL)));
                    else
                        Toast.makeText(this, "Internet needed to open Eva Community", Toast.LENGTH_LONG).show();
                }
                else if (userQuery.equalsIgnoreCase("Quit") || userQuery.equalsIgnoreCase("Close") ||
                        userQuery.equalsIgnoreCase("Exit")) {
                    setInstructions("quit");
                } else if (userQuery.equalsIgnoreCase("cancel")) {
                    setInstructions("cancel");
                } else if (userQuery.equalsIgnoreCase("create show") || userQuery.equalsIgnoreCase("create shows") ||
                        userQuery.equalsIgnoreCase("create")) {
                    setInstructions("create");
                } else if (userQuery.equalsIgnoreCase("list shows") ||
                        userQuery.equalsIgnoreCase("list show") || userQuery.equalsIgnoreCase("list")) {
                    setInstructions("listShows");
                }
                else if (userQuery.toLowerCase().contains("play") && userQuery.contains(" ")) {
                    setInstructions(userQuery.trim());
                }else if (userQuery.toLowerCase().contains("delete") && userQuery.contains(" ")) {
                    setInstructions(userQuery.trim());
                } else if (userQuery.toLowerCase().contains("edit") && userQuery.contains(" ")) {
                    setInstructions(userQuery.trim());
                }
                else if (userQuery.toLowerCase().contains("show settings") || userQuery.equalsIgnoreCase("settings")|| userQuery.equalsIgnoreCase("setting")) {
                    stopSpeechRecognition();
                    if(handlerMicrophone!=null && mRunnableMicrophone!=null)
                        handlerMicrophone.removeCallbacks(mRunnableMicrophone);
                    Intent intent = new Intent(ShowCommandsActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            }
            if(mSpeechRecognizer!=null)
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        startSpeechRecognition();
        if(listening.getVisibility()==View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
            listening.setVisibility(View.INVISIBLE);
            statusText.setText(processingText);

            new CountDownTimer(1500, 1000) {
                public void onFinish() {
                    progressBar.setVisibility(View.INVISIBLE);
                    listening.setVisibility(View.VISIBLE);
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText(listeningText);
                }

                public void onTick(long millisUntilFinished) {
                    // millisUntilFinished    The amount of time until finished.
                }
            }.start();
        }
        handlerMicrophone.postDelayed(mRunnableMicrophone, 5L * 1000L);
        myHandler.postDelayed(myRunnable, Long.parseLong(delayNavigation) * 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(handlerMicrophone!=null && mRunnableMicrophone!=null)
            handlerMicrophone.removeCallbacks(mRunnableMicrophone);
        myHandler.removeCallbacks(myRunnable);
        stopSpeechRecognition();
    }

    void stopSpeechRecognition() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            listening.clearAnimation();
            statusText.setText(processingText);
            progressBar.setVisibility(View.VISIBLE);
            listening.setVisibility(View.INVISIBLE);
            mSpeechRecognizer=null;
        }
    }

    void startSpeechRecognition() {
        resetSpeechRecognizer();
        setRecogniserIntent();
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        userQueryTextView.setVisibility(View.VISIBLE);
        Animation pulse = AnimationUtils.loadAnimation(ShowCommandsActivity.this, R.anim.pulse);
        listening.startAnimation(pulse);
        ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(ShowCommandsActivity.this, R.color.green)));
        listening.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        statusText.setText(listeningText);
    }

    void setInstructions(String action) {
        Intent intent = new Intent(ShowCommandsActivity.this, MainActivity.class);
        intent.putExtra("Action", action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    void finishActivity(){
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }
        if(handlerMicrophone!=null && mRunnableMicrophone!=null)
            handlerMicrophone.removeCallbacks(mRunnableMicrophone);
        myHandler.removeCallbacks(myRunnable);
        finish();
    }
    public static boolean isOffline(Context thisActivity) {
        ConnectivityManager connMgr = (ConnectivityManager) thisActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return true;
        }
        return false;
    }

}