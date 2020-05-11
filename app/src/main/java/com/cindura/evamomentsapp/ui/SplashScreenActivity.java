package com.cindura.evamomentsapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.cindura.evamomentsapp.R;

public class SplashScreenActivity extends AppCompatActivity {
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        Thread timer=new Thread(){
            @Override
            public void run()
            {
                try {
                    sleep(2000);
                    String state = pref.getString("autoAuth", null);
                    if(state!=null){
                        Intent intent=new Intent(SplashScreenActivity.this ,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Intent intent=new Intent(SplashScreenActivity.this,RegistrationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    super.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }
}
