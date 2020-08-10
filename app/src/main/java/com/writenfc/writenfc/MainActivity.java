package com.writenfc.writenfc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_CONTACT = 1005;
    private static final int PERMISSION_REQUEST_CONTACT = 101;
    public static ViewPager viewPager;
    TabLayout tabLayout;
    static ViewPagerAdapter viewPagerAdapter;

    Dialog dialog_about;

    NfcHelper nfcHelper;
    TextView txtFirstName,about_phone_number,about_fax_number,about_email_address,about_website;

    private ReadTAGFragment readTagFragment;
    private WriteTAGFragment writeTagFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcHelper = new NfcHelper(this);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);

        dialog_about = new Dialog(this);
        dialog_about.setContentView(R.layout.about_app_dialog_layout);

       if (!nfcHelper.isNfcEnabledDevice()) {
            //Dialog for Alert Device has no NFC feature
           showNoNFCDeviceAlertDialog();
        }else{
           if (!nfcHelper.isNfcEnabled()) {
               //Dialog for NFC Enable settings
               showEnableNFCAlertDialog();
           }
        }

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        readTagFragment = new ReadTAGFragment();
        writeTagFragment = new WriteTAGFragment();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(writeTagFragment,"Write To Pen");
        viewPagerAdapter.addFragment(readTagFragment,"Read From Pen");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position==0){
                    getSupportActionBar().setTitle("Write in an NFC tag. ");
                }
                if(position==1){
                    getSupportActionBar().setTitle("Read an NFC tag.");
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setTabTextColors(ColorStateList.valueOf(Color.WHITE));
        tabLayout.setSelectedTabIndicatorColor(Color.RED);
        tabLayout.setupWithViewPager(viewPager);
        handleIntent(getIntent());
    }

    private void showNoNFCDeviceAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("NFC Error!!");
        alertDialog.setMessage("Your Device Has No NFC Feature, Please Upgrade Your Device");
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                return;
            }
        });
        alertDialog.show();
    }

    private void showEnableNFCAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable NFC");
        alertDialog.setMessage("Would you like to enable NFC? Select NFC Settings");
        alertDialog.setPositiveButton("NFC Settings", new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new  Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                return;
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcHelper.isNfcEnabledDevice()) {
            nfcHelper.enableForegroundDispatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcHelper.isNfcEnabledDevice()) {
            nfcHelper.disableForegroundDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);

        if (nfcHelper.isNfcIntent(intent)) {
            if (writeTagFragment.getIsWritingTag()) {
                writeTagFragment.setIntent(intent);
           }else{
                viewPager.setCurrentItem(1);
                readTagFragment.setIntent(intent);
           }
        } else {
            handleIntent(intent);
        }
    }

    public void handleIntent(Intent intent) {

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    txtFirstName.setText(sharedText);
                }
/*            Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
            readVcard(uri);*/
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    public void exitApp(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Exiting Garland")
                .setMessage("Do You Want Exit ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {return;}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void aboutDesk(MenuItem item) {
        int width = getWindow().getDecorView().getWidth();
        int height = getWindow().getDecorView().getHeight();
        dialog_about.getWindow().setLayout((int) (width),(int)(height*0.9));
        dialog_about.show();
        Button button = (Button) dialog_about.findViewById(R.id.about_dialog_exit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_about.dismiss();
            }
        });
        about_phone_number = (TextView) dialog_about.findViewById(R.id.TextView1);
        about_fax_number = (TextView) dialog_about.findViewById(R.id.TextView2);
        about_email_address = (TextView) dialog_about.findViewById(R.id.TextView3);
        about_website = (TextView) dialog_about.findViewById(R.id.TextView5);

        about_phone_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "+1 401 828-9582";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });
        about_email_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: customerservice@garlandpen.com"));
                startActivity(Intent.createChooser(emailIntent, "Contact Us"));
            }
        });
        about_website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.garlandpen.com/")));
            }
        });
    }

    public void helpDesk(MenuItem item) {
        Intent intent = new Intent(this,UserMenualActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        writeTagFragment.startContactPicker();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission not Granted! try Again", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

}
