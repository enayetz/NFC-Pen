package com.writenfc.writenfc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class UserMenualActivity extends AppCompatActivity {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    int current_position;
    Context context;
    int padding;
    private boolean firstTime=false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menual);

        sharedPreferences = getApplicationContext().getSharedPreferences("my",MODE_PRIVATE);
        try{
            firstTime = sharedPreferences.getBoolean("first_time", true);
        }catch (Exception e){}
        if (firstTime){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_time", false);
            editor.commit();
        }

        context = this.getWindow().getContext();
        final Button button1,button2;
        final ImageView imageView1,imageView2,imageView3,imageView4;

        viewPager = (ViewPager) findViewById(R.id.user_menual_viewPager);

        padding = getResources().getDimensionPixelSize(R.dimen.img_padding);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new Welcome_To_UserMenual_Fragment());
        viewPagerAdapter.addFragment(new How_To_Read_UserMenualFragment());
        viewPagerAdapter.addFragment(new How_To_Write_UserMenualFragment());
        viewPagerAdapter.addFragment(new Other_Featur_UserMenualFragment());
        viewPager.setAdapter(viewPagerAdapter);

        button1 = (Button) findViewById(R.id.button_one);
        button2 = (Button) findViewById(R.id.button_two);

        imageView1 = (ImageView) findViewById(R.id.img1);
        imageView2 = (ImageView) findViewById(R.id.img2);
        imageView3 = (ImageView) findViewById(R.id.img3);
        imageView4 = (ImageView) findViewById(R.id.img4);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                current_position = position;
                switch (position){
                    case 0:
                        button1.setText("Skip");
                        button2.setText("Next");
                        imageView1.setImageResource(R.drawable.circle_big);
                        imageView2.setImageResource(R.drawable.circle_small);
                        imageView3.setImageResource(R.drawable.circle_small);
                        imageView4.setImageResource(R.drawable.circle_small);
                        break;
                    case 1:
                        button1.setText("Back");
                        button2.setText("Next");
                        imageView1.setImageResource(R.drawable.circle_small);
                        imageView2.setImageResource(R.drawable.circle_big);
                        imageView3.setImageResource(R.drawable.circle_small);
                        imageView4.setImageResource(R.drawable.circle_small);
                        break;
                    case 2:
                        button1.setText("Back");
                        button2.setText("Next");
                        imageView1.setImageResource(R.drawable.circle_small);
                        imageView2.setImageResource(R.drawable.circle_small);
                        imageView3.setImageResource(R.drawable.circle_big);
                        imageView4.setImageResource(R.drawable.circle_small);
                        break;
                    case 3:
                        button1.setText("Back");
                        button2.setText("Finish");
                        imageView1.setImageResource(R.drawable.circle_small);
                        imageView2.setImageResource(R.drawable.circle_small);
                        imageView3.setImageResource(R.drawable.circle_small);
                        imageView4.setImageResource(R.drawable.circle_big);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}

        });

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button1.setBackgroundResource(R.drawable.button_click_bg);
                    button1.setTextColor(Color.WHITE);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    button1.setBackgroundResource(R.drawable.button_bg);
                    button1.setTextColor(Color.BLACK);
                    if (button1.getText().toString().equals("Skip")){
                        if (firstTime){
                            Intent intent = new Intent(UserMenualActivity.this,MainActivity.class);
                            startActivity(intent);
                        }finish();
                    }else{
                        viewPager.setCurrentItem(current_position-1);
                        viewPager.invalidate();
                    }
                }
                return true;
            }
        });
        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button2.setBackgroundResource(R.drawable.button_click_bg);
                    button2.setTextColor(Color.WHITE);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    button2.setBackgroundResource(R.drawable.button_bg);
                    button2.setTextColor(Color.BLACK);
                    if (button2.getText().toString().equals("Finish")){
                        if (firstTime){
                            Intent intent = new Intent(UserMenualActivity.this,MainActivity.class);
                            startActivity(intent);
                        }finish();
                    }else{
                        viewPager.setCurrentItem(current_position+1);
                        viewPager.invalidate();
                    }
                }
                return true;
            }
        });
    }

}
