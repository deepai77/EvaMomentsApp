package com.cindura.evamomentsapp.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.adapter.GridAdapter;
import com.cindura.evamomentsapp.helper.Config;
import com.cindura.evamomentsapp.helper.ItemMoveCallback;
import com.cindura.evamomentsapp.model.Presentation;
import com.cindura.evamomentsapp.model.PresentationList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

//Activity which shows Settings Page
public class SettingsActivity extends AppCompatActivity implements
        RecognitionListener {
    private boolean callOnce;
    private ImageView progressBar;
    private TextView userQueryTextView;
    private Handler handlerMicrophone;
    private Runnable mRunnableMicrophone;
    private String listeningText="Say a command...";
    private String processingText="";
    private ImageView listening;
    private TextView statusText;
    private int microphoneOnCount;
    private Switch switchAutoSwipe,switchPlayback;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private String mimeType2;
    private ImageView imageView;
    private Button setDefaultButton, deregisterButton, familyPicButton;
    private ImageView backButton;
    private String name, familyPic;
    private TextView speechVolumeIndicator, playerVolumeIndicator, pitchIndicator, speedIndicator,deviceRegDate, deviceUserName;
    private SeekBar speechVolume, mediaPlayerVolume, pitchSeekBar, speedSeekBar;
    private AudioManager audioManager;
    private Spinner spinnerDelayPhotoAlbum, spinnerNavigation, spinnerRepeat;
    private String[] delayNavigation = {"5","10","15","20","25","30","35","40","45","50","55","60"};
    private String[] photoAlbumDelay = {"1","3", "5", "10", "15", "20", "25", "30"};
    private String[] playbackRepeat={"0","1","2","3"};
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Handler myHandler;
    private Runnable myRunnable;
    private String navigationDelayValue,repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        userQueryTextView=findViewById(R.id.userQuery);
        listening = findViewById(R.id.listening);
        statusText = findViewById(R.id.statusText);
        name=pref.getString("name","");
        familyPic = pref.getString("familyPic", "");
        String deviceRegDateText = pref.getString("date", "");
        int mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
        int speechVolumeProgress = pref.getInt("speechVolume", 0);
        int pitch = pref.getInt("pitch", 0);
        int speed = pref.getInt("speed", 0);
        String autoSwipe = pref.getString("autoSwipe", null);
        String playback=pref.getString("playback",null);
        int delay = pref.getInt("delayPhotoAlbum", 0);
        navigationDelayValue = pref.getString("navigationDelay", "");
        repeat=pref.getString("repeat","");

        imageView=findViewById(R.id.imageView);
        ContentResolver cR2 = getContentResolver();
        if(familyPic!=null)
        mimeType2=cR2.getType(Uri.parse(familyPic));
        if(mimeType2!=null) {
            Cursor returnCursor =
                    getContentResolver().query(Uri.parse(familyPic), null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: " + size);
            if (size < 100000) {
                imageView.setImageURI(Uri.parse(familyPic));
            } else
                imageView.setImageBitmap(getBitmap(Uri.parse(familyPic)));
        }else{
            imageView.setImageResource(R.drawable.media_not_found_image);
        }

        deviceRegDate = findViewById(R.id.deviceRegDate);
        deviceUserName = findViewById(R.id.userName);
        deregisterButton = findViewById(R.id.deregisterButton);
        familyPicButton=findViewById(R.id.familyPicButton);
        progressBar=findViewById(R.id.progress_loader);
        deviceUserName.setText(name);
        deviceRegDate.setText(deviceRegDateText);

        spinnerNavigation = findViewById(R.id.spinnerDelayNavigation);
        spinnerRepeat=findViewById(R.id.spinnerRepeat);
        switchPlayback=findViewById(R.id.switchPlayback);
        spinnerDelayPhotoAlbum = findViewById(R.id.spinnerDelayPhotoAlbum);
        setDefaultButton = findViewById(R.id.defaultButton);
        speechVolume = findViewById(R.id.speechVolume);
        mediaPlayerVolume = findViewById(R.id.audioPlayerVolume);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        backButton = findViewById(R.id.backButton);
        speechVolumeIndicator = findViewById(R.id.speechVolumeIndicator);
        playerVolumeIndicator = findViewById(R.id.audioVolumeIndicator);
        pitchIndicator = findViewById(R.id.pitchIndicator);
        speedIndicator = findViewById(R.id.speedIndicator);
        switchAutoSwipe = findViewById(R.id.switchButtonAutoSwipe);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Check if microphone on
        handlerMicrophone = new Handler();
         mRunnableMicrophone = new Runnable() {
            @Override
            public void run() {
                if (microphoneOnCount > 1) {
                    System.out.println("microphone turned on...");
                    Animation pulse = AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.pulse);
                    listening.startAnimation(pulse);
                    userQueryTextView.setVisibility(View.VISIBLE);
                    ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.green)));
                    listening.setVisibility(View.VISIBLE);
                    statusText.setText(listeningText);

                    microphoneOnCount = 0;
                    resetSpeechRecognizer();
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }  else
                    microphoneOnCount++;
                handlerMicrophone.postDelayed(this, 5L * 1000L);
            }
        };
        handlerMicrophone.post(mRunnableMicrophone);

       //Photo Album delay
        spinnerDelayPhotoAlbum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("delayPhotoAlbum", Integer.parseInt(photoAlbumDelay[position]));
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter aa2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, photoAlbumDelay);
        aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDelayPhotoAlbum.setAdapter(aa2);
        for (int i = 0; i < photoAlbumDelay.length; i++) {
            if (Integer.parseInt(photoAlbumDelay[i]) == delay) {
                spinnerDelayPhotoAlbum.setSelection(i);
            }
        }

        familyPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myHandler.removeCallbacks(myRunnable);

                Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 2);
            }
        });
        deregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove("list");
                editor.remove("autoAuth");
                editor.remove("familyPic");
                editor.remove("firstLaunch");
                editor.apply();

                Toast.makeText(SettingsActivity.this, "Successfully unregistered. Please Login again.", Toast.LENGTH_LONG).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(SettingsActivity.this, RegistrationActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }, 2000);
                    }
        });

        //Screen Navigation delay
        spinnerNavigation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putString("navigationDelay", delayNavigation[position]);
                editor.apply();
                navigationDelayValue = pref.getString("navigationDelay", "");
                myHandler.removeCallbacks(myRunnable);
                myHandler.postDelayed(myRunnable, Long.parseLong(navigationDelayValue) * 1000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter aa4 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, delayNavigation);
        aa4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNavigation.setAdapter(aa4);
        for (int i = 0; i < delayNavigation.length; i++) {
            if (delayNavigation[i].equals(navigationDelayValue)) {
                spinnerNavigation.setSelection(i);
            }
        }

        //Repeat Playback
        spinnerRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putString("repeat", playbackRepeat[position]);
                editor.apply();
              }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter aa5 = new ArrayAdapter(this, android.R.layout.simple_spinner_item,playbackRepeat);
        aa5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(aa5);
        for (int i = 0; i < playbackRepeat.length; i++) {
            if (playbackRepeat[i].equals(repeat)) {
                spinnerRepeat.setSelection(i);
            }
        }

        if (autoSwipe != null && autoSwipe.equals("true")) {
            switchAutoSwipe.setChecked(true);
        } else {
            switchAutoSwipe.setChecked(false);
        }
        if (playback != null && playback.equals("true")) {
            switchPlayback.setChecked(true);
        } else {
            switchPlayback.setChecked(false);
        }

        if (mediaPlayerProgress != 0) {
            mediaPlayerVolume.setProgress(mediaPlayerProgress);
            playerVolumeIndicator.setText(String.valueOf(mediaPlayerProgress * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100));
        } else {
            mediaPlayerVolume.setProgress(0);
            playerVolumeIndicator.setText("0");
        }
        if (speechVolumeProgress != 0) {
            speechVolume.setProgress(speechVolumeProgress);
            speechVolumeIndicator.setText(String.valueOf(speechVolumeProgress * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100));
        } else {
            speechVolume.setProgress(0);
            speechVolumeIndicator.setText("0");
        }
        if (pitch != 0) {
            pitchIndicator.setText(String.valueOf((float) pitch / 100));
            pitchSeekBar.setProgress(pitch);
        }
        if (speed != 0) {
            speedIndicator.setText(String.valueOf((float) speed / 100));
            speedSeekBar.setProgress(speed);
        }

        switchAutoSwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putString("autoSwipe", "true");
                    editor.apply();
                } else {
                    editor.remove("autoSwipe");
                    editor.apply();
                }
            }
        });
        switchPlayback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putString("playback", "true");
                    editor.apply();
                } else {
                    editor.remove("playback");
                    editor.apply();
                }
            }
        });

        speechVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speechVolumeIndicator.setText(String.valueOf(progress * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100));
                editor.putInt("speechVolume", progress);
                editor.apply();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (progress * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mediaPlayerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playerVolumeIndicator.setText(String.valueOf(progress * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100));
                editor.putInt("mediaPlayerVolume", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pitchIndicator.setText(String.valueOf((float) progress / 100));
                editor.putInt("pitch", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedIndicator.setText(String.valueOf((float) progress / 100));
                editor.putInt("speed", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("speed", 85);
                editor.putInt("pitch", 80);
                editor.putInt("mediaPlayerVolume", 70);
                editor.putInt("speechVolume", 0);
                editor.putInt("maxWords", 10);
                editor.putInt("delayPhotoAlbum", 1);
                editor.putString("navigationDelay", "30");
                switchAutoSwipe.setChecked(true);
                mediaPlayerVolume.setProgress(70);
                pitchSeekBar.setProgress(80);
                speechVolume.setProgress(0);
                speedSeekBar.setProgress(85);
                spinnerDelayPhotoAlbum.setSelection(1);
                spinnerRepeat.setSelection(0);
                switchPlayback.setChecked(false);
                spinnerNavigation.setSelection(0);
                editor.apply();
            }
        });
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
        myHandler.postDelayed(myRunnable, Long.parseLong(navigationDelayValue) * 1000);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        myHandler.removeCallbacks(myRunnable);
        myHandler.postDelayed(myRunnable, Long.parseLong(navigationDelayValue) * 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacks(myRunnable);
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        SettingsActivity.this.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }
                    Cursor returnCursor =
                            getContentResolver().query(uri, null, null, null, null);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    long size = returnCursor.getLong(sizeIndex);

                    System.out.println("file size: "+size);
                    if(size < 100000) {
                        imageView.setImageURI(uri);
                    }
                    else
                        imageView.setImageBitmap(getBitmap(uri));
                    editor.putString("familyPic",uri.toString());
                    editor.apply();

                    myHandler.postDelayed(myRunnable, Long.parseLong(navigationDelayValue) * 1000);

                }
                break;
        }
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
        Log.d("Speech Settings","onError: "+errorMessage);
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
            callOnce=false;
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String userQuery = matches.get(0);
            if(!userQuery.equals(" ") && !userQuery.equals("")) {
                for (int i = 0; i < matches.size(); i++) {
                    if (matches.get(i).equalsIgnoreCase("Eva Community") ||matches.get(i).equalsIgnoreCase("Quit") || matches.get(i).equalsIgnoreCase("Close") ||
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
                    boolean isNetworkAvailable= isOffline(SettingsActivity.this);
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
                }else if (userQuery.equalsIgnoreCase("list shows") ||
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
                 else if (userQuery.toLowerCase().contains("show commands") || userQuery.equalsIgnoreCase("help")) {
                  stopSpeechRecognition();
                    if(handlerMicrophone!=null && mRunnableMicrophone!=null)
                        handlerMicrophone.removeCallbacks(mRunnableMicrophone);
                    Intent intent = new Intent(SettingsActivity.this, ShowCommandsActivity.class);
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
        handlerMicrophone.postDelayed(mRunnableMicrophone, 5L * 1000L);
        myHandler.postDelayed(myRunnable, Long.parseLong(navigationDelayValue) * 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(handlerMicrophone!=null && mRunnableMicrophone!=null)
            handlerMicrophone.removeCallbacks(mRunnableMicrophone);
        myHandler.removeCallbacks(myRunnable);
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
            resetSpeechRecognizer();
            setRecogniserIntent();
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        Animation pulse = AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.pulse);
        listening.startAnimation(pulse);
        userQueryTextView.setVisibility(View.VISIBLE);
        ImageViewCompat.setImageTintList(listening, ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.green)));
        listening.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        statusText.setText(listeningText);
    }
    void setInstructions(String action){
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.putExtra("Action",action);
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
