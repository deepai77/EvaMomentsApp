package com.cindura.evamomentsapp.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.adapter.SliderAdapterPhotoAlbum;
import com.cindura.evamomentsapp.model.Presentation;
import com.cindura.evamomentsapp.model.PresentationList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

//The activity which plays the show
public class PlayMomentActivity extends AppCompatActivity {
    private ImageView imageButton;
    private String mimeType2;
    private TextView title;
    private Handler myHandler;
    private Runnable myRunnable;
    private int position;
    private List<Uri> selectedItems;
    private PresentationList presentationList;
    private List<Presentation> list;
    String AudioSavePathInDevice = null;
    private Uri selfPicUri;
    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private AudioManager am;
    private CountDownTimer timer,autoSwipeTimer;
    private int currentPagePhotoAlbum;
    private boolean videoCompleted;
    private int repeat;
    private String speaker;

    //SharedPreference
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_moment);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        repeat= Integer.parseInt(pref.getString("repeat",null));
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        list=new ArrayList<>();
        selectedItems=new ArrayList<>();
        imageButton=findViewById(R.id.backButton);
        title=findViewById(R.id.title);

        Intent intent=getIntent();
        position=intent.getIntExtra("position",position);
        speaker=intent.getStringExtra("speaker");

        Gson gson = new GsonBuilder().serializeNulls().create();
        presentationList= gson.fromJson(pref.getString("list", null), PresentationList.class);
        if(presentationList!=null && !presentationList.getPresentationList().isEmpty())
            list=presentationList.getPresentationList();

        String key="";
        for(int i=0;i<list.get(position).getKeywords().size();i++){
            key=key+list.get(position).getKeywords().get(i)+", ";
        }
        title.setText(key.substring(0,key.length()-2));
        for(int i=0;i<list.get(position).getSelectedItems().size();i++) {
            selectedItems.add(Uri.parse(list.get(position).getSelectedItems().get(i)));
        }

        if(list.get(position).getSelfPic()!=null) {
            selfPicUri = Uri.parse(list.get(position).getSelfPic());
          }
        else
            selfPicUri=null;

        AudioSavePathInDevice=list.get(position).getAudioSavedPath();

        if(speaker==null) {
            String num = presentationList.getPresentationList().get(position).getNumOfTimesPlayed();
            presentationList.getPresentationList().get(position).setLastPlayed(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
            if (num == null)
                presentationList.getPresentationList().get(position).setNumOfTimesPlayed("1");
            else {
                int i = Integer.parseInt(num);
                ++i;
                presentationList.getPresentationList().get(position).setNumOfTimesPlayed(String.valueOf(i));
            }
            String json = gson.toJson(presentationList);
            editor.putString("list", json);
            editor.apply();
        }

        playPrelude();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
    }
    void playPrelude(){
        removeLayout();
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
                mMMR.setDataSource(PlayMomentActivity.this, selectedItems.get(0));
                Bitmap bitmap = mMMR.getFrameAtTime();
                imageView.setImageBitmap(bitmap);
                videoIcon.setVisibility(View.VISIBLE);
            }
        }

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mediaPlayer = new MediaPlayer();
        int mediaPlayerProgress = pref.getInt("mediaPlayerVolume", 0);
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
                    Toast.makeText(PlayMomentActivity.this, "Failed to play prelude.", Toast.LENGTH_LONG).show();
                    if(speaker==null)
                    showAlbum();
                    else
                        finishActivity();
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer=null;
                    if(speaker==null)
                    showAlbum();
                    else
                        finishActivity();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Prelude not found.", Toast.LENGTH_LONG).show();
            if(mediaPlayer!=null)
                mediaPlayer.release();
            if(speaker==null)
                showAlbum();
            else
                finishActivity();
        }
    }
    void removeLayout() {
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.viewGiftConatiner);
        insertPoint.removeAllViews();
    }

    void showAlbum() {
        currentPagePhotoAlbum=0;
        removeLayout();
        final LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        final View v = vi.inflate(R.layout.photo_album, null);

        final ViewPager viewPager = v.findViewById(R.id.photo_album_view);
        final ImageView leftSwipePhotoAlbum = v.findViewById(R.id.leftSwipePhotoAlbum);
        final ImageView rightSwipePhotoAlbum = v.findViewById(R.id.rightSwipePhotoAlbum);
        SliderAdapterPhotoAlbum sliderAdapterPhotoAlbum = new SliderAdapterPhotoAlbum(PlayMomentActivity.this, selectedItems);
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

        if(selfPicUri==null && mimeType!=null &&mimeType.contains("image")&& selectedItems.size()>1) {
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
                if(selfPicUri==null && mimeType!=null &&mimeType.contains("image")) {
                    videoView = (VideoView) viewPager.findViewWithTag("video1");
                    if(videoView!=null)
                    videoView.setVideoURI(selectedItems.get(1));
                }else {
                    videoView = (VideoView) viewPager.findViewWithTag("video0");
                    if(videoView!=null)
                    videoView.setVideoURI(selectedItems.get(0));
                }
                if(videoView!=null) {
                    if(myHandler!=null && myRunnable!=null)
                        myHandler.removeCallbacks(myRunnable);
                    final MediaController mediaController=new MediaController(PlayMomentActivity.this,false);
                    mediaController.setBackgroundColor(getResources().getColor(R.color.media_controller_background));
                    RelativeLayout relative=viewPager.findViewWithTag("r"+position);
                    mediaController.setAnchorView(relative);
                    videoView.setMediaController(mediaController);
                    myHandler = new Handler();
                    myRunnable = new Runnable() {
                        @Override
                        public void run() {
                           try {
                               mediaController.show();
                           }catch (Exception e){
                               e.printStackTrace();
                           }
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
                                finishActivity();
                            }
                            else if(selectedItems.size()==2 && selfPicUri==null &&mimeType!=null&& mimeType.contains("image")){
                               finishActivity();
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

        if(selectedItems.size()==2 && selfPicUri==null && mimeType!=null&&mimeType.contains("image") && videoCompleted){
            timer=new CountDownTimer(3000, 1000) {
                public void onFinish() {
                    finishActivity();
                }

                public void onTick(long millisUntilFinished) {
                    // millisUntilFinished    The amount of time until finished.
                }
            }.start();
        }
        else if(selectedItems.size()>1)
            rightSwipePhotoAlbum.setVisibility(View.VISIBLE);
        else if(selectedItems.size()==1 && selfPicUri==null &&mimeType!=null&& mimeType.contains("image")){
            finishActivity();
        }
        else if(selectedItems.size()==1 && mimeType!=null&&mimeType.contains("image")){
            timer=new CountDownTimer(3000, 1000) {
                public void onFinish() {
                   finishActivity();
                }

                public void onTick(long millisUntilFinished) {
                    // millisUntilFinished    The amount of time until finished.
                }
            }.start();
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

                if(mimeType!=null&&mimeType.contains("image"))
                    videoCompleted=true;
                else
                    videoCompleted=false;

                if(videoView!=null && videoView.isPlaying()){
                    videoView.stopPlayback();
                }

                currentPagePhotoAlbum = position;
                videoView=(VideoView) viewPager.findViewWithTag("video"+position);
                if(videoView!=null){
                 final MediaController mediaController=new MediaController(PlayMomentActivity.this,false);
                    mediaController.setBackgroundColor(getResources().getColor(R.color.media_controller_background));
                    RelativeLayout relative=viewPager.findViewWithTag("r"+position);
                    videoView.setVideoURI(selectedItems.get(position));
                    mediaController.setAnchorView(relative);
                    videoView.setMediaController(mediaController);
                    myHandler = new Handler();
                    myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaController.show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
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
                                if(repeat!=0){
                                    --repeat;
                                    playPrelude();
                                }else
                               finishActivity();
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

                    if(repeat!=0){
                        --repeat;
                        playPrelude();
                    }else
                    timer=new CountDownTimer(3000, 1000) {
                        public void onFinish() {
                            ContentResolver cR = getContentResolver();
                            final String mimeType=cR.getType(selectedItems.get(selectedItems.size() - 1));

                            if(mimeType!=null&&mimeType.contains("image"))
                            finishActivity();
                        }

                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start();
                } else {
                    leftSwipePhotoAlbum.setVisibility(View.VISIBLE);
                    rightSwipePhotoAlbum.setVisibility(View.VISIBLE);
                }

                autoSwipePhotoAlbum(viewPager,currentPagePhotoAlbum,selectedItems.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(viewListenerPhotoAlbum);

         autoSwipePhotoAlbum(viewPager,currentPagePhotoAlbum,selectedItems.size());
    }
    public void autoSwipePhotoAlbum(final ViewPager slideViewPagerPhotoAlbum,final int currentPagePhotoAlbum2, final int photoAlbumUriLength){
        if(autoSwipeTimer!=null)
            autoSwipeTimer.cancel();
        int delay = pref.getInt("delayPhotoAlbum",0);
        if(delay==0){
            delay=3;
        }
        if (currentPagePhotoAlbum != selectedItems.size()) {
            autoSwipeTimer=new CountDownTimer(delay * 1000, 1000) {
                public void onFinish() {

                    int finalCurrentPagePhotoAlbum = currentPagePhotoAlbum;
                    ContentResolver cR = getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String mimeType="";
                    if(finalCurrentPagePhotoAlbum+1 != selectedItems.size()) {
                        mimeType = cR.getType(selectedItems.get(finalCurrentPagePhotoAlbum++));
                        if (mimeType==null || mimeType.contains("image") || (mimeType.contains("video") && videoCompleted)) {
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
    protected void onDestroy() {
        super.onDestroy();
        if(myHandler!=null && myRunnable!=null)
            myHandler.removeCallbacks(myRunnable);
        if(timer!=null){
            timer.cancel();
        }
        if(autoSwipeTimer!=null)
            autoSwipeTimer.cancel();
    }

    void finishActivity(){
        if(myHandler!=null && myRunnable!=null)
            myHandler.removeCallbacks(myRunnable);
        if(timer!=null){
            timer.cancel();
        }
        if(autoSwipeTimer!=null)
            autoSwipeTimer.cancel();
        finish();
    }
}
