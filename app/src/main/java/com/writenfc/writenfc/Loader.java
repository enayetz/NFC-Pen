package com.writenfc.writenfc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Loader extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    boolean firstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        sharedPreferences = getApplicationContext().getSharedPreferences("my",MODE_PRIVATE);
        try{
            firstTime = sharedPreferences.getBoolean("first_time", true);
        }catch (Exception e){}
        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(2000);  //Delay of 10 seconds
                } catch (Exception e) {}
                finally {
                    if (firstTime) {
                        Intent i = new Intent(Loader.this, UserMenualActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        Intent i = new Intent(Loader.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }
        };
        welcomeThread.start();
    }
}
