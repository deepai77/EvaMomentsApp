package com.cindura.evamomentsapp.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cindura.evamomentsapp.BuildConfig;
import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.adapter.GridAdapter;
import com.cindura.evamomentsapp.adapter.ListMomentsAdapter;
import com.cindura.evamomentsapp.adapter.SliderAdapterPhotoAlbum;
import com.cindura.evamomentsapp.helper.Config;
import com.cindura.evamomentsapp.helper.IRecyclerViewClickListener;
import com.cindura.evamomentsapp.helper.ItemMoveCallback;
import com.cindura.evamomentsapp.helper.Levenshtein;
import com.cindura.evamomentsapp.model.Presentation;
import com.cindura.evamomentsapp.model.PresentationList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

//The main home screen activity where a show can be created, modified and listed
public class MainActivity extends AppCompatActivity implements
        RecognitionListener{
    private int maxItemCount=20;
    private int levenshteinDistance=5;
    private RelativeLayout relative;
    private LinearLayout chips;
    private TextView chipsCancel,chipsYes,chipsNo,chipsExtra;
    private String noShowFound="No Shows found matching the search criteria...";
    //firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Handler handlerMicrophone, handlerFirebase;
    private Runnable mRunnableMicrophone, mRunnableFirebase;
    private String mimeType2;
    private TextView commands;
    private TextView keywordsTextView,preludeImageAvailable;
    private LinearLayout preludeAudioLayout, preludeImageLayout, mediaCountLayout,keywordsLayout;
    private ImageView preludeImage;
    private int MAX_SHOW_LIMIT=64;
    private Handler myHandler;
    private Runnable myRunnable;
    private CountDownTimer timer;
    private TextView matchCount;
    private LinearLayout detailsLayout;
    private TextView mediaCount2;
    private boolean callOnce;
    private String delayNavigation;
    private LinearLayout top;
    private ListMomentsAdapter listAdapter;
    private PresentationList presentationList;
    private List<Presentation> list,filteredList;
    private List<String> uris;
    private SpeechRecognizer mSpeechRecognizer;
    private VideoView videoView;
    private int currentPagePhotoAlbum;
    private Intent mSpeechRecognizerIntent;
    private ImageView progressBar;
    private TextToSpeech textToSpeech;
    private int microphoneOnCount;
    private boolean speechOff = true;
    private TextView userQueryTextView, statusText;
    private ImageView listening,add,backButton;
    private String noMoreKeywordsText="Say 'Done' or 'No more' to skip";
    private String showingCommandsText="";
    private String sayPlay="Say Play/Edit/Delete keyword";
    private String listeningText = "Say a command...";
   // private String processingText = "Not Ready...";
   private String processingText = "";
    private String saySomethingText = "Please say something...";
    private String dragText="Drag and drop to reorder the contents...";
    private int result22, speechVolumeProgress, mediaPlayerProgress, pitch, speed;
    private AudioManager am;
    private HashMap<String, String> map = new HashMap<String, String>();
    private List<Uri> selectedItems;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> keywords;
    private String playPrelude;
    private boolean showCommandsActivated,settingsActivated,playClicked,createCalledFromAdd,keywordsEnabled, askSelfPhoto, changeOrder,askEdit,commandsList,askKeywordsAgain,addMoreState,anotherPresentation,askList, showingMoment,videoCompleted;
    private int countKeywords;
    private TextView timerText, keyword1, keyword2, keyword3, done;
    private ImageView audioAttached, selfPic;
    private MediaRecorder mediaRecorder;
    String AudioSavePathInDevice = null;
    private GridAdapter adapter;
    private SliderAdapterPhotoAlbum sliderAdapterPhotoAlbum;
    private Uri selfPicUri;
    private MediaPlayer mediaPlayer;
    private String editPosition;
    private String playingFrom;
    private int showPostion;
    private String action;
    private boolean onCreateCalled;

    private CountDownTimer countDownTimerListMoments,counter,counterShowDetails,counterCommands,autoSwipeCounter;
    private String familyPicUri,firstLaunch,name;

    //SharedPreference
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        Intent intent=getIntent();
        action= intent.getStringExtra("Action");

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        firstLaunch=pref.getString("firstLaunch",null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        list=new ArrayList<>();
        filteredList=new ArrayList<>();
        presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
        familyPicUri=pref.getString("familyPic",null);
        name=pref.getString("name",null);
        if(name.contains(" ")){
            name=name.substring(0,name.indexOf(" "));
        }
        if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
        list=presentationList.getPresentationList();

        playPrelude="";
        selectedItems = new ArrayList<>();
        keywords = new ArrayList<>();
        timerText = findViewById(R.id.timer);
        uris=new ArrayList<>();
        add=findViewById(R.id.add);
        userQueryTextView = findViewById(R.id.userQuery);
        listening = findViewById(R.id.listening);
        chips=findViewById(R.id.chips);
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progress_loader);
        backButton=findViewById(R.id.backButton);
        done = findViewById(R.id.done);
        chipsCancel=findViewById(R.id.chipsCancel);
        chipsYes=findViewById(R.id.chipsYes);
        chipsNo=findViewById(R.id.chipsNo);
        chipsExtra=findViewById(R.id.chipsExtra);

        displayImage();

        if(action!=null && !action.equalsIgnoreCase(""))
        {
            speechOff = false;
            textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        result22 = textToSpeech.setLanguage(Locale.US);
                    }
                }
            });
            if(action.equalsIgnoreCase("quit")){
                new CountDownTimer(2000, 1000) {
                    public void onFinish() {
                        quitFunction();
                    }
                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
            }else if(action.equalsIgnoreCase("create")){
                imageVideoPicker();
            }
            else if(action.equalsIgnoreCase("listShows")){
                listMoments();
            }
            else if(action.equalsIgnoreCase("cancel")){
                displayImage();
            }
            else if(action.toLowerCase().contains("play")){
                String s = action.substring(action.lastIndexOf("play") + 5).trim();
                playShow(s);
            }
            else if(action.toLowerCase().contains("delete")){
                String s = action.substring(action.lastIndexOf("delete") + 7).trim();
                deleteShow(s);
            }
            else if(action.toLowerCase().contains("edit")){
                String s = action.substring(action.lastIndexOf("edit") + 5).trim();
                editShow(s);
            }
        }
            else
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    result22 = textToSpeech.setLanguage(Locale.US);
                    mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (mediaPlayerProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                    if(firstLaunch==null) {
                        String s=getResources().getString(R.string.welcome);
                        s=s.replace("!",name+"!");
                        userQueryTextView.setText(s);
                        textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
                        editor.putString("firstLaunch", "true");
                        editor.apply();
                    }else if(list!=null && !list.isEmpty()){
                        String s=getResources().getString(R.string.list);
                        s=s.replace("Stu",name);
                        userQueryTextView.setText(s);
                        textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
                        askList=true;
                    }else{
                        String s=getResources().getString(R.string.would_you);
                        s=s.replace("Would","Hello "+name+"! Would");
                        userQueryTextView.setText(s);
                        textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
                        anotherPresentation=true;
                    }
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            speechVolumeProgress = pref.getInt("speechVolume", 0);
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, (speechVolumeProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    speechOff = false;
                                    startSpeechRecognition();
                                    if(firstLaunch==null) {
                                        firstLaunch = "";
                                        imageVideoPicker();
                                    }
                                  else if(askList){
                                        chips.setVisibility(View.VISIBLE);
                                    }else if(anotherPresentation){
                                        chips.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Speech feature not supported.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        pitch = pref.getInt("pitch", 0);
        speed = pref.getInt("speed", 0);
        textToSpeech.setPitch((float) pitch / 100);
        textToSpeech.setSpeechRate((float) speed / 100);

        //Check if microphone on
        handlerMicrophone = new Handler();
        mRunnableMicrophone = new Runnable() {
            @Override
            public void run() {
                if (microphoneOnCount > 1 && !speechOff) {
                    System.out.println("microphone turned on...");
                    microphoneOnCount = 0;
                    resetSpeechRecognizer();
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

                    if(action!=null && !action.equalsIgnoreCase("")){   //|| showCommandsActivated || settingsActivated ) {
                        action="";
                        showCommandsActivated=false;
                        settingsActivated=false;
                        Animation pulse = AnimationUtils.loadAnimation(MainActivity.this, R.anim.pulse);
                        listening.startAnimation(pulse);
                        ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
                        listening.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(listeningText);
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                    statusText.setText(listeningText);
                } else if (speechOff) {
                    microphoneOnCount = 0;
                    System.out.println("Request processing...");
                } else
                    microphoneOnCount++;
                handlerMicrophone.postDelayed(this, 5L * 1000L);
            }
        };
        handlerMicrophone.post(mRunnableMicrophone);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //Firebase
        handlerFirebase = new Handler();
        mRunnableFirebase = new Runnable() {
            @Override
            public void run() {
                // Update an existing document
                String imei=pref.getString("imei",null);
                boolean isNetworkAvailable= isOffline(MainActivity.this);
                if(imei!=null && isNetworkAvailable) {
                   new FirebaseTask(imei).execute();
                }
                handlerFirebase.postDelayed(this, 60L * 1000L);
            }
        };
        handlerFirebase.post(mRunnableFirebase);

        chipsExtra.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                askEdit=true;
                addMoreState=true;
                imageVideoPicker();
                return false;
            }
        });
        chipsCancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chips.setVisibility(View.GONE);
                userQueryTextView.setText(showingCommandsText);
                if(chipsCancel.getText().equals("Cancel")) {
                    if (createCalledFromAdd) {
                        createCalledFromAdd = false;
                        commandsList = false;
                        reset();
                        listMoments();
                    } else {
                        reset();
                        removeLayout();
                        displayImage();
                    }
                }else{
                    showingMoment = true;
                    playClicked = true;
                    speechOff = true;
                    if (countDownTimerListMoments != null)
                        countDownTimerListMoments.cancel();
                    if (counterShowDetails != null)
                        counterShowDetails.cancel();
                    Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                    intent.putExtra("speaker", "true");
                    intent.putExtra("position", Integer.parseInt(playPrelude));
                    startActivity(intent);
                }
                return false;
            }
        });
        chipsYes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chips.setVisibility(View.GONE);
                if(chipsYes.getText().equals("Done") && (top!=null && top.getVisibility()==View.VISIBLE)){
                    editDone();
                }
                else if(chipsYes.getText().equals("Done")){
                    chipsYes.setText("Yes");
                    statusText.setText(processingText);
                    adapter = new GridAdapter(MainActivity.this, selectedItems);
                    recyclerView.setAdapter(adapter);
                    nextStep(getResources().getString(R.string.ask_keywords));
                }else {
                    if (askList) {
                        askList = false;
                        nextStep(getResources().getString(R.string.great));
                    } else if (askSelfPhoto) {
                        askSelfPhoto = false;
                        Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 2);

                    } else if (changeOrder) {
                        changeOrder = false;
                        adapter = new GridAdapter(MainActivity.this, selectedItems);
                        ItemTouchHelper.Callback callback =
                                new ItemMoveCallback(adapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(recyclerView);
                        recyclerView.setAdapter(adapter);

                        done.setVisibility(View.GONE);
                        chips.setVisibility(View.VISIBLE);
                        chipsYes.setText("Done");
                        chipsNo.setVisibility(View.GONE);

                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(dragText);
                    } else if (askKeywordsAgain) {
                        askKeywordsAgain = false;
                        if (detailsLayout != null) {
                            detailsLayout.setVisibility(View.GONE);
                            keywordsLayout.setVisibility(View.GONE);
                            preludeImageLayout.setVisibility(View.GONE);
                            mediaCountLayout.setVisibility(View.GONE);
                            preludeAudioLayout.setVisibility(View.GONE);
                            preludeImage.setVisibility(View.GONE);
                            preludeImageAvailable.setVisibility(View.GONE);
                        }
                        selfPic.setVisibility(View.GONE);
                        selfPicUri = null;
                        keyword1.setVisibility(View.GONE);
                        keyword2.setVisibility(View.GONE);
                        keyword3.setVisibility(View.GONE);
                        audioAttached.setVisibility(View.GONE);
                        if (top != null && top.getVisibility()==View.VISIBLE) {
                            top.setVisibility(View.GONE);
                            statusText.setText(processingText);
                            adapter = new GridAdapter(MainActivity.this, selectedItems);
                            recyclerView.setAdapter(adapter);
                        }
                        nextStep(getResources().getString(R.string.ask_keywords));
                    } else if (askEdit) {
                        speechOff = false;
                        startSpeechRecognition();
                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(dragText);
                        new LoadGridTask(askEdit).execute();
                    } else if (anotherPresentation) {
                        anotherPresentation = false;
                        commandsList = false;
                        speechOff = false;
                        removeLayout();
                        displayImage();
                        imageVideoPicker();
                    }
                }
                return false;
            }
        });
        chipsNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chips.setVisibility(View.GONE);
                if(chipsNo.getText().equals("Save")){
                    if(!selectedItems.isEmpty()) {
                        askEdit = false;
                        addMoreState=false;
                        top.setVisibility(View.GONE);
                        adapter = new GridAdapter(MainActivity.this, selectedItems);
                        recyclerView.setAdapter(adapter);
                        afterEdit();
                    }else{
                        Toast.makeText(MainActivity.this, "Empty album. Please add images/videos", Toast.LENGTH_LONG).show();
                    }
                }else {
                    if (askList) {
                        askList = false;
                    } else if (askSelfPhoto) {
                        askSelfPhoto = false;
                        preludeImageLayout.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setText("Not Available");
                        if (editPosition != null && !editPosition.equalsIgnoreCase(""))
                            nextStep(getResources().getString(R.string.congratulations_modified));
                        else
                            nextStep(getResources().getString(R.string.congratulations));

                    } else if (changeOrder) {
                        changeOrder = false;
                        nextStep(getResources().getString(R.string.ask_keywords));
                    } else if (askKeywordsAgain) {
                        askKeywordsAgain = false;
                        if (top != null && top.getVisibility()==View.VISIBLE) {
                            top.setVisibility(View.GONE);
                            statusText.setText(processingText);
                            adapter = new GridAdapter(MainActivity.this, selectedItems);
                            recyclerView.setAdapter(adapter);
                        }
                        if (editPosition != null && !editPosition.equalsIgnoreCase(""))
                            nextStep(getResources().getString(R.string.congratulations_modified));
                        else
                            nextStep(getResources().getString(R.string.congratulations));
                    } else if (askEdit) {
                        askEdit = false;
                        afterEdit();
                    } else if (anotherPresentation) {
                        anotherPresentation = false;
                        commandsList = false;
                        userQueryTextView.setText("");
                    }
                }
                return false;
            }
        });
        done.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                done.setVisibility(View.GONE);
                statusText.setText(processingText);
                adapter = new GridAdapter(MainActivity.this, selectedItems);
                recyclerView.setAdapter(adapter);
                nextStep(getResources().getString(R.string.ask_keywords));
                return false;
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
                    list=presentationList.getPresentationList();

                if(list==null || list.isEmpty() || list.size()<MAX_SHOW_LIMIT) {
                    removeLayout();
                    displayImage();
                    reset();
                    createCalledFromAdd = true;
                    imageVideoPicker();
                }else{
                    Toast.makeText(MainActivity.this, "Cannot Create more than "+MAX_SHOW_LIMIT+" shows.", Toast.LENGTH_LONG).show();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playPrelude.equalsIgnoreCase("")){
                    listMoments();
                }else {
                    reset();
                    removeLayout();
                    userQueryTextView.setText(showingCommandsText);
                    displayImage();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(settingsActivated){
            settingsActivated=false;
            speechOff=false;
            familyPicUri=pref.getString("familyPic",null);
            removeLayout();
            displayImage();
            userQueryTextView.setText(showingCommandsText);
        }
        if(showingMoment && playClicked){
            showingMoment=false;
            playClicked=false;
            speechOff=false;
            if(playingFrom!=null && !playingFrom.equalsIgnoreCase("") &&playingFrom.equals("list")){
                playingFrom="";
                listMoments();
            }
            else if(playingFrom!=null && !playingFrom.equalsIgnoreCase("") && playingFrom.equals("show")){
                playingFrom="";
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
                    list=presentationList.getPresentationList();

                showMomentDetails(showPostion,list);
            }else
            counterShowDetails= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                public void onFinish() {
                    if(progressBar.getVisibility()==View.INVISIBLE)
                    statusText.setText(listeningText);
                    new LoadMoments(selectedItems).execute();
                }
                public void onTick(long millisUntilFinished) {
                }
            }.start();
        }else if(showingMoment){
            showingMoment=false;
            speechOff=false;
            if(playingFrom!=null && !playingFrom.equalsIgnoreCase("") &&playingFrom.equals("list")){
                playingFrom="";
                listMoments();
            }
            else if(playingFrom!=null && !playingFrom.equalsIgnoreCase("") && playingFrom.equals("show")){
                playingFrom="";
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
                    list=presentationList.getPresentationList();

                showMomentDetails(showPostion,list);
            }
            else
            countDownTimerListMoments= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                public void onFinish() {
                    removeLayout();
                    reset();
                    if(progressBar.getVisibility()==View.INVISIBLE)
                    statusText.setText(listeningText);
                    userQueryTextView.setText(showingCommandsText);
                    displayImage();
                }
                public void onTick(long millisUntilFinished) {
                }
            }.start();
        }else if(showCommandsActivated){
            showCommandsActivated=false;
            speechOff=false;
        }
        pitch=pref.getInt("pitch", 0);
        speed=pref.getInt("speed",0);
        speechVolumeProgress=pref.getInt("speechVolume",0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,(speechVolumeProgress*am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/100, 0);

        textToSpeech.setPitch((float)pitch/100);
        textToSpeech.setSpeechRate((float) speed/100);
        textToSpeech.speak("" , TextToSpeech.QUEUE_FLUSH, null, null);

        startSpeechRecognition();
        if(listening.getVisibility()==View.VISIBLE && !speechOff){
            progressBar.setVisibility(View.VISIBLE);
            listening.setVisibility(View.INVISIBLE);
            statusText.setVisibility(View.GONE);

            new CountDownTimer(1500, 1000) {
                public void onFinish() {
                    progressBar.setVisibility(View.INVISIBLE);
                    listening.setVisibility(View.VISIBLE);
                    statusText.setVisibility(View.VISIBLE);
                }

                public void onTick(long millisUntilFinished) {
                    // millisUntilFinished    The amount of time until finished.
                }
            }.start();
        }
            handlerMicrophone.postDelayed(mRunnableMicrophone, 5L * 1000L);
        if(onCreateCalled) {
            handlerFirebase.postDelayed(mRunnableFirebase, 60L * 1000L);
        }
        onCreateCalled=true;
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void resetSpeechRecognizer() {
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i("Speech Recognizer", "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if (SpeechRecognizer.isRecognitionAvailable(this))
            mSpeechRecognizer.setRecognitionListener(this);
        else
            finish();
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

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Checking the request code of our request
        if (requestCode == 100) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if(!askEdit)
                    selectedItems.clear();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        int currentItem = 0;
                        while (currentItem < count) {
                            Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                            final int takeFlags = data.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                            try {
                                MainActivity.this.getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            if(selectedItems!=null && selectedItems.size()<=maxItemCount-1) {
                                selectedItems.add(imageUri);
                                currentItem = currentItem + 1;
                            }
                            else {
                                currentItem=count;
                            }

                            if (currentItem == count) {
                                if (!askEdit) {
                                    new LoadGridTask().execute();
                                } else {
                                    new LoadGridTask(askEdit).execute();
                                }
                            }
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        try {
                            MainActivity.this.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                        catch (SecurityException e){
                            e.printStackTrace();
                        }
                       if(selectedItems!=null && selectedItems.size()<=maxItemCount-1)
                        selectedItems.add(uri);

                        if(!askEdit)
                        new LoadGridTask().execute();
                        else
                            new LoadGridTask(askEdit).execute();
                    }
                }
                break;

            case 2:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        MainActivity.this.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }
                    preludeImageLayout.setVisibility(View.VISIBLE);
                    preludeImage.setVisibility(View.VISIBLE);
                    Cursor returnCursor =
                            getContentResolver().query(uri, null, null, null, null);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    long size = returnCursor.getLong(sizeIndex);

                    System.out.println("file size: "+size);
                    if(size < 100000) {
                        preludeImage.setImageURI(uri);
                    }
                    else
                       preludeImage.setImageBitmap(getBitmap(uri));
                    selfPicUri=uri;

                    new CountDownTimer(3000, 1000) {
                        public void onFinish() {
                            if(editPosition!=null && !editPosition.equalsIgnoreCase(""))
                                nextStep(getResources().getString(R.string.congratulations_modified));
                                else
                            nextStep(getResources().getString(R.string.congratulations));
                        }

                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start();

                }
                break;
        }
    }

    void editDone(){
        if(!selectedItems.isEmpty()) {
            if(timer !=null)
            {
                timer.cancel();
            }
            top.setVisibility(View.GONE);
            adapter = new GridAdapter(MainActivity.this, selectedItems);
            recyclerView.setAdapter(adapter);
            if(mediaCount2!=null) {
                if(String.valueOf(selectedItems.size()).equalsIgnoreCase(String.valueOf(maxItemCount))){
                    mediaCount2.setText(String.valueOf(selectedItems.size())+" (Maximum Items)");
                }else
                    mediaCount2.setText(String.valueOf(selectedItems.size()));
            }
            askEdit = false;
            addMoreState=false;
            nextStep(getResources().getString(R.string.ask_edit_keywords));

            if(selectedItems!=null && selectedItems.size()==maxItemCount)
                Toast.makeText(MainActivity.this, "Maximum "+maxItemCount+" Items can be added", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Empty album. Please add images/videos", Toast.LENGTH_LONG).show();
        }
    }
    public class LoadGridTask extends AsyncTask<Void, Void, Void> {

        private boolean askEdit;

        public LoadGridTask() {

        }
        public LoadGridTask(boolean askEdit) {
            this.askEdit=askEdit;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeLayout();
                        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                                .LAYOUT_INFLATER_SERVICE);
                        View v = vi.inflate(R.layout.grid_response, null);

                        System.out.println("s items : " + selectedItems.size());
                        adapter = new GridAdapter(MainActivity.this, selectedItems,askEdit);
                        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
                        selfPic = v.findViewById(R.id.selfPic);
                        keyword1 = v.findViewById(R.id.key1);
                        keyword2 = v.findViewById(R.id.key2);
                        keyword3 = v.findViewById(R.id.key3);
                        audioAttached = v.findViewById(R.id.audioAttached);
                        layoutManager = new GridLayoutManager(MainActivity.this, 3);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);
                        commands=v.findViewById(R.id.commands);

                        detailsLayout=v.findViewById(R.id.details);
                        keywordsTextView=v.findViewById(R.id.keywords);
                        preludeImageAvailable=v.findViewById(R.id.preludeImageAvailability);
                        preludeImage=v.findViewById(R.id.preludeImagePic);
                        mediaCount2=v.findViewById(R.id.mediaCount);
                        preludeImageLayout=v.findViewById(R.id.preludeImageLayout);
                        preludeAudioLayout=v.findViewById(R.id.preludeAudioLayout);
                        mediaCountLayout=v.findViewById(R.id.mediaCountLayout);
                        keywordsLayout=v.findViewById(R.id.keywordsLayout);

                        if(askEdit) {
                            userQueryTextView.setText("");
                            chipsCancel.setVisibility(View.VISIBLE);
                            chipsExtra.setVisibility(View.VISIBLE);
                            chipsYes.setVisibility(View.VISIBLE);
                            chipsNo.setVisibility(View.VISIBLE);
                            chipsCancel.setText("Cancel");
                            chipsExtra.setText("Add more");
                            chipsYes.setText("Done");
                            chipsNo.setText("Save");
                            chips.setVisibility(View.VISIBLE);
                            changeOrder=false;
                            if(detailsLayout.getVisibility()!=View.VISIBLE)
                            detailsLayout.setVisibility(View.GONE);
                            preludeAudioLayout.setVisibility(View.VISIBLE);
                            preludeImageLayout.setVisibility(View.VISIBLE);
                            mediaCountLayout.setVisibility(View.VISIBLE);
                            keywordsLayout.setVisibility(View.VISIBLE);
                            ItemTouchHelper.Callback callback =
                                    new ItemMoveCallback(adapter);
                            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                            touchHelper.attachToRecyclerView(recyclerView);

                            final ImageView done=v.findViewById(R.id.done);
                            final ImageView addMore=v.findViewById(R.id.addMore);
                            ImageView cancelEdit=v.findViewById(R.id.cancelInTab);
                            ImageView cancel=v.findViewById(R.id.cancel);
                            top=v.findViewById(R.id.top);

                            top.setVisibility(View.VISIBLE);

                            String size= String.valueOf(selectedItems.size());
                            if(size.equalsIgnoreCase(String.valueOf(maxItemCount))){
                                mediaCount2.setText(size+" (Maximum Items)");
                            }else
                            mediaCount2.setText(size);
                            String key="";
                            for(int i=0;i<keywords.size();i++){
                                key=key+keywords.get(i)+", ";
                            }
                            keywordsTextView.setText(key.substring(0,key.length()-2));

                            ContentResolver cR2 = getContentResolver();
                            if(selfPicUri!=null)
                            mimeType2=cR2.getType(selfPicUri);
                            if(selfPicUri!=null && mimeType2!=null){
                                preludeImage.setVisibility(View.VISIBLE);
                                Cursor returnCursor =
                                        getContentResolver().query(selfPicUri, null, null, null, null);
                                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                                returnCursor.moveToFirst();
                                long size2 = returnCursor.getLong(sizeIndex);

                                System.out.println("file size: " + size);
                                if (size2 < 100000) {
                                    preludeImage.setImageURI(selfPicUri);
                                } else
                                    preludeImage.setImageBitmap(getBitmap(selfPicUri));
                            }else{
                                preludeImageAvailable.setVisibility(View.VISIBLE);
                                preludeImageAvailable.setText("Not Available");
                            }

                            cancelEdit.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    speechOff = false;
                                    startSpeechRecognition();
                                        reset();
                                        listMoments();
                                    return false;
                                }
                            });
                            addMore.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    askEdit=true;
                                    addMoreState=true;
                                    imageVideoPicker();
                                    return false;
                                }
                            });

                            done.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                 editDone();
                                    return false;
                                }
                            });
                            cancel.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if(!selectedItems.isEmpty()) {
                                        askEdit = false;
                                        addMoreState=false;
                                        top.setVisibility(View.GONE);
                                        adapter = new GridAdapter(MainActivity.this, selectedItems);
                                        recyclerView.setAdapter(adapter);
                                       afterEdit();
                                    }else{
                                        Toast.makeText(MainActivity.this, "Empty album. Please add images/videos", Toast.LENGTH_LONG).show();
                                    }
                                    return false;
                                }
                            });
                        }
                        recyclerView.setAdapter(adapter);

                        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
                        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                        timer=new CountDownTimer(2000, 1000) {
                            public void onFinish() {
                                if(!askEdit) {
                                    if (selectedItems.size() > 1) {
                                        changeOrder = true;
                                        nextStep(getResources().getString(R.string.change_order));
                                        if(selectedItems!=null && selectedItems.size()==maxItemCount)
                                            Toast.makeText(MainActivity.this, "Maximum "+maxItemCount+" Items can be added", Toast.LENGTH_LONG).show();
                                    }
                                    else if (selectedItems.size() == 1) {
                                        nextStep(getResources().getString(R.string.ask_keywords));
                                    }
                                }else if(askEdit && addMoreState)
                                {
                                    askEdit=false;
                                    addMoreState=false;
                                    if(mediaCount2!=null) {
                                        if(String.valueOf(selectedItems.size()).equalsIgnoreCase(String.valueOf(maxItemCount))){
                                            mediaCount2.setText(String.valueOf(selectedItems.size())+" (Maximum Items)");
                                        }else
                                        mediaCount2.setText(String.valueOf(selectedItems.size()));
                                    }
                                    nextStep(getResources().getString(R.string.ask_edit_keywords));
                                    if(selectedItems!=null && selectedItems.size()==maxItemCount)
                                        Toast.makeText(MainActivity.this, "Maximum "+maxItemCount+" Items can be added", Toast.LENGTH_LONG).show();
                                }
                            }

                            public void onTick(long millisUntilFinished) {
                                // millisUntilFinished    The amount of time until finished.
                            }
                        }.start();

                        createEditHelperCommands();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    void createEditHelperCommands(){
        counterCommands= new CountDownTimer(10*1000, 1000) {
            public void onFinish() {
                if(commands!=null)
                    commands.setVisibility(View.VISIBLE);
            }
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }
    public class LoadMoments extends AsyncTask<Void, Void, Void> {

        private List<Uri> selectedItems;

        public LoadMoments(List<Uri> selectedItems) {
            this.selectedItems = selectedItems;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                delayNavigation= pref.getString("navigationDelay", "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(progressBar.getVisibility()==View.INVISIBLE)
                statusText.setText(sayPlay);
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                filteredList.clear();
                if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
                    list=presentationList.getPresentationList();

                if(list!=null && !list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        String json = gson.toJson(list.get(0));
                        System.out.println("list of moments: " + json);
                    }
                    removeLayout();
                    LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                            .LAYOUT_INFLATER_SERVICE);
                    View v = vi.inflate(R.layout.grid_response, null);

                    backButton.setVisibility(View.VISIBLE);
                    final TextView commands=v.findViewById(R.id.commands);
                    if(!commandsList)
                    commands.setVisibility(View.VISIBLE);
                    commands.setText("'Play <Show name>'  'Edit <Show name>' \n \n 'Delete <Show name>'");

                    if(commandsList)
                    new CountDownTimer(10*1000, 1000) {
                        public void onFinish() {
                            commandsList=false;
                            if(commands!=null)
                                commands.setVisibility(View.VISIBLE);
                        }
                        public void onTick(long millisUntilFinished) {
                        }
                    }.start();

                    IRecyclerViewClickListener listener = new IRecyclerViewClickListener() {
                        @Override
                        public void onLongClicked(int position) {
                            showMomentDetails(position,list);
                        }

                        @Override
                        public void onMenuClicked(int position) {
                            showMomentDetails(position,list);
                        }

                        @Override
                        public void onSingleClick(int position) {
                            showingMoment=true;
                            speechOff=true;
                            if (countDownTimerListMoments != null)
                                countDownTimerListMoments.cancel();
                            if(counterShowDetails!=null)
                                counterShowDetails.cancel();
                            playingFrom="list";
                            Intent intent=new Intent(MainActivity.this, PlayMomentActivity.class);
                            intent.putExtra("position",position);
                            startActivity(intent);
                        }
                    };
                    listAdapter= new ListMomentsAdapter(list, MainActivity.this, listener);
                    recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
                    audioAttached = v.findViewById(R.id.audioAttached);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(listAdapter);

                    add.setVisibility(View.VISIBLE);

                    countDownTimerListMoments= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                        public void onFinish() {
                            removeLayout();
                            reset();
                            if(progressBar.getVisibility()==View.INVISIBLE)
                            statusText.setText(listeningText);
                            userQueryTextView.setText(showingCommandsText);
                            displayImage();
                        }
                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start();

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if(countDownTimerListMoments!=null)
                                countDownTimerListMoments.cancel();

                            countDownTimerListMoments= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                                public void onFinish() {
                                    removeLayout();
                                    reset();
                                    if(progressBar.getVisibility()==View.INVISIBLE)
                                    statusText.setText(listeningText);
                                    userQueryTextView.setText(showingCommandsText);
                                    displayImage();
                                }
                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();
                        }
                    });

                    ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
                    insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                }else{
                    nextStep(getResources().getString(R.string.list_moments_empty));
                }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    public class LoadFilteredMoments extends AsyncTask<Void, Void, Void> {

        private List<Presentation> filteredItems;

        public LoadFilteredMoments(List<Presentation> filteredItems) {
            this.filteredItems = filteredItems;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                delayNavigation= pref.getString("navigationDelay", "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                            removeLayout();
                            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                                    .LAYOUT_INFLATER_SERVICE);
                            View v = vi.inflate(R.layout.grid_response, null);
                        TextView commands=v.findViewById(R.id.commands);
                        commands.setVisibility(View.VISIBLE);
                        commands.setText("'Play <Show name>'  'Edit <Show name>' \n \n 'Delete <Show name>'");
                            IRecyclerViewClickListener listener = new IRecyclerViewClickListener() {
                                @Override
                                public void onLongClicked(int position) {
                                    showMomentDetails(position,filteredItems);
                                }

                                @Override
                                public void onMenuClicked(int position) {
                                    showMomentDetails(position,filteredItems);
                                }

                                @Override
                                public void onSingleClick(int position) {
                                    showingMoment=true;
                                    speechOff=true;
                                    if (countDownTimerListMoments != null)
                                        countDownTimerListMoments.cancel();
                                    if(counterShowDetails!=null)
                                        counterShowDetails.cancel();
                                    playingFrom="list";
                                    Intent intent=new Intent(MainActivity.this, PlayMomentActivity.class);
                                    intent.putExtra("position",filteredItems.get(position).getFilteredPosition());
                                    startActivity(intent);
                                }
                            };
                            listAdapter= new ListMomentsAdapter(filteredItems, MainActivity.this, listener);
                            recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
                            audioAttached = v.findViewById(R.id.audioAttached);
                            matchCount=v.findViewById(R.id.matchCount);
                            matchCount.setVisibility(View.VISIBLE);
                            matchCount.setText(filteredItems.size()+ " Matches Found");
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

                            recyclerView.setHasFixedSize(true);
                            recyclerView.setAdapter(listAdapter);

                            add.setVisibility(View.VISIBLE);

                            countDownTimerListMoments= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                                public void onFinish() {
                                    if(progressBar.getVisibility()==View.INVISIBLE)
                                    statusText.setText(listeningText);
                                     new LoadMoments(selectedItems).execute();
                                }
                                public void onTick(long millisUntilFinished) {
                                    // millisUntilFinished    The amount of time until finished.
                                }
                            }.start();

                            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if(countDownTimerListMoments!=null)
                                        countDownTimerListMoments.cancel();

                                    countDownTimerListMoments= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                                        public void onFinish() {
                                            removeLayout();
                                            if(progressBar.getVisibility()==View.INVISIBLE)
                                            statusText.setText(listeningText);
                                            reset();
                                            userQueryTextView.setText(showingCommandsText);
                                            displayImage();
                                        }
                                        public void onTick(long millisUntilFinished) {
                                            // millisUntilFinished    The amount of time until finished.
                                        }
                                    }.start();
                                }
                            });

                            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
                            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    void removeLayout() {
        chips.setVisibility(View.GONE);
        if(autoSwipeCounter!=null){
            autoSwipeCounter.cancel();
        }
        if(myHandler!=null && myRunnable!=null)
        myHandler.removeCallbacks(myRunnable);
        if(countDownTimerListMoments!=null){
            countDownTimerListMoments.cancel();
        }
        if(counter!=null){
            counter.cancel();
        }
        if(commands!=null)
            commands.setVisibility(View.GONE);
        if(counterCommands!=null){
            counterCommands.cancel();
        }
        if(counterShowDetails!=null)
            counterShowDetails.cancel();
        if(timer !=null)
        {
            timer.cancel();
        }
        done.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        playPrelude="";
        backButton.setVisibility(View.GONE);
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.removeAllViews();
    }

    void displayImage() {
        filteredList.clear();
        add.setVisibility(View.GONE);
        if(statusText.getText().equals(sayPlay)){
            statusText.setText(listeningText);
        }
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.image_response, null);
        ImageView imageView = v.findViewById(R.id.familyPic);
        TextView commands=v.findViewById(R.id.commands);
        TextView commands2=v.findViewById(R.id.commands2);

        commands2.setVisibility(View.VISIBLE);
        commands.setVisibility(View.VISIBLE);

        ContentResolver cR2 = getContentResolver();
        if(familyPicUri!=null)
        mimeType2=cR2.getType(Uri.parse(familyPicUri));
        if(mimeType2!=null) {
            Cursor returnCursor =
                    getContentResolver().query(Uri.parse(familyPicUri), null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: " + size);
            if (size < 100000) {
                imageView.setImageURI(Uri.parse(familyPicUri));
            } else
                imageView.setImageBitmap(getBitmap(Uri.parse(familyPicUri)));
        }else{
            imageView.setImageResource(R.drawable.media_not_found_image);
        }

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void nextStep(final String nextCommand) {
        chipsExtra.setVisibility(View.GONE);
        chipsYes.setVisibility(View.VISIBLE);
        chipsCancel.setVisibility(View.VISIBLE);
        chipsCancel.setText("Cancel");
        chipsYes.setText("Yes");
        chipsNo.setText("No");
        chipsNo.setVisibility(View.VISIBLE);
        chips.setVisibility(View.GONE);
        mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (mediaPlayerProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
        userQueryTextView.setText(nextCommand);
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

        textToSpeech.speak(nextCommand, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                speechOff = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(counterCommands!=null)
                            counterCommands.cancel();
                        if(commands!=null)
                            commands.setVisibility(View.GONE);
                        stopSpeechRecognition();
                    }
                });
            }

            @Override
            public void onDone(String utteranceId) {
                speechVolumeProgress = pref.getInt("speechVolume", 0);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, (speechVolumeProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(nextCommand.equals(getResources().getString(R.string.lets_start))){
                            speechOff=false;
                            imageVideoPicker();
                        }
                        else if (nextCommand.equals(getResources().getString(R.string.ask_keywords))) {
                            createEditHelperCommands();
                            speechOff = false;
                            chipsYes.setVisibility(View.GONE);
                            chipsNo.setVisibility(View.GONE);
                            chips.setVisibility(View.VISIBLE);
                            startSpeechRecognition();
                            keywordsEnabled = true;
                            countKeywords = 0;
                            keywords.clear();
                        } else if (nextCommand.equals(getResources().getString(R.string.ask_prelude))) {
                            recordPrelude();
                        } else if (nextCommand.equals(getResources().getString(R.string.self_photo))) {
                            createEditHelperCommands();
                            chips.setVisibility(View.VISIBLE);
                            speechOff = false;
                            startSpeechRecognition();
                            askSelfPhoto = true;
                        } else if (nextCommand.equals(getResources().getString(R.string.change_order))) {
                            createEditHelperCommands();
                            speechOff = false;
                            chips.setVisibility(View.VISIBLE);
                            startSpeechRecognition();
                        } else if (nextCommand.equals(getResources().getString(R.string.congratulations))
                        || nextCommand.equals(getResources().getString(R.string.congratulations_modified))) {
                            String playback=pref.getString("playback",null);
                            if(playback!=null && playback.equalsIgnoreCase("true"))
                            playPrelude();
                            else {
                                selfPic.setVisibility(View.GONE);
                                showBeforeEdit();
                                nextStep(getResources().getString(R.string.edit));
                            }
                        }else if(nextCommand.equals(getResources().getString(R.string.edit))){
                           createEditHelperCommands();
                            speechOff = false;
                            chips.setVisibility(View.VISIBLE);
                            startSpeechRecognition();
                            String playback=pref.getString("playback",null);
                            if(playback!=null && playback.equalsIgnoreCase("true"))
                            showBeforeEdit();
                            askEdit = true;
                        }
                        else if(nextCommand.equals(getResources().getString(R.string.saved))){
                            anotherPresentation=true;
                            speechOff = false;
                            chipsYes.setVisibility(View.VISIBLE);
                            chipsCancel.setVisibility(View.VISIBLE);
                            chipsCancel.setText("Cancel");
                            chipsYes.setText("Yes");
                            chipsNo.setVisibility(View.VISIBLE);
                            chips.setVisibility(View.VISIBLE);
                            startSpeechRecognition();
                        }
                        else if(nextCommand.equalsIgnoreCase(getResources().getString(R.string.list_moments_empty))){
                            anotherPresentation=true;
                            speechOff = false;
                            chips.setVisibility(View.VISIBLE);
                            startSpeechRecognition();
                        }else if(nextCommand.equals(getResources().getString(R.string.great))){
                            listMoments();
                            speechOff = false;
                            startSpeechRecognition();
                        }else if(nextCommand.equalsIgnoreCase(getResources().getString(R.string.ask_edit_keywords))){
                           createEditHelperCommands();
                            speechOff = false;
                            startSpeechRecognition();
                            chips.setVisibility(View.VISIBLE);
                            askKeywordsAgain=true;
                         }
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    void afterSave (){
        Gson gson = new GsonBuilder().serializeNulls().create();
        presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
        list.clear();
        if (presentationList != null && !presentationList.getPresentationList().isEmpty())
            list = presentationList.getPresentationList();

        if (list != null && !list.isEmpty()) {
            commandsList = true;
            listMoments();
        } else {
            removeLayout();
            displayImage();
        }
    }

    void playPrelude(){
        removeLayout();

        selfPic.setVisibility(View.GONE);

        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.image_response, null);

            ImageView imageView = v.findViewById(R.id.familyPic);
            ImageView videoIcon=v.findViewById(R.id.video_icon);
        ContentResolver cR2 = getContentResolver();
        if(selfPicUri!=null)
        mimeType2=cR2.getType(selfPicUri);
        if(selfPicUri!=null && mimeType2!=null) {
            Cursor returnCursor =
                    getContentResolver().query(selfPicUri, null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: " + size);
            if (size < 100000) {
                imageView.setImageURI(selfPicUri);
            } else
                imageView.setImageBitmap(getBitmap(selfPicUri));
        }else{
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType=cR.getType(selectedItems.get(0));

            System.out.println("mime type : "+mimeType);
            if(mimeType==null){
                imageView.setImageResource(R.drawable.media_not_found_image);
            }
            else if(mimeType.contains("image")) {
                Cursor returnCursor =
                       getContentResolver().query(selectedItems.get(0), null, null, null, null);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long size = returnCursor.getLong(sizeIndex);

                System.out.println("file size: "+size);
                if(size < 100000) {
                    imageView.setImageURI(selectedItems.get(0));
                }
                else
                    imageView.setImageBitmap(getBitmap(selectedItems.get(0)));
            }
            else if(mimeType.contains("video"))
            {
                MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
                mMMR.setDataSource(MainActivity.this, selectedItems.get(0));
                Bitmap bitmap = mMMR.getFrameAtTime();
                imageView.setImageBitmap(bitmap);
                videoIcon.setVisibility(View.VISIBLE);
            }
        }

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mediaPlayer = new MediaPlayer();
        int mediaPlayerProgress = pref.getInt("preludeVolume", 0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (mediaPlayerProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
        try {
            mediaPlayer.setDataSource(AudioSavePathInDevice);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mediaPlayer.release();
                    mediaPlayer=null;
                    Toast.makeText(MainActivity.this, "Failed to play prelude.", Toast.LENGTH_LONG).show();
                    showAlbum();
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer=null;
                    showAlbum();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Prelude not found.", Toast.LENGTH_LONG).show();
            if(mediaPlayer!=null)
            mediaPlayer.release();
            mediaPlayer=null;
            showAlbum();
        }
    }

    void showAlbum() {
        int mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (mediaPlayerProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);

        currentPagePhotoAlbum=0;
         removeLayout();
        final LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        final View v = vi.inflate(R.layout.photo_album, null);

        final ViewPager viewPager = v.findViewById(R.id.photo_album_view);
        final ImageView leftSwipePhotoAlbum = v.findViewById(R.id.leftSwipePhotoAlbum);
        final ImageView rightSwipePhotoAlbum = v.findViewById(R.id.rightSwipePhotoAlbum);
        sliderAdapterPhotoAlbum = new SliderAdapterPhotoAlbum(MainActivity.this, selectedItems);
        viewPager.setAdapter(sliderAdapterPhotoAlbum);
        ContentResolver cR = getContentResolver();
        final String mimeType=cR.getType(selectedItems.get(0));

        if(selfPicUri==null && mimeType!=null && mimeType.contains("image") && selectedItems.size()>1) {
            viewPager.setCurrentItem(1);
            leftSwipePhotoAlbum.setVisibility(View.VISIBLE);
            currentPagePhotoAlbum=1;
        }
        else {
            viewPager.setCurrentItem(0);
        }

        if(selfPicUri==null && mimeType!=null&& mimeType.contains("image") && selectedItems.size()>1) {
            final String mimeType2=cR.getType(selectedItems.get(1));
            if(mimeType2!=null && mimeType2.contains("image"))
                videoCompleted= true;
            else
                videoCompleted=false;
        }
        else if(mimeType!=null && mimeType.contains("image"))
            videoCompleted=true;
        else
            videoCompleted=false;

        new CountDownTimer(1000, 1000) {
            public void onFinish() {
                if(selfPicUri==null && mimeType!=null && mimeType.contains("image")) {
                    videoView = (VideoView) viewPager.findViewWithTag("video1");
                    relative=viewPager.findViewWithTag("r1");
                    if(videoView!=null)
                        videoView.setVideoURI(selectedItems.get(1));
                }else {
                    relative=viewPager.findViewWithTag("r0");
                    videoView = (VideoView) viewPager.findViewWithTag("video0");
                    if(videoView!=null)
                        videoView.setVideoURI(selectedItems.get(0));
                }
                if(videoView!=null) {
                    if(myHandler!=null && myRunnable!=null)
                        myHandler.removeCallbacks(myRunnable);
                    final MediaController mediaController=new MediaController(MainActivity.this,false);
                    mediaController.setBackgroundColor(getResources().getColor(R.color.media_controller_background));
                    mediaController.setAnchorView(relative);
                    videoView.setMediaController(mediaController);
                    myHandler = new Handler();
                    myRunnable = new Runnable() {
                        @Override
                        public void run() {
                             mediaController.show();
                            myHandler.postDelayed(myRunnable,1000);
                        }
                    };
                    myHandler.post(myRunnable);
                    videoView.start();

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            videoCompleted=true;
                            if(selectedItems.size()==1){
                                nextStep(getResources().getString(R.string.edit));
                            }
                            else if(selectedItems.size()==2 && selfPicUri==null && mimeType!=null && mimeType.contains("image")){
                                nextStep(getResources().getString(R.string.edit));
                            }
                            else
                            viewPager.setCurrentItem(++currentPagePhotoAlbum);
                        }
                    });
                }
            }
            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();

        if(selectedItems.size()==2 && selfPicUri==null && mimeType!=null && mimeType.contains("image") && videoCompleted){
            nextStep(getResources().getString(R.string.edit));
        }
           else if(selectedItems.size()>1)
        rightSwipePhotoAlbum.setVisibility(View.VISIBLE);
        else if(selectedItems.size()==1 && mimeType!=null && mimeType.contains("image")){
            nextStep(getResources().getString(R.string.edit));
        }

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        rightSwipePhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(++currentPagePhotoAlbum);
            }
        });

        leftSwipePhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(--currentPagePhotoAlbum);
            }
        });

        ViewPager.OnPageChangeListener viewListenerPhotoAlbum = new ViewPager.OnPageChangeListener() {   //to select the dot
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                if(myHandler!=null && myRunnable!=null)
                    myHandler.removeCallbacks(myRunnable);

                ContentResolver cR = getContentResolver();
                final String mimeType=cR.getType(selectedItems.get(position));

                if(mimeType!=null && mimeType.contains("image"))
                    videoCompleted=true;
                else
                videoCompleted=false;

                if(videoView!=null && videoView.isPlaying()){
                    videoView.stopPlayback();
                }

                currentPagePhotoAlbum = position;
                videoView=(VideoView) viewPager.findViewWithTag("video"+position);
                if(videoView!=null){
                    final MediaController mediaController=new MediaController(MainActivity.this,false);
                    mediaController.setBackgroundColor(getResources().getColor(R.color.media_controller_background));
                    RelativeLayout relative=viewPager.findViewWithTag("r"+position);
                    videoView.setVideoURI(selectedItems.get(position));
                    mediaController.setAnchorView(relative);
                    videoView.setMediaController(mediaController);
                    myHandler = new Handler();
                    myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mediaController.show();
                            myHandler.postDelayed(myRunnable,1000);
                        }
                    };
                    myHandler.post(myRunnable);
                    videoView.start();

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            videoCompleted=true;
                            if(currentPagePhotoAlbum==selectedItems.size()-1){
                                    nextStep(getResources().getString(R.string.edit));
                            }else
                                viewPager.setCurrentItem(++currentPagePhotoAlbum);
                        }
                    });

                }

                if (position == 0) {
                    rightSwipePhotoAlbum.setVisibility(View.VISIBLE);
                    leftSwipePhotoAlbum.setVisibility(View.GONE);
                } else if (position == selectedItems.size() - 1) {
                    leftSwipePhotoAlbum.setVisibility(View.VISIBLE);
                    rightSwipePhotoAlbum.setVisibility(View.GONE);

                    if (mimeType!=null && mimeType.contains("image"))
                        nextStep(getResources().getString(R.string.edit));
                } else {
                    leftSwipePhotoAlbum.setVisibility(View.VISIBLE);
                    rightSwipePhotoAlbum.setVisibility(View.VISIBLE);
                }

                if(pref.getString("autoSwipe","").equalsIgnoreCase("true"))
                autoSwipePhotoAlbum(viewPager,currentPagePhotoAlbum,selectedItems.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
         viewPager.addOnPageChangeListener(viewListenerPhotoAlbum);

        if(pref.getString("autoSwipe","").equalsIgnoreCase("true"))
        autoSwipePhotoAlbum(viewPager,currentPagePhotoAlbum,selectedItems.size());
    }

    public void autoSwipePhotoAlbum(final ViewPager slideViewPagerPhotoAlbum,final int currentPagePhotoAlbum2, final int photoAlbumUriLength){
       if(autoSwipeCounter!=null){
           autoSwipeCounter.cancel();
       }
        int delay = pref.getInt("delayPhotoAlbum",0);
        if(delay==0){
            delay=3;
        }
        Handler handlerPhotoAlbum = new Handler();

        if (currentPagePhotoAlbum != selectedItems.size()) {
            autoSwipeCounter=new CountDownTimer(delay * 1000, 1000) {
                public void onFinish() {
                    int finalCurrentPagePhotoAlbum = currentPagePhotoAlbum;
                    ContentResolver cR = getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String mimeType="";
                    if(finalCurrentPagePhotoAlbum+1 != selectedItems.size()) {
                        mimeType = cR.getType(selectedItems.get(finalCurrentPagePhotoAlbum++));
                        if (mimeType==null || mimeType.contains("image") || (mimeType!=null && mimeType.contains("video") && videoCompleted)) {
                            slideViewPagerPhotoAlbum.setCurrentItem(currentPagePhotoAlbum++, true);
                            autoSwipePhotoAlbum(slideViewPagerPhotoAlbum, currentPagePhotoAlbum, photoAlbumUriLength);
                        }
                    }else{
                        slideViewPagerPhotoAlbum.setCurrentItem(selectedItems.size()-1, true);
                    }
                }

                public void onTick(long millisUntilFinished) {
                    // millisUntilFinished    The amount of time until finished.
                }
            }.start();
        }
    }

    void recordPrelude(){
        progressBar.setVisibility(View.INVISIBLE);
        statusText.setText(saySomethingText);
        timerText.setVisibility(View.VISIBLE);

        String folderPath=null;
        if (Build.VERSION.SDK_INT >= 29)
            folderPath=  getExternalFilesDir(null) + "/Eva Moments";
        else
            folderPath = Environment.getExternalStorageDirectory() + "/Eva Moments";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            File directory = new File(folderPath);
            directory.mkdirs();
        }

        if (Build.VERSION.SDK_INT >= 29) {
            AudioSavePathInDevice =
                    getExternalFilesDir(null) + "/Eva Moments" + "/" +
                            System.currentTimeMillis() + ".mp3";
        }
        else
         AudioSavePathInDevice =
                Environment.getExternalStorageDirectory() + "/Eva Moments" + "/" +
                        System.currentTimeMillis() + ".mp3";

        MediaRecorderReady();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e){
            e.printStackTrace();
        }

        final int[] time = {10};
        CountDownTimer countDowntimer = new CountDownTimer(11000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(checkDigit(time[0]));
                time[0]--;
            }

            public void onFinish() {
                try {
                   mediaRecorder.stop();
                    timerText.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    statusText.setText(processingText);
                    preludeAudioLayout.setVisibility(View.VISIBLE);
                    nextStep(getResources().getString(R.string.self_photo));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        countDowntimer.start();
    }
    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        callOnce=true;
        Log.d("Speech ","onBeginingOfSpeech");
        microphoneOnCount=0;
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Speech ","onEndOfSpeech");
        mSpeechRecognizer.stopListening();
    }

    @Override
    public void onError(int error) {
        callOnce=true;
        String errorMessage = getErrorText(error);
        Log.d("Speech Main Activity ","onError: "+errorMessage);
        resetSpeechRecognizer();
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    @Override
    public void onResults(Bundle results) {
        if (callOnce) {
            callOnce=false;
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String userQuery = matches.get(0);
            if(!userQuery.equals(" ") && !userQuery.equals("")) {
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
                    boolean isNetworkAvailable= isOffline(MainActivity.this);
                    if(isNetworkAvailable)
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SITE_URL)));
                    else
                        Toast.makeText(this, "Internet needed to open Eva Community", Toast.LENGTH_LONG).show();
                }
                else if (userQuery.equalsIgnoreCase("Quit") || userQuery.equalsIgnoreCase("Close") ||
                        userQuery.equalsIgnoreCase("Exit")) {
                   quitFunction();

                } else if (userQuery.equalsIgnoreCase("cancel")) {
                    if (createCalledFromAdd) {
                        createCalledFromAdd = false;
                        commandsList=false;
                        reset();
                        listMoments();
                    } else {
                        reset();
                        removeLayout();
                        displayImage();
                    }
                } else if (userQuery.equalsIgnoreCase("create show") || userQuery.equalsIgnoreCase("create shows") ||
                        userQuery.equalsIgnoreCase("create")) {
                    createCalledFromAdd = false;
                    reset();
                    removeLayout();
                    displayImage();
                    imageVideoPicker();
                }else if (userQuery.equalsIgnoreCase("list shows") ||
                        userQuery.equalsIgnoreCase("list show") || userQuery.equalsIgnoreCase("list")) {
                    reset();
                    commandsList=false;
                    userQueryTextView.setText(showingCommandsText);
                    listMoments();
                } else if (userQuery.toLowerCase().contains("play") && userQuery.contains(" ")) {
                    String s = userQuery.substring(userQuery.lastIndexOf("play") + 5).trim();
                  playShow(s);
                }else if (userQuery.toLowerCase().contains("delete") && userQuery.contains(" ")) {
                    String s = userQuery.substring(userQuery.lastIndexOf("delete") + 7).trim();
                  deleteShow(s);
                } else if (userQuery.toLowerCase().contains("edit") && userQuery.contains(" ")) {
                    String s = userQuery.substring(userQuery.lastIndexOf("edit") + 5).trim();
                  editShow(s);
                } else if (userQuery.equalsIgnoreCase("Settings") || userQuery.equalsIgnoreCase("Show Settings")) {
                    reset();
                    removeLayout();
                    displayImage();
                    if(handlerMicrophone!=null && mRunnableMicrophone!=null)
                        handlerMicrophone.removeCallbacks(mRunnableMicrophone);
                    stopSpeechRecognition();
                    settingsActivated=true;
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (userQuery.toLowerCase().contains("show commands") || userQuery.equalsIgnoreCase("help")) {
                    reset();
                    removeLayout();
                    displayImage();
                    stopSpeechRecognition();
                    showCommandsActivated=true;
                    if(handlerMicrophone!=null && mRunnableMicrophone!=null)
                        handlerMicrophone.removeCallbacks(mRunnableMicrophone);
                    userQueryTextView.setText(showingCommandsText);
                    Intent intent = new Intent(MainActivity.this, ShowCommandsActivity.class);
                    startActivity(intent);
                }
                else if (!playPrelude.equalsIgnoreCase("") && userQuery.equalsIgnoreCase("play prelude")) {
                    showingMoment = true;
                    playClicked = true;
                    speechOff = true;
                    if (countDownTimerListMoments != null)
                        countDownTimerListMoments.cancel();
                    if (counterShowDetails != null)
                        counterShowDetails.cancel();
                    Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                    intent.putExtra("speaker", "true");
                    intent.putExtra("position", Integer.parseInt(playPrelude));
                    startActivity(intent);
                } else if (askList) {
                    askList = false;
                    if (userQuery.equalsIgnoreCase("yes")) {
                        nextStep(getResources().getString(R.string.great));
                    }else{
                        chips.setVisibility(View.GONE);
                    }
                } else if (keywordsEnabled) {
                    if ((userQuery.equalsIgnoreCase("no more") ||
                            userQuery.equalsIgnoreCase("done")) && !keywords.isEmpty() ) {
                        keywordsEnabled = false;
                        nextStep(getResources().getString(R.string.ask_prelude));
                    } else if (!userQuery.equalsIgnoreCase("")) {
                        userQuery = userQuery.substring(0, 1).toUpperCase() + userQuery.substring(1);
                        if (countKeywords < 3) {
                            if (countKeywords == 0) {
                                keywordsLayout.setVisibility(View.VISIBLE);
                                detailsLayout.setVisibility(View.VISIBLE);
                                keywordsTextView.setText(userQuery);

                                statusText.setText(noMoreKeywordsText);

                                keywords.add(userQuery);
                                countKeywords++;
                            } else if (countKeywords == 1) {
                                if(!keywords.get(0).equalsIgnoreCase(userQuery)) {
                                    keywordsTextView.setText(keywords.get(0) + ", " + userQuery);
                                    keywords.add(userQuery);
                                    countKeywords++;
                                }
                                else
                                    Toast.makeText(this, "Keyword already added", Toast.LENGTH_LONG).show();
                            } else if (countKeywords == 2) {
                                if(!keywords.get(0).equalsIgnoreCase(userQuery) && !keywords.get(1).equalsIgnoreCase(userQuery)) {
                                    keywordsTextView.setText(keywords.get(0) + ", " + keywords.get(1) + ", " + userQuery);
                                    keywords.add(userQuery);
                                    countKeywords++;
                                } else
                                    Toast.makeText(this, "Keyword already added", Toast.LENGTH_LONG).show();
                            }
                            if (countKeywords >= 3) {
                                keywordsEnabled = false;
                                nextStep(getResources().getString(R.string.ask_prelude));
                            }
                        }
                    }
                } else if (askSelfPhoto) {
                    askSelfPhoto = false;
                    if (userQuery.toLowerCase().contains("yes")) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 2);
                    } else if (userQuery.toLowerCase().contains("no")) {
                        preludeImageLayout.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setText("Not Available");
                        if (editPosition != null && !editPosition.equalsIgnoreCase(""))
                            nextStep(getResources().getString(R.string.congratulations_modified));
                        else
                            nextStep(getResources().getString(R.string.congratulations));
                    } else {
                        preludeImageLayout.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setVisibility(View.VISIBLE);
                        preludeImageAvailable.setText("Not Available");
                        if (editPosition != null && !editPosition.equalsIgnoreCase(""))
                            nextStep(getResources().getString(R.string.congratulations_modified));
                        else
                            nextStep(getResources().getString(R.string.congratulations));
                    }
                } else if (changeOrder) {
                    changeOrder = false;
                    if (userQuery.toLowerCase().contains("yes")) {
                        adapter = new GridAdapter(MainActivity.this, selectedItems);
                        ItemTouchHelper.Callback callback =
                                new ItemMoveCallback(adapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(recyclerView);
                        recyclerView.setAdapter(adapter);

                        done.setVisibility(View.GONE);
                        chips.setVisibility(View.VISIBLE);
                        chipsYes.setText("Done");
                        chipsNo.setVisibility(View.GONE);

                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(dragText);
                    } else if (userQuery.toLowerCase().contains("no")) {
                        nextStep(getResources().getString(R.string.ask_keywords));
                    } else {
                        nextStep(getResources().getString(R.string.ask_keywords));
                    }
                } else if (askKeywordsAgain) {
                    askKeywordsAgain = false;
                    if (userQuery.toLowerCase().contains("yes")) {
                        if(detailsLayout!=null) {
                            detailsLayout.setVisibility(View.GONE);
                            keywordsLayout.setVisibility(View.GONE);
                            preludeImageLayout.setVisibility(View.GONE);
                            mediaCountLayout.setVisibility(View.GONE);
                            preludeAudioLayout.setVisibility(View.GONE);
                            preludeImage.setVisibility(View.GONE);
                            preludeImageAvailable.setVisibility(View.GONE);
                        }
                        selfPic.setVisibility(View.GONE);
                        selfPicUri = null;
                        keyword1.setVisibility(View.GONE);
                        keyword2.setVisibility(View.GONE);
                        keyword3.setVisibility(View.GONE);
                        audioAttached.setVisibility(View.GONE);
                       if (top != null && top.getVisibility()==View.VISIBLE) {
                           top.setVisibility(View.GONE);
                           statusText.setText(processingText);
                           adapter = new GridAdapter(MainActivity.this, selectedItems);
                           recyclerView.setAdapter(adapter);
                       }
                        nextStep(getResources().getString(R.string.ask_keywords));
                    } else {
                        if (top != null && top.getVisibility()==View.VISIBLE) {
                            top.setVisibility(View.GONE);
                            statusText.setText(processingText);
                            adapter = new GridAdapter(MainActivity.this, selectedItems);
                            recyclerView.setAdapter(adapter);
                        }
                        if (editPosition != null && !editPosition.equalsIgnoreCase(""))
                            nextStep(getResources().getString(R.string.congratulations_modified));
                        else
                            nextStep(getResources().getString(R.string.congratulations));
                    }
                } else if (askEdit) {
                    if(top==null ||  top.getVisibility()!=View.VISIBLE) {
                        if (userQuery.toLowerCase().contains("yes")) {
                            speechOff = false;
                            startSpeechRecognition();
                            if (progressBar.getVisibility() == View.INVISIBLE)
                                statusText.setText(dragText);
                            new LoadGridTask(askEdit).execute();
                        } else {
                            chips.setVisibility(View.GONE);
                            askEdit = false;
                            afterEdit();
                        }
                    }else if(top.getVisibility()==View.VISIBLE){
                        if(userQuery.toLowerCase().equalsIgnoreCase("Add more")){
                            askEdit=true;
                            addMoreState=true;
                            imageVideoPicker();
                        }else if(userQuery.toLowerCase().equalsIgnoreCase("Done")){
                            editDone();
                        }
                        else if(userQuery.toLowerCase().equalsIgnoreCase("Save")){
                            if(!selectedItems.isEmpty()) {
                                askEdit = false;
                                addMoreState=false;
                                top.setVisibility(View.GONE);
                                adapter = new GridAdapter(MainActivity.this, selectedItems);
                                recyclerView.setAdapter(adapter);
                                afterEdit();
                            }else{
                                Toast.makeText(MainActivity.this, "Empty album. Please add images/videos", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else if (anotherPresentation) {
                    anotherPresentation = false;
                    commandsList=false;
                    if (userQuery.toLowerCase().contains("yes")) {
                        speechOff = false;
                        removeLayout();
                        displayImage();
                        imageVideoPicker();
                    }else{
                        userQueryTextView.setText("");
                        chips.setVisibility(View.GONE);
                    }
                }
            }
            if(mSpeechRecognizer!=null)
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }
    }

    void imageVideoPicker(){
        commandsList=false;
        filteredList.clear();
        if(timer!=null){
            timer.cancel();
        }
        Gson gson = new GsonBuilder().serializeNulls().create();
        presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
        list.clear();
        if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
            list=presentationList.getPresentationList();

        if(list==null || list.isEmpty() || list.size()<MAX_SHOW_LIMIT) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
            photoPickerIntent.setType("*/*");
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            startActivityForResult(photoPickerIntent, 1);
        }
        else{
            Toast.makeText(this, "Cannot Create more than "+MAX_SHOW_LIMIT+" shows.", Toast.LENGTH_LONG).show();
            System.out.println("list size: "+list.size());
            if(createCalledFromAdd) {
                createCalledFromAdd = false;
                reset();
                listMoments();
            }else{
                reset();
                removeLayout();
                displayImage();
            }
            Toast.makeText(this, "Cannot Create more than "+MAX_SHOW_LIMIT+" shows.", Toast.LENGTH_LONG).show();
        }
    }
    void afterEdit(){
        String createdDate,modifiedDate,numTimesPlayed = null,mostRecent=null;
        if(!createCalledFromAdd) {
            removeLayout();
            displayImage();
        }else{
            removeLayout();
        }

        createdDate=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        modifiedDate=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        Gson gson = new GsonBuilder().serializeNulls().create();
        presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
        list.clear();
        if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
            list=presentationList.getPresentationList();

        if(editPosition!=null && !editPosition.equalsIgnoreCase("")){
            if(list.get(Integer.parseInt(editPosition)).getCreatedDate()!=null)
            createdDate=list.get(Integer.parseInt(editPosition)).getCreatedDate();
            if(list.get(Integer.parseInt(editPosition)).getNumOfTimesPlayed()!=null)
                numTimesPlayed=list.get(Integer.parseInt(editPosition)).getNumOfTimesPlayed();
            if(list.get(Integer.parseInt(editPosition)).getLastPlayed()!=null)
                mostRecent=list.get(Integer.parseInt(editPosition)).getLastPlayed();
            list.remove(Integer.parseInt(editPosition));
            String json = gson.toJson(presentationList);
            editor.putString("list", json);
            editor.apply();
        }

        Presentation presentation=new Presentation();
        presentation.setAudioSavedPath(AudioSavePathInDevice);
        System.out.println("audio file: "+AudioSavePathInDevice);
        presentation.setKeywords(keywords);
        uris.clear();
        for(int i=0;i<selectedItems.size();i++) {
            uris.add(selectedItems.get(i).toString());
            if(selectedItems.size()-1 == i)
                presentation.setSelectedItems(uris);
        }

        if(selfPicUri!=null) {
            presentation.setSelfPic(selfPicUri.toString());
        }else
            presentation.setSelfPic(null);
        presentation.setCreatedDate(createdDate);
        presentation.setModifiedDate(modifiedDate);
        if(numTimesPlayed!=null)
        presentation.setNumOfTimesPlayed(numTimesPlayed);
        if(mostRecent!=null)
        presentation.setLastPlayed(mostRecent);
        list.add(0,presentation);
        presentationList =new PresentationList();
        presentationList.setPresentationList(list);

        String json = gson.toJson(presentationList);
        editor.putString("list", json);
        editor.apply();
        reset();
        afterSave();
        speechOff=true;
        stopSpeechRecognition();
        nextStep(getResources().getString(R.string.saved));
    }
    void listMoments(){
       new LoadMoments(selectedItems).execute();
    }
    void showBeforeEdit(){
        removeLayout();
        chips.setVisibility(View.VISIBLE);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.show_before_edit, null);

        TextView keywordsTextView=v.findViewById(R.id.keywords);
        TextView preludeImageAvailable=v.findViewById(R.id.preludeImageAvailability);
        ImageView preludeImage=v.findViewById(R.id.preludeImagePic);
        TextView mediaCount=v.findViewById(R.id.mediaCount);

        String size= String.valueOf(selectedItems.size());
        if(size.equalsIgnoreCase(String.valueOf(maxItemCount))){
            mediaCount.setText(size+" (Maximum Items)");
        }else
        mediaCount.setText(size);
        String key="";
        for(int i=0;i<keywords.size();i++){
            key=key+keywords.get(i)+", ";
        }
        if(key!=null && key.length()!=0)
        keywordsTextView.setText(key.substring(0,key.length()-2));

        adapter = new GridAdapter(MainActivity.this, selectedItems,askEdit);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);

        layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        ContentResolver cR2 = getContentResolver();
        if(selfPicUri!=null)
        mimeType2=cR2.getType(selfPicUri);
        if(selfPicUri!=null && mimeType2!=null){
            preludeImage.setVisibility(View.VISIBLE);
            Cursor returnCursor =
                    getContentResolver().query(selfPicUri, null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size2 = returnCursor.getLong(sizeIndex);

            System.out.println("file size: " + size);
            if (size2 < 100000) {
                preludeImage.setImageURI(selfPicUri);
            } else
                preludeImage.setImageBitmap(getBitmap(selfPicUri));
        }else{
            preludeImageAvailable.setVisibility(View.VISIBLE);
            preludeImageAvailable.setText("Not Available");
        }
        recyclerView.setAdapter(adapter);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void showMomentDetails(final int position, final List<Presentation> list){
        if(progressBar.getVisibility()==View.INVISIBLE)
        statusText.setText(listeningText);
        if(countDownTimerListMoments!=null)
            countDownTimerListMoments.cancel();
        removeLayout();
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.show_moment_details_2, null);

        playPrelude= String.valueOf(position);
        chipsCancel.setText("Play Prelude");
        chipsYes.setVisibility(View.GONE);
        chipsNo.setVisibility(View.GONE);
        chips.setVisibility(View.VISIBLE);
        add.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
       // ImageView backButton = v.findViewById(R.id.backButton);
       ImageView play=v.findViewById(R.id.play);
        ImageView edit=v.findViewById(R.id.edit);
        ImageView delete=v.findViewById(R.id.delete);
        ImageView cancel=v.findViewById(R.id.cancel);
        RelativeLayout touchLayout=v.findViewById(R.id.touchLayout);
        ImageView speaker=v.findViewById(R.id.speaker);

        TextView keywordsTextView=v.findViewById(R.id.keywords);
        TextView createdOn=v.findViewById(R.id.createdOn);
        TextView modifiedOn=v.findViewById(R.id.lastModified);
        TextView preludeImageAvailable=v.findViewById(R.id.preludeImageAvailability);
        ImageView preludeImage=v.findViewById(R.id.preludeImagePic);
        TextView mediaCount=v.findViewById(R.id.mediaCount);
        TextView numTimesPlayed=v.findViewById(R.id.numTimesPlayed);
        TextView lastPlayed=v.findViewById(R.id.lastPlayed);

       touchLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(counterShowDetails!=null)
                    counterShowDetails.cancel();
                counterShowDetails= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
                    public void onFinish() {
                        // removeLayout();
                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(listeningText);
                        new LoadMoments(selectedItems).execute();
                    }
                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();
                return false;
            }
        });
        counterShowDetails= new CountDownTimer(Integer.parseInt(delayNavigation)*1000, 1000) {
            public void onFinish() {
                if(progressBar.getVisibility()==View.INVISIBLE)
                statusText.setText(listeningText);
               new LoadMoments(selectedItems).execute();
            }
            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showingMoment=true;
                playClicked=true;
                speechOff=true;
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if(counterShowDetails!=null)
                    counterShowDetails.cancel();
                Intent intent=new Intent(MainActivity.this, PlayMomentActivity.class);
                intent.putExtra("speaker","true");
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        /*
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listMoments();
            }
        });

         */
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCalledFromAdd=true;
                editPosition= String.valueOf(position);
                selectedItems.clear();
                keywords.clear();
                for(int i=0;i<list.get(position).getSelectedItems().size();i++) {
                    selectedItems.add(Uri.parse(list.get(position).getSelectedItems().get(i)));
                }
                for(int i=0;i<list.get(position).getKeywords().size();i++){
                    keywords.add(list.get(position).getKeywords().get(i));
                }
                if(list.get(position).getSelfPic()!=null) {
                    selfPicUri = Uri.parse(list.get(position).getSelfPic());
                  }
                else
                    selfPicUri=null;

                AudioSavePathInDevice=list.get(position).getAudioSavedPath();
                askEdit=true;

                if(progressBar.getVisibility()==View.INVISIBLE)
                statusText.setText(dragText);
                new LoadGridTask(askEdit).execute();
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showingMoment=true;
                playClicked=true;
                speechOff=true;
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if(counterShowDetails!=null)
                    counterShowDetails.cancel();
                playingFrom="show";
                showPostion=position;
                Intent intent=new Intent(MainActivity.this, PlayMomentActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listMoments();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                presentationList =new PresentationList();
                presentationList.setPresentationList(list);

                Gson gson = new GsonBuilder().serializeNulls().create();
                String json = gson.toJson(presentationList);
                editor.putString("list", json);
                editor.apply();

                if(!list.isEmpty())
                listMoments();
                else{
                    reset();
                    removeLayout();
                    userQueryTextView.setText(showingCommandsText);
                    displayImage();
                }
            }
        });

        if(list.get(position).getLastPlayed()!=null) {
            lastPlayed.setText(list.get(position).getLastPlayed());
        }
        if(list.get(position).getNumOfTimesPlayed()!=null) {
          numTimesPlayed.setText(list.get(position).getNumOfTimesPlayed());
        }

        String size= String.valueOf(list.get(position).getSelectedItems().size());
        if(size.equalsIgnoreCase(String.valueOf(maxItemCount))){
            mediaCount.setText(size+" (Maximum Items)");
        }else
        mediaCount.setText(size);
        String key="";
        for(int i=0;i<list.get(position).getKeywords().size();i++){
           key=key+list.get(position).getKeywords().get(i)+", ";
        }
        keywordsTextView.setText(key.substring(0,key.length()-2));

        adapter = new GridAdapter(MainActivity.this, selectedItems,askEdit);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);

        layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        selectedItems.clear();
        keywords.clear();
        for(int i=0;i<list.get(position).getSelectedItems().size();i++) {
            selectedItems.add(Uri.parse(list.get(position).getSelectedItems().get(i)));
        }
        for(int i=0;i<list.get(position).getKeywords().size();i++){
            keywords.add(list.get(position).getKeywords().get(i));
        }

      if(list.get(position).getModifiedDate()!=null)
      modifiedOn.setText(list.get(position).getModifiedDate());
        if(list.get(position).getCreatedDate()!=null)
            createdOn.setText(list.get(position).getCreatedDate());

        if(list.get(position).getSelfPic()!=null) {
            selfPicUri = Uri.parse(list.get(position).getSelfPic());
        }
        else
            selfPicUri=null;

        AudioSavePathInDevice=list.get(position).getAudioSavedPath();

        ContentResolver cR = getContentResolver();
        if(selfPicUri!=null)
        mimeType2=cR.getType(selfPicUri);
            if(selfPicUri!=null && mimeType2!=null){
                preludeImage.setVisibility(View.VISIBLE);
                Cursor returnCursor =
                        getContentResolver().query(selfPicUri, null, null, null, null);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long size2 = returnCursor.getLong(sizeIndex);

                System.out.println("file size: " + size);
                if (size2 < 100000) {
                    preludeImage.setImageURI(selfPicUri);
                } else
                   preludeImage.setImageBitmap(getBitmap(selfPicUri));
        }else{
                preludeImageAvailable.setVisibility(View.VISIBLE);
                preludeImageAvailable.setText("Not Available");
            }
        recyclerView.setAdapter(adapter);


        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

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
    protected void onStop() {
        super.onStop();
        if(handlerMicrophone!=null && mRunnableMicrophone!=null)
        handlerMicrophone.removeCallbacks(mRunnableMicrophone);
        handlerFirebase.removeCallbacks(mRunnableFirebase);
        if(myHandler!=null && myRunnable!=null)
            myHandler.removeCallbacks(myRunnable);
        textToSpeech.speak("",TextToSpeech.QUEUE_FLUSH,null,null);
        stopSpeechRecognition();
    }

    void stopSpeechRecognition(){
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            listening.clearAnimation();
            progressBar.setVisibility(View.VISIBLE);
            listening.setVisibility(View.INVISIBLE);
            statusText.setText(processingText);
            mSpeechRecognizer=null;
        }
    }

    void startSpeechRecognition(){
         if(!speechOff) {
             speechVolumeProgress = pref.getInt("speechVolume", 0);
             am.setStreamVolume(AudioManager.STREAM_MUSIC, (speechVolumeProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);

             resetSpeechRecognizer();
            setRecogniserIntent();
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            Animation pulse = AnimationUtils.loadAnimation(MainActivity.this, R.anim.pulse);
            listening.startAnimation(pulse);
            ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
            listening.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(listeningText);
        }
    }
    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            //check Orientation
            int orientation=0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ExifInterface exifInterface = new ExifInterface(in);
                orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
            }

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("Bitmap ", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap bitmap = null;
            in =getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                bitmap = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                Log.d("Bitmap", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x,
                        (int) y, true);
                bitmap.recycle();
                bitmap = scaledBitmap;

                System.gc();
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeStream(in,null,options);
            }
            in.close();

            Log.d("Bitmap ", "bitmap size - width: " + bitmap.getWidth() + ", height: " +
                    bitmap.getHeight());

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            bitmap = rotateBitmap(bitmap, orientation);

            return bitmap;
        } catch (IOException e) {
            Log.e("Bitmap ", e.getMessage(), e);
            return null;
        }
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        System.out.println("orientation : "+orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    void reset(){
        chipsExtra.setVisibility(View.GONE);
        if(myHandler!=null && myRunnable!=null)
        myHandler.removeCallbacks(myRunnable);
        if(countDownTimerListMoments!=null){
            countDownTimerListMoments.cancel();
        }
        if(commands!=null)
            commands.setVisibility(View.GONE);
        if(counterCommands!=null){
            counterCommands.cancel();
        }
        if(counter!=null){
            counter.cancel();
        }
        if(counterShowDetails!=null)
            counterShowDetails.cancel();
        if(timer !=null)
        {
            timer.cancel();
        }
        currentPagePhotoAlbum=0;
        selectedItems.clear();
        keywords.clear();
        selfPicUri=null;
        AudioSavePathInDevice="";
        playingFrom="";
        keywordsEnabled=false; askSelfPhoto=false; changeOrder=false;askEdit=false;addMoreState=false;
        anotherPresentation=false; showingMoment=false;playClicked=false; askList=false; askKeywordsAgain=false;
        createCalledFromAdd=false;
        editPosition="";
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
        add.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
    }
    void quitFunction(){
        speechOff = true;
        stopSpeechRecognition();
        mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (mediaPlayerProgress * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
        userQueryTextView.setText(getResources().getString(R.string.quit));
        textToSpeech.speak(getResources().getString(R.string.quit), TextToSpeech.QUEUE_FLUSH, null, null);

        new CountDownTimer(4 * 1000, 1000) {
            public void onFinish() {
                finishAndRemoveTask();
            }

            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }
        if(textToSpeech != null) {
            textToSpeech.stop();
           textToSpeech.shutdown();
        }
    }

    void playShow(String s){
        if (!s.equalsIgnoreCase("")) {
            if (!filteredList.isEmpty()) {
                List<Presentation> list2= new ArrayList<>();
                list2.addAll(filteredList);
                int clearList=0;
                for (int i = 0; i < list2.size(); i++) {
                    for (int j = 0; j < list2.get(i).getKeywords().size(); j++) {
                        if(list2.get(i).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list2.get(i).getKeywords().get(j))<=levenshteinDistance){
                            // list2.get(i).setFilteredPosition(i);
                            if(clearList==0) {
                                filteredList.clear();
                                ++clearList;
                            }
                            filteredList.add(list2.get(i));
                            if (i == list2.size() - 1){// && j == list2.get(i).getKeywords().size() - 1) {
                                if (filteredList.size() == 1) {
                                    if (countDownTimerListMoments != null)
                                        countDownTimerListMoments.cancel();
                                    if (counterShowDetails != null)
                                        counterShowDetails.cancel();
                                    showingMoment = true;
                                    speechOff = true;
                                    playingFrom = "list";
                                    Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                                    intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                                    startActivity(intent);
                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i = list2.size();
                                break;
                            }else {
                              j=list2.get(i).getKeywords().size();
                            }
                        } else if (i == list2.size() - 1 && j == list2.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                            if (filteredList.size() == 1) {
                                showingMoment = true;
                                speechOff = true;
                                if (countDownTimerListMoments != null)
                                    countDownTimerListMoments.cancel();
                                if (counterShowDetails != null)
                                    counterShowDetails.cancel();
                                playingFrom = "list";
                                Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                                intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                                startActivity(intent);
                            } else {
                                new LoadFilteredMoments(filteredList).execute();
                            }
                            i = list2.size();
                            break;
                        }
                    }
                    if (i == list2.size() - 1) {
                        checkLevenshteinPlayFiltered(list2,s);
                    }
                }
            }
            else {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                filteredList.clear();
                if (presentationList != null && !presentationList.getPresentationList().isEmpty()) {
                    list = presentationList.getPresentationList();
                    for (int i = 0; i < list.size(); i++) {
                        for (int j = 0; j < list.get(i).getKeywords().size(); j++) {
                            System.out.println("distance of "+ list.get(i).getKeywords().get(j)+": "+Levenshtein.distance(s, list.get(i).getKeywords().get(j)));
                            if (list.get(i).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list.get(i).getKeywords().get(j))<=levenshteinDistance) {
                                list.get(i).setFilteredPosition(i);
                                filteredList.add(list.get(i));
                                if (i == list.size() - 1 ){ //&& j == list.get(i).getKeywords().size() - 1) {
                                    if (filteredList.size() == 1) {
                                        if (countDownTimerListMoments != null)
                                            countDownTimerListMoments.cancel();
                                        if (counterShowDetails != null)
                                            counterShowDetails.cancel();
                                        showingMoment = true;
                                        speechOff = true;
                                        playingFrom = "list";
                                        Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                                        intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                                        startActivity(intent);
                                    } else {
                                        new LoadFilteredMoments(filteredList).execute();
                                    }
                                    i = list.size();
                                    break;
                                }else {
                                    j=list.get(i).getKeywords().size();
                                }
                            } else if (i == list.size() - 1 && j == list.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                                if (filteredList.size() == 1) {
                                    showingMoment = true;
                                    speechOff = true;
                                    if (countDownTimerListMoments != null)
                                        countDownTimerListMoments.cancel();
                                    if (counterShowDetails != null)
                                        counterShowDetails.cancel();
                                    playingFrom = "list";
                                    Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                                    intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                                    startActivity(intent);
                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i = list.size();
                                break;
                            }
                        }
                        if (i == list.size() - 1) {
                         checkLevenshteinPlay(list,s);
                        }
                    }
                } else {
                    Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
        }
    }

    void checkLevenshteinPlayFiltered(List<Presentation> list2,String s){
        int clearList=0;
        for (int i = 0; i < list2.size(); i++) {
            for (int j = 0; j < list2.get(i).getKeywords().size(); j++) {
                if(Levenshtein.distance(s, list2.get(i).getKeywords().get(j))<=levenshteinDistance){
                    // list2.get(i).setFilteredPosition(i);
                    if(clearList==0) {
                        filteredList.clear();
                        ++clearList;
                    }
                    filteredList.add(list2.get(i));
                    if (i == list2.size() - 1){// && j == list2.get(i).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            if (countDownTimerListMoments != null)
                                countDownTimerListMoments.cancel();
                            if (counterShowDetails != null)
                                counterShowDetails.cancel();
                            showingMoment = true;
                            speechOff = true;
                            playingFrom = "list";
                            Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                            intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                            startActivity(intent);
                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i = list2.size();
                        break;
                    }else {
                        j=list2.get(i).getKeywords().size();
                    }
                } else if (i == list2.size() - 1 && j == list2.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        showingMoment = true;
                        speechOff = true;
                        if (countDownTimerListMoments != null)
                            countDownTimerListMoments.cancel();
                        if (counterShowDetails != null)
                            counterShowDetails.cancel();
                        playingFrom = "list";
                        Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                        intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                        startActivity(intent);
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i = list2.size();
                    break;
                }
            }
            if (i == list2.size() - 1) {
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }
    void checkLevenshteinPlay(List<Presentation> list,String s){
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getKeywords().size(); j++) {
                System.out.println("distance of "+ list.get(i).getKeywords().get(j)+": "+Levenshtein.distance(s, list.get(i).getKeywords().get(j)));
                if (Levenshtein.distance(s, list.get(i).getKeywords().get(j))<=levenshteinDistance) {
                    list.get(i).setFilteredPosition(i);
                    filteredList.add(list.get(i));
                    if (i == list.size() - 1 ){ //&& j == list.get(i).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            if (countDownTimerListMoments != null)
                                countDownTimerListMoments.cancel();
                            if (counterShowDetails != null)
                                counterShowDetails.cancel();
                            showingMoment = true;
                            speechOff = true;
                            playingFrom = "list";
                            Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                            intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                            startActivity(intent);
                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i = list.size();
                        break;
                    }else {
                        j=list.get(i).getKeywords().size();
                    }
                } else if (i == list.size() - 1 && j == list.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        showingMoment = true;
                        speechOff = true;
                        if (countDownTimerListMoments != null)
                            countDownTimerListMoments.cancel();
                        if (counterShowDetails != null)
                            counterShowDetails.cancel();
                        playingFrom = "list";
                        Intent intent = new Intent(MainActivity.this, PlayMomentActivity.class);
                        intent.putExtra("position", filteredList.get(0).getFilteredPosition());
                        startActivity(intent);
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i = list.size();
                    break;
                }
            }
            if (i == list.size() - 1) {
                listMoments();
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }
    void deleteShow(String s){
        if (!s.equalsIgnoreCase("")) {
            if (!filteredList.isEmpty()) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if (presentationList != null && !presentationList.getPresentationList().isEmpty()) {
                    list = presentationList.getPresentationList();
                }
                List<Presentation> list2 = new ArrayList<>();
                list2.addAll(filteredList);
                int clearList = 0;
                for (int i = 0; i < list2.size(); i++) {
                    for (int j = 0; j < list2.get(i).getKeywords().size(); j++) {
                        if (list2.get(i).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list2.get(i).getKeywords().get(j))<=levenshteinDistance) {
                            //  list.get(i).setFilteredPosition(i);
                            if(clearList==0) {
                                filteredList.clear();
                                ++clearList;
                            }
                            filteredList.add(list2.get(i));
                            if (i == list2.size() - 1){// && j == list2.get(i).getKeywords().size() - 1) {
                                if (filteredList.size() == 1) {
                                    list.remove(filteredList.get(0).getFilteredPosition());
                                    presentationList = new PresentationList();
                                    presentationList.setPresentationList(list);

                                    String json = gson.toJson(presentationList);
                                    editor.putString("list", json);
                                    editor.apply();

                                    if (list.isEmpty()) {
                                        removeLayout();
                                        reset();
                                        userQueryTextView.setText(showingCommandsText);
                                        displayImage();
                                    }else{
                                        listMoments();
                                    }
                                    Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i = list2.size();
                                break;
                            }else{
                                j=list2.get(i).getKeywords().size();
                            }
                        } else if (i == list2.size() - 1 && j == list2.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                            if (filteredList.size() == 1) {
                                list.remove(filteredList.get(0).getFilteredPosition());
                                presentationList = new PresentationList();
                                presentationList.setPresentationList(list);

                                String json = gson.toJson(presentationList);
                                editor.putString("list", json);
                                editor.apply();

                                if (list.isEmpty()) {
                                    removeLayout();
                                    reset();
                                    userQueryTextView.setText(showingCommandsText);
                                    displayImage();
                                }else{
                                    listMoments();
                                }
                                Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                            } else {
                                new LoadFilteredMoments(filteredList).execute();
                            }
                            i = list2.size();
                            break;
                        }
                    }
                    if (i == list2.size() - 1) {
                      checkLevenshteinDeleteFiltered(gson,list2,s);
                    }
                }
            } else {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                filteredList.clear();
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if (presentationList != null && !presentationList.getPresentationList().isEmpty()) {
                    list = presentationList.getPresentationList();
                    for (int i = 0; i < list.size(); i++) {
                        for (int j = 0; j < list.get(i).getKeywords().size(); j++) {
                            if (list.get(i).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list.get(i).getKeywords().get(j))<=levenshteinDistance) {
                                list.get(i).setFilteredPosition(i);
                                filteredList.add(list.get(i));
                                if (i == list.size() - 1){// && j == list.get(i).getKeywords().size() - 1) {
                                    if (filteredList.size() == 1) {
                                        list.remove(filteredList.get(0).getFilteredPosition());
                                        presentationList = new PresentationList();
                                        presentationList.setPresentationList(list);

                                        String json = gson.toJson(presentationList);
                                        editor.putString("list", json);
                                        editor.apply();

                                        if (list.isEmpty()) {
                                            removeLayout();
                                            reset();
                                            userQueryTextView.setText(showingCommandsText);
                                            displayImage();
                                        }else{
                                            listMoments();
                                        }
                                        Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                                    } else {
                                        new LoadFilteredMoments(filteredList).execute();
                                    }
                                    i = list.size();
                                    break;
                                }else
                                {
                                   j= list.get(i).getKeywords().size();
                                }
                            } else if (i == list.size() - 1 && j == list.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                                if (filteredList.size() == 1) {
                                    list.remove(filteredList.get(0).getFilteredPosition());
                                    presentationList = new PresentationList();
                                    presentationList.setPresentationList(list);

                                    String json = gson.toJson(presentationList);
                                    editor.putString("list", json);
                                    editor.apply();

                                    if (list.isEmpty()) {
                                        removeLayout();
                                        reset();
                                        userQueryTextView.setText(showingCommandsText);
                                        displayImage();
                                    }else{
                                        listMoments();
                                    }
                                    Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i = list.size();
                                break;
                            }
                        }
                        if (i == list.size() - 1) {
                           checkLevenshteinDelete(gson,list,s);
                        }
                    }
                } else {
                    Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
                }
            }
        }else {
            Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
        }
    }
    void checkLevenshteinDeleteFiltered(Gson gson,List<Presentation> list2,String s){
        int clearList = 0;
        for (int i = 0; i < list2.size(); i++) {
            for (int j = 0; j < list2.get(i).getKeywords().size(); j++) {
                if (Levenshtein.distance(s, list2.get(i).getKeywords().get(j))<=levenshteinDistance) {
                    //  list.get(i).setFilteredPosition(i);
                    if(clearList==0) {
                        filteredList.clear();
                        ++clearList;
                    }
                    filteredList.add(list2.get(i));
                    if (i == list2.size() - 1){// && j == list2.get(i).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            list.remove(filteredList.get(0).getFilteredPosition());
                            presentationList = new PresentationList();
                            presentationList.setPresentationList(list);

                            String json = gson.toJson(presentationList);
                            editor.putString("list", json);
                            editor.apply();

                            if (list.isEmpty()) {
                                removeLayout();
                                reset();
                                userQueryTextView.setText(showingCommandsText);
                                displayImage();
                            }else{
                                listMoments();
                            }
                            Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i = list2.size();
                        break;
                    }else{
                        j=list2.get(i).getKeywords().size();
                    }
                } else if (i == list2.size() - 1 && j == list2.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        list.remove(filteredList.get(0).getFilteredPosition());
                        presentationList = new PresentationList();
                        presentationList.setPresentationList(list);

                        String json = gson.toJson(presentationList);
                        editor.putString("list", json);
                        editor.apply();

                        if (list.isEmpty()) {
                            removeLayout();
                            reset();
                            userQueryTextView.setText(showingCommandsText);
                            displayImage();
                        }else{
                            listMoments();
                        }
                        Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i = list2.size();
                    break;
                }
            }
            if (i == list2.size() - 1) {
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }
    void checkLevenshteinDelete(Gson gson, List<Presentation> list,String s){
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getKeywords().size(); j++) {
                if (Levenshtein.distance(s, list.get(i).getKeywords().get(j))<=levenshteinDistance) {
                    list.get(i).setFilteredPosition(i);
                    filteredList.add(list.get(i));
                    if (i == list.size() - 1){// && j == list.get(i).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            list.remove(filteredList.get(0).getFilteredPosition());
                            presentationList = new PresentationList();
                            presentationList.setPresentationList(list);

                            String json = gson.toJson(presentationList);
                            editor.putString("list", json);
                            editor.apply();

                            if (list.isEmpty()) {
                                removeLayout();
                                reset();
                                userQueryTextView.setText(showingCommandsText);
                                displayImage();
                            }else{
                                listMoments();
                            }
                            Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i = list.size();
                        break;
                    }else
                    {
                        j= list.get(i).getKeywords().size();
                    }
                } else if (i == list.size() - 1 && j == list.get(i).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        list.remove(filteredList.get(0).getFilteredPosition());
                        presentationList = new PresentationList();
                        presentationList.setPresentationList(list);

                        String json = gson.toJson(presentationList);
                        editor.putString("list", json);
                        editor.apply();

                        if (list.isEmpty()) {
                            removeLayout();
                            reset();
                            userQueryTextView.setText(showingCommandsText);
                            displayImage();
                        }else{
                            listMoments();
                        }
                        Toast.makeText(this, "Moment deleted", Toast.LENGTH_LONG).show();
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i = list.size();
                    break;
                }
            }
            if (i == list.size() - 1) {
                listMoments();
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }
    void editShow(String s){
        if (!s.equalsIgnoreCase("")) {
            if (!filteredList.isEmpty()) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if (presentationList != null && !presentationList.getPresentationList().isEmpty()) {
                    list = presentationList.getPresentationList();
                }
                List<Presentation> list2 = new ArrayList<>();
                list2.addAll(filteredList);
                int clearList = 0;
                for (int i1 = 0; i1 < list2.size(); i1++) {
                    for (int j = 0; j < list2.get(i1).getKeywords().size(); j++) {
                        if (list2.get(i1).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list2.get(i1).getKeywords().get(j))<=levenshteinDistance) {
                            // list.get(i1).setFilteredPosition(i1);
                            if(clearList==0) {
                                filteredList.clear();
                                ++clearList;
                            }
                            filteredList.add(list2.get(i1));
                            if (i1 == list2.size() - 1){// && j == list2.get(i1).getKeywords().size() - 1) {
                                if (filteredList.size() == 1) {
                                    editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                                    selectedItems.clear();
                                    keywords.clear();
                                    for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                        selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                                    }
                                    for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                        keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                                    }
                                    if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                        selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                                    } else
                                        selfPicUri = null;

                                    AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                                    askEdit = true;
                                    //   speechOff = true;
                                    // stopSpeechRecognition();
                                    if(progressBar.getVisibility()==View.INVISIBLE)
                                    statusText.setText(dragText);
                                    new LoadGridTask(askEdit).execute();

                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i1 = list2.size();
                                break;
                            }else{
                                j=list2.get(i1).getKeywords().size();
                            }
                        } else if (i1 == list2.size() - 1 && j == list2.get(i1).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                            if (filteredList.size() == 1) {
                                editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                                selectedItems.clear();
                                keywords.clear();
                                for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                    selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                                }
                                for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                    keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                                }
                                if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                    selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                                } else
                                    selfPicUri = null;

                                AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                                askEdit = true;
                                //  speechOff = true;
                                // stopSpeechRecognition();
                                if(progressBar.getVisibility()==View.INVISIBLE)
                                statusText.setText(dragText);
                                new LoadGridTask(askEdit).execute();
                            } else {
                                new LoadFilteredMoments(filteredList).execute();
                            }
                            i1 = list2.size();
                            break;
                        }
                    }
                    if (i1 == list2.size() - 1) {
                       checkLevenshteinEditFiltered(gson,list2,s);
                    }
                }
            } else {
                Gson gson = new GsonBuilder().serializeNulls().create();
                presentationList = gson.fromJson(pref.getString("list", null), PresentationList.class);
                list.clear();
                filteredList.clear();
                if (countDownTimerListMoments != null)
                    countDownTimerListMoments.cancel();
                if (presentationList != null && !presentationList.getPresentationList().isEmpty()) {
                    list = presentationList.getPresentationList();
                    for (int i1 = 0; i1 < list.size(); i1++) {
                        for (int j = 0; j < list.get(i1).getKeywords().size(); j++) {
                            if (list.get(i1).getKeywords().get(j).equalsIgnoreCase(s)){// || Levenshtein.distance(s, list.get(i1).getKeywords().get(j))<=levenshteinDistance) {
                                list.get(i1).setFilteredPosition(i1);
                                filteredList.add(list.get(i1));
                                if (i1 == list.size() - 1){// && j == list.get(i1).getKeywords().size() - 1) {
                                    if (filteredList.size() == 1) {
                                        editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                                        selectedItems.clear();
                                        keywords.clear();
                                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                            selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                                        }
                                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                            keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                                        }
                                        if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                            selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                                        } else
                                            selfPicUri = null;

                                        AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                                        askEdit = true;
                                        if(progressBar.getVisibility()==View.INVISIBLE)
                                        statusText.setText(dragText);
                                        new LoadGridTask(askEdit).execute();

                                    } else {
                                        new LoadFilteredMoments(filteredList).execute();
                                    }
                                    i1 = list.size();
                                    break;
                                }
                                else{
                                    j=list.get(i1).getKeywords().size();
                                }
                            } else if (i1 == list.size() - 1 && j == list.get(i1).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                                if (filteredList.size() == 1) {
                                    editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                                    selectedItems.clear();
                                    keywords.clear();
                                    for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                        selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                                    }
                                    for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                        keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                                    }
                                    if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                        selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                                    } else
                                        selfPicUri = null;

                                    AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                                    askEdit = true;
                                    if(progressBar.getVisibility()==View.INVISIBLE)
                                    statusText.setText(dragText);
                                    new LoadGridTask(askEdit).execute();
                                } else {
                                    new LoadFilteredMoments(filteredList).execute();
                                }
                                i1 = list.size();
                                break;
                            }
                        }
                        if (i1 == list.size() - 1) {
                          checkLevenshteinEdit(list,s);
                        }
                    }
                } else {
                    Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
                }
            }
        }else {
            Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
        }
    }
    void checkLevenshteinEditFiltered(Gson gson,List<Presentation> list2,String s){
        int clearList = 0;
        for (int i1 = 0; i1 < list2.size(); i1++) {
            for (int j = 0; j < list2.get(i1).getKeywords().size(); j++) {
                if (Levenshtein.distance(s, list2.get(i1).getKeywords().get(j))<=levenshteinDistance) {
                    // list.get(i1).setFilteredPosition(i1);
                    if(clearList==0) {
                        filteredList.clear();
                        ++clearList;
                    }
                    filteredList.add(list2.get(i1));
                    if (i1 == list2.size() - 1){// && j == list2.get(i1).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                            selectedItems.clear();
                            keywords.clear();
                            for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                            }
                            for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                            }
                            if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                            } else
                                selfPicUri = null;

                            AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                            askEdit = true;
                            //   speechOff = true;
                            // stopSpeechRecognition();
                            if(progressBar.getVisibility()==View.INVISIBLE)
                            statusText.setText(dragText);
                            new LoadGridTask(askEdit).execute();

                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i1 = list2.size();
                        break;
                    }else{
                        j=list2.get(i1).getKeywords().size();
                    }
                } else if (i1 == list2.size() - 1 && j == list2.get(i1).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                        selectedItems.clear();
                        keywords.clear();
                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                            selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                        }
                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                            keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                        }
                        if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                            selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                        } else
                            selfPicUri = null;

                        AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                        askEdit = true;
                        //  speechOff = true;
                        // stopSpeechRecognition();
                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(dragText);
                        new LoadGridTask(askEdit).execute();
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i1 = list2.size();
                    break;
                }
            }
            if (i1 == list2.size() - 1) {
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }
    void checkLevenshteinEdit(List<Presentation> list, String s)
    {
        for (int i1 = 0; i1 < list.size(); i1++) {
            for (int j = 0; j < list.get(i1).getKeywords().size(); j++) {
                if (Levenshtein.distance(s, list.get(i1).getKeywords().get(j))<=levenshteinDistance) {
                    list.get(i1).setFilteredPosition(i1);
                    filteredList.add(list.get(i1));
                    if (i1 == list.size() - 1){// && j == list.get(i1).getKeywords().size() - 1) {
                        if (filteredList.size() == 1) {
                            editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                            selectedItems.clear();
                            keywords.clear();
                            for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                                selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                            }
                            for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                                keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                            }
                            if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                                selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                            } else
                                selfPicUri = null;

                            AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                            askEdit = true;
                            if(progressBar.getVisibility()==View.INVISIBLE)
                            statusText.setText(dragText);
                            new LoadGridTask(askEdit).execute();

                        } else {
                            new LoadFilteredMoments(filteredList).execute();
                        }
                        i1 = list.size();
                        break;
                    }
                    else{
                        j=list.get(i1).getKeywords().size();
                    }
                } else if (i1 == list.size() - 1 && j == list.get(i1).getKeywords().size() - 1 && !filteredList.isEmpty()) {
                    if (filteredList.size() == 1) {
                        editPosition = String.valueOf(filteredList.get(0).getFilteredPosition());
                        selectedItems.clear();
                        keywords.clear();
                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().size(); i++) {
                            selectedItems.add(Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelectedItems().get(i)));
                        }
                        for (int i = 0; i < list.get(filteredList.get(0).getFilteredPosition()).getKeywords().size(); i++) {
                            keywords.add(list.get(filteredList.get(0).getFilteredPosition()).getKeywords().get(i));
                        }
                        if (list.get(filteredList.get(0).getFilteredPosition()).getSelfPic() != null) {
                            selfPicUri = Uri.parse(list.get(filteredList.get(0).getFilteredPosition()).getSelfPic());
                        } else
                            selfPicUri = null;

                        AudioSavePathInDevice = list.get(filteredList.get(0).getFilteredPosition()).getAudioSavedPath();
                        askEdit = true;
                        if(progressBar.getVisibility()==View.INVISIBLE)
                        statusText.setText(dragText);
                        new LoadGridTask(askEdit).execute();
                    } else {
                        new LoadFilteredMoments(filteredList).execute();
                    }
                    i1 = list.size();
                    break;
                }
            }
            if (i1 == list.size() - 1) {
                listMoments();
                Toast.makeText(this, noShowFound, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean isOffline(Context thisActivity) {
        ConnectivityManager connMgr = (ConnectivityManager) thisActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return true;
        }
        return false;
    }


    //Firebase
    public class FirebaseTask extends AsyncTask<Void, Void, Void> {

        private String imei;

        public FirebaseTask(String imei) {
            this.imei = imei;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                System.out.println("Update Firebase");
                Gson gson=new GsonBuilder().serializeNulls().create();
                PresentationList presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
                if(presentationList!=null && !presentationList.getPresentationList().isEmpty()) {
                  firebaseAuth(imei,presentationList);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    void firebaseAuth(final String imei, final PresentationList presentationList){
        mAuth.signInWithEmailAndPassword(Config.AUTH_EMAIL, Config.AUTH_PASSWORD)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithEmail:success");
                            List<Presentation> list = presentationList.getPresentationList();
                            int totalViewCount=0,showsPlayed=0;
                            for(int i=0;i<list.size();i++){
                                if(list.get(i).getNumOfTimesPlayed()!=null) {
                                    totalViewCount = totalViewCount + Integer.valueOf(list.get(i).getNumOfTimesPlayed());
                                    showsPlayed++;
                                }
                                if(i==list.size()-1){
                                    String date;
                                    if(list.get(0).getModifiedDate()!=null)
                                        date=list.get(0).getModifiedDate();
                                    else
                                       date=list.get(0).getCreatedDate();

                                    db.collection("Eva Moments/"+imei+"/"+pref.getString("firebaseRegDate",""))
                                            .document(pref.getString("firebaseDocId","")).update(
                                            "Last used", new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()),
                                            "Show Count",list.size(),
                                            "View Count", totalViewCount,
                                            "Number of Shows Played",showsPlayed,
                                            "Last Created or Modified",date,
                                            "App release", BuildConfig.VERSION_NAME
                                    ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                           System.out.println("firebase update sucess");
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("firebase update failure: "+e.getMessage());
                                                }
                                            });
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "signInWithEmail:failure", task.getException());
                       }
                    }
                });
    }
}
