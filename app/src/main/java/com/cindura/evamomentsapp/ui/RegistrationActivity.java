package com.cindura.evamomentsapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cindura.evamomentsapp.BuildConfig;
import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.helper.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

//Activity to show the registration page and register the user
public class RegistrationActivity extends AppCompatActivity {
    private String imei,familyUri,autoAuth;
    private FirebaseAuth mAuth;
    private int askPermissionCount=0;
    private Button submit;
    private SharedPreferences pref;
    private ImageView familyPic;
    private SharedPreferences.Editor editor;
    private EditText name,email,city,state,country;
    private String nameText,emailText,cityText,statetext,countryText;
    private ProgressBar progressBar;
    //firebase
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAuth = FirebaseAuth.getInstance();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        autoAuth=pref.getString("autoAuth",null);
        imei=pref.getString("imei",null);
        int pitch = pref.getInt("pitch", 0);
        int speed = pref.getInt("speed", 0);
        int mediaPlayerVolume = pref.getInt("mediaPlayerVolume", 0);
        int speechVolume = pref.getInt("speechVolume", 0);

        submit=findViewById(R.id.submit);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        city=findViewById(R.id.city);
        state=findViewById(R.id.state);
        country=findViewById(R.id.country);
        familyPic=findViewById(R.id.familyPic);
        progressBar=findViewById(R.id.progress_loader);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 101);
            } else {
                if (imei == null)
                    imei = "Eva3" + UUID.randomUUID().toString().substring(0, 20);
            }
        }
        else {
            if (imei == null)
                imei = "Eva3" + UUID.randomUUID().toString().substring(0, 20);
        }

        if (mediaPlayerVolume == 0 && speechVolume == 0 && speed == 0 && pitch == 0) {
            editor.putInt("speed", 85);
            editor.putInt("pitch", 80);
            editor.putInt("mediaPlayerVolume", 70);
            editor.putInt("speechVolume",0);
            editor.putInt("preludeVolume",100);
            editor.putString("autoSwipe", "true");
            editor.putInt("delayPhotoAlbum",3);
            editor.putString("navigationDelay","30");
            editor.putString("playback","false");
            editor.putString("repeat","0");
            editor.apply();
        }
        if (autoAuth!=null) {
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if(submit.getText().equals(getResources().getString(R.string.submit_button_text))){
                    if(!name.getText().toString().trim().equalsIgnoreCase("") && !email.getText().toString().trim().equalsIgnoreCase("")
                    && !city.getText().toString().trim().equalsIgnoreCase("") && !state.getText().toString().trim().equalsIgnoreCase("")
                    && !country.getText().toString().trim().equalsIgnoreCase("")) {
                        progressBar.setVisibility(View.VISIBLE);
                        showNoInternetDialog();
                       firebaseAuth();
                    }
                    else
                        Toast.makeText(RegistrationActivity.this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
                }else{
                    nameText=name.getText().toString().trim();
                    emailText=email.getText().toString().trim();
                    cityText= city.getText().toString().trim();
                    statetext=state.getText().toString().trim();
                    countryText=country.getText().toString().trim();

                    Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 2);
                }
            }
        });
    }
    void showNoInternetDialog(){
        boolean isNetworkAvailable= isOffline(RegistrationActivity.this);
        if(!isNetworkAvailable) {
            try {
                progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(RegistrationActivity.this)
                        .setTitle("Internet Error!")
                        .setMessage("Please check your internet connection.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }catch (Exception e1){
                e1.printStackTrace();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        try {
                            RegistrationActivity.this.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        familyPic.setVisibility(View.VISIBLE);
                        Cursor returnCursor =
                                getContentResolver().query(uri, null, null, null, null);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                        long size = returnCursor.getLong(sizeIndex);

                        System.out.println("file size: " + size);
                        if (size < 100000) {
                            familyPic.setImageURI(uri);
                        } else
                            familyPic.setImageBitmap(getBitmap(uri));
                        familyUri = uri.toString();

                        name.setText(nameText);
                        email.setText(emailText);
                        city.setText(cityText);
                        state.setText(statetext);
                        country.setText(countryText);
                       submit.setText(getResources().getString(R.string.submit_button_text));
                    }
                }
                break;
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
    //This method will be called when the user will tap on allow or deny
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Checking the request code of our request
        if (requestCode == 101) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 101);
                } else if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 101);
                }
                if (imei == null)
                    imei = "Eva3" + UUID.randomUUID().toString().substring(0, 20);
            }else {
                askPermissionCount++;
                if(askPermissionCount<=3) {
                    if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 101);
                    } else if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 101);
                    }
                }
            }
        }
    }

    //Firebase
    public class FirebaseTask extends AsyncTask<Void, Void, Void> {

        private String submit;

        public FirebaseTask(String submit) {
            this.submit = submit;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                final Map<String, Object> objPatient = new HashMap<>();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        objPatient.put("Name", name.getText().toString().trim());
                        objPatient.put("Email",email.getText().toString().trim());
                        objPatient.put("City", city.getText().toString().trim());
                        objPatient.put("State",state.getText().toString().trim());
                        objPatient.put("Country",country.getText().toString().trim());
                        objPatient.put("App release", BuildConfig.VERSION_NAME);
                    }
                });

                db = FirebaseFirestore.getInstance();

                final String date2=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                db.collection("Eva Moments/" +imei+"/"+ date2).add(objPatient)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.w("DocumentSnapshot", "success adding document");
                                editor.putString("firebaseRegDate",date2);
                                editor.putString("firebaseDocId",documentReference.getId());
                                editor.putString("imei",imei);
                                editor.putString("name",name.getText().toString().trim());
                                editor.putString("date",new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                                editor.putString("familyPic",familyUri);
                                editor.putString("autoAuth","true");
                                editor.apply();
                                progressBar.setVisibility(View.GONE);
                                new CountDownTimer(2000, 1000) {
                                    public void onFinish() {
                                        Toast.makeText(RegistrationActivity.this, "Installation Successful.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }

                                    public void onTick(long millisUntilFinished) {
                                        // millisUntilFinished    The amount of time until finished.
                                    }
                                }.start();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Log.w("DocumentSnapshot", "Error adding document", e);
                                Toast.makeText(RegistrationActivity.this, "Please check your internet connection.", Toast.LENGTH_LONG).show();
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

    void hideKeyboard(){
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    void firebaseAuth(){
        mAuth.signInWithEmailAndPassword(Config.AUTH_EMAIL, Config.AUTH_PASSWORD)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithEmail:success");
                            new FirebaseTask(submit.getText().toString()).execute();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "signInWithEmail:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegistrationActivity.this, "Please check your internet connection.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
