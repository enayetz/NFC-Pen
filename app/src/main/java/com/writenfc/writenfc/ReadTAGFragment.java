package com.writenfc.writenfc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;

import static android.content.Context.MODE_PRIVATE;

public class ReadTAGFragment extends Fragment {

    private static final String FORMATED_NAME = "FN:";
    private static final String NICK_NAME = "NICKNAME:";
    private static final String STRUCTURED_NAME = "N:";
    private static final String PHONE_NUM = "TEL;";
    private static final String COMPANY_NAME = "ORG:";
    private static final String ADDRESS = "ADR:";
    private static final String EMAIL = "EMAIL;";
    private static final int PERMISSION_REQUEST_CONTACT = 100;

    private String company="",address="",family_name ="",given_name="",name_prefex="",middle_name="",nick_name="";

    private ArrayList<String> emailList = new ArrayList<>();
    private ArrayList<String> phoneList = new ArrayList<>();

    private Context context;
    private NfcHelper nfcHelper;
    private TextView first_last_tv,phone_tv,company_tv,email_tv,address_tv,retryText;
    private Intent intent;

    private LinearLayout retryLayout,noNFCAlertLayout;
    private RelativeLayout tagContainetLayout;
    private ImageView retry_img,sorryImg;
    private Button retryButton,save_button,edit_button;

    private ProgressDialog readingTAGProgressing;

    private ArrayList < ContentProviderOperation > ops = new ArrayList <> ();

    private Intent intentSaveContact = new Intent(ContactsContract.Intents.Insert.ACTION);

    private Dialog nfcReadDialog;

    public ReadTAGFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_read_tag, container, false);

        intentSaveContact.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        context = view.getContext();
        nfcHelper = new NfcHelper(getActivity());

        retryLayout = (LinearLayout) view.findViewById(R.id.retry_Read_TAG_Layout_ID);
        tagContainetLayout = (RelativeLayout) view.findViewById(R.id.tag_Content_Layout_ID);
        noNFCAlertLayout = (LinearLayout) view.findViewById(R.id.No_FNC_Alert_Layout_ID);
        retry_img = (ImageView) view.findViewById(R.id.retry_img);
        sorryImg = (ImageView) view.findViewById(R.id.sorryImg);
        retryText = (TextView) view.findViewById(R.id.retryText);
        retryButton = (Button) view.findViewById(R.id.retryButton);

        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryReadTAG(v);
            }
        });

        if (!nfcHelper.isNfcEnabledDevice()) {
            noNFCAlertLayout.setVisibility(View.VISIBLE);
            tagContainetLayout.setVisibility(View.GONE);
            retryLayout.setVisibility(View.GONE);
        }else{
            if (!nfcHelper.isNfcEnabled()) {
                noNFCAlertLayout.setVisibility(View.GONE);
                tagContainetLayout.setVisibility(View.GONE);
                retryLayout.setVisibility(View.VISIBLE);
            }
        }

        first_last_tv = (TextView) view.findViewById(R.id.last_name_TV);
        phone_tv = (TextView) view.findViewById(R.id.phone_number_TV);
        company_tv = (TextView) view.findViewById(R.id.company_TV);
        email_tv = (TextView) view.findViewById(R.id.email_number_TV);
        address_tv = (TextView) view.findViewById(R.id.address_TV);

        save_button = (Button) view.findViewById(R.id.button_save_contact);
        edit_button = (Button) view.findViewById(R.id.button_edit_contact);

        save_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    save_button.setBackgroundResource(R.drawable.button_click_bg);
                }
                if (event.getAction()==MotionEvent.ACTION_UP){
                    save_button.setBackgroundResource(R.drawable.button_bg);
                    v.performClick();
                }
                return true;
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields())
                    startActivity(intentSaveContact);
                else{
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Empty Contact")
                            .setMessage("The View Must Contain Name and Mobile number")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
        edit_button.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    edit_button.setBackgroundResource(R.drawable.button_click_bg);
                }
                if (event.getAction()==MotionEvent.ACTION_UP){
                    edit_button.setBackgroundResource(R.drawable.button_bg);
                    Bundle bundle = new Bundle();
                    bundle.putString("name_prefex",name_prefex);
                    bundle.putString("given_name",given_name);
                    bundle.putString("family_name",family_name);
                    bundle.putString("middle_name",middle_name);
                    bundle.putString("nick_name",nick_name);
                    bundle.putString("address",address);
                    bundle.putString("company",company);

                    MainActivity.viewPager.setCurrentItem(0);
                    MainActivity.viewPager.invalidate();
                    if (bundle!=null);
                        WriteTAGFragment.setViewfromRead(bundle,emailList,phoneList);
                }
                return true;
            }
        });

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        nfcReadDialog = new Dialog(getActivity());
        nfcReadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nfcReadDialog.setCancelable(true);
        nfcReadDialog.setContentView(R.layout.read_tag_nearby_layout);


        return view;
    }

    @Override
    public void setMenuVisibility(final boolean visible){
        if (visible && !nfcReadDialog.isShowing() && noNFCAlertLayout.getVisibility() != View.VISIBLE){
            Log.i("readTag","now visibale to user");

            if(tagContainetLayout.getVisibility() !=  View.VISIBLE && retryLayout.getVisibility() != View.VISIBLE) {
                retryLayout.setVisibility(View.GONE);
                nfcReadDialog.show();
                nfcReadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //dialog.dismiss();
                        retryLayout.setVisibility(View.VISIBLE);

                    }
                });
            }
        }
    }

    private boolean validateFields() {

        if(checkEmpty(first_last_tv)) {
            Log.i("validation","every Field Empty");
            if(checkEmpty(phone_tv))
                return false;
        }

        return true;
    }

    private boolean checkEmpty(TextView et){
        if (et.getText().toString().equals("")) return true;
        return false;
    }

    private void retryReadTAG(View v) {
        if (!nfcHelper.isNfcEnabledDevice()) {
            //Dialog for Alert Device has no NFC feature
            showNoNFCDeviceAlertDialog();
        }else{
            if (!nfcHelper.isNfcEnabled()) {
                //Dialog for NFC Enable settings
                showEnableNFCAlertDialog();
            }else{
                setIntent(this.intent);
            }
        }
    }

    private void showNoNFCDeviceAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
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

    public void setIntent(Intent intent){
        this.intent = intent;
        if (nfcReadDialog.isShowing())
            nfcReadDialog.dismiss();
        retryButton.setVisibility(View.GONE);
        first_last_tv.setText("");
        phone_tv.setText("");
        company_tv.setText("");
        email_tv.setText("");
        address_tv.setText("");

        //setContactOnView("");
        readingTAGProgressing = ProgressDialog.show(context, "",
                getString(R.string.reading_Tag), false, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        //TODO: do something...
                    }
                });

            NdefMessage ndefMessage = nfcHelper.getNdefMessageFromIntent(intent);
            if (ndefMessage != null) {
                NdefRecord ndefRecord = nfcHelper.getFirstNdefRecord(ndefMessage);
                if (ndefRecord != null) {
                    boolean isVcardRecord = nfcHelper.isNdefRecordOfTnfAndRdt(ndefRecord,
                            NdefRecord.TNF_MIME_MEDIA, "text/vcard".getBytes());
                    if (isVcardRecord) {

                        String penTagContent = nfcHelper.getTextFromNdefRecord(ndefRecord);

                        setContactOnView(penTagContent);
                        readingTAGProgressing.dismiss();
                    } else {
                        readingTAGProgressing.dismiss();
                        Toast.makeText(context, "Record is not Vcard formatted.",
                                Toast.LENGTH_LONG).show();
                        noNFCAlertLayout.setVisibility(View.GONE);
                        tagContainetLayout.setVisibility(View.GONE);
                        retryLayout.setVisibility(View.VISIBLE);
                        retryButton.setVisibility(View.VISIBLE);
                        retryText.setText("Please bring your NFC pen nearby and retry");
                    }
                }
                else {
                    readingTAGProgressing.dismiss();
                    Toast.makeText(context, "No Ndef record found.", Toast.LENGTH_SHORT)
                            .show();
                    noNFCAlertLayout.setVisibility(View.GONE);
                    tagContainetLayout.setVisibility(View.GONE);
                    retryLayout.setVisibility(View.VISIBLE);
                    retryButton.setVisibility(View.VISIBLE);
                    retryText.setText("Please bring your NFC pen nearby and retry");
                }
            } else {
                readingTAGProgressing.dismiss();
                Toast.makeText(context, "No Ndef message found.", Toast.LENGTH_SHORT).show();
                noNFCAlertLayout.setVisibility(View.GONE);
                tagContainetLayout.setVisibility(View.GONE);
                retryLayout.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                retryText.setText("Please bring your NFC pen nearby and retry");
            }
    }

    private void setContactOnView(String penTagContent) {
        noNFCAlertLayout.setVisibility(View.GONE);
        tagContainetLayout.setVisibility(View.VISIBLE);
        retryLayout.setVisibility(View.GONE);
        name_prefex = given_name = family_name = middle_name = nick_name = "";
        /*penTagContent = "VERSION:3.0\n" +
                "    PRODID:ez-vcard 0.10.4\n" +
                "    FN:;mama.\n" +
                "    NICKNAME:\n" +
                "    N:bogra;amin\n" +
                "    TEL;TYPE=work:123\n" +
                "    TEL;TYPE=home:12345\n" +
                "    TEL;TYPE=cell:1234567\n" +
                "    EMAIL;TYPE=work:abc\n" +
                "    EMAIL;TYPE=home:abcde\n" +
                "    EMAIL;TYPE=other:abcdefg\n" +
                "    ORG:shkalal\n" +
                "    ADR:hajjs;;shja;ahjaks;shsjs;ajkaa\n" +
                "    END:VCARD";*/

        String[] splitData = penTagContent.split("\n");
        ArrayList<String> phoneList = new ArrayList<>();
        ArrayList<String> eamilList = new ArrayList<>();
        String addressList ="";
        String company= "";
        for(String s:splitData){
            if(s.contains(FORMATED_NAME)){
                String[] ss = s.replace(FORMATED_NAME, "").split(";");
                for(int i=0;i<ss.length;i++){
                    if(i==0) name_prefex = ss[i];
                    else middle_name += ss[i];
                }
            }else if(s.contains(STRUCTURED_NAME)){
                String[] ss = s.replace(STRUCTURED_NAME, "").split(";");
                for(int i=0;i<ss.length;i++){
                    if(i==0) family_name = ss[i];
                    else given_name += ss[i];
                }
            }else if(s.contains(NICK_NAME)){
                nick_name = s.replace(NICK_NAME, "");
            }else if(s.contains(PHONE_NUM)){
                phoneList.add(s.replace(PHONE_NUM, ""));
            }else if(s.contains(EMAIL)){
                eamilList.add(s.replace(EMAIL, ""));
            }else if(s.contains(COMPANY_NAME)){
                company = s.replace(COMPANY_NAME, "");
            }else if(s.contains(ADDRESS)){
                addressList = s.replace(ADDRESS, "");
                addressList = addressList.replaceAll(";"," ");
            }
        }

        this.address = addressList;
        this.company = company;

        String name ="";
        if(!name_prefex.isEmpty()){
            name+=name_prefex+" ";
        }
        if(!given_name.isEmpty()){
            name+=given_name+" ";
        }
        if(!middle_name.isEmpty()) {
            name+=middle_name+" ";
        }
        if(!family_name.isEmpty()) {
            name+=family_name+" ";
        }
        if(!nick_name.isEmpty()) {
          name+=nick_name+" ";
        }

        ArrayList<String> finalEamilList = new ArrayList<>();
        ArrayList<String> finalPhoneList = new ArrayList<>();

        ArrayList<ContentValues> datas = new ArrayList<ContentValues>();

        for(String e:eamilList){
            String[] s = e.split(":");
            String typ="",addrs="";
            ContentValues row = new ContentValues();
            for(int i=0;i<s.length;i++){
                if(i==0){
                    String[] type = s[i].split("=");
                    for(int j=0;j<type.length;j++) if (j>0) typ=type[j];
                }else {addrs = s[i];}
            }
            if(!addrs.isEmpty() && !typ.isEmpty()) {
                finalEamilList.add(addrs + "\n" + typ);
                intentSaveContact.putExtra(ContactsContract.Intents.Insert.EMAIL, addrs);
                intentSaveContact.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, typ);
                row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                row.put(ContactsContract.CommonDataKinds.Email.ADDRESS, addrs);
                row.put(ContactsContract.CommonDataKinds.Email.TYPE, typ);
                datas.add(row);
            }
        }
        emailList = finalEamilList;

        for(String e:phoneList){
            String[] s = e.split(":");
            String typ="",num="";
            ContentValues row = new ContentValues();
            for(int i=0;i<s.length;i++){
                if(i==0){
                    String[] type = s[i].split("=");
                    for(int j=0;j<type.length;j++) if (j>0) typ=type[j];
                }else {num = s[i];}
            }
            if(!num.isEmpty() && !typ.isEmpty()){
                finalPhoneList.add(num+"\n"+typ);
                row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, num);
                row.put(ContactsContract.CommonDataKinds.Phone.TYPE, typ);
                datas.add(row);
            }
        }
        //store in field
        this.phoneList = finalPhoneList;

        String all_name = name_prefex+" "+given_name+" "+family_name+" "+middle_name+" "+nick_name;
        all_name = all_name.trim();
        first_last_tv.setText(all_name);

        String numbers="";
        for(String s:finalPhoneList){
            numbers+=s+"\n\n";
        }
        phone_tv.setText(numbers.trim());

        String emails="";
        for(String s:finalEamilList){
            emails+=s+"\n\n";
        }

        intentSaveContact.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA,datas);
        intentSaveContact.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intentSaveContact.putExtra(ContactsContract.Intents.Insert.POSTAL,addressList );
        intentSaveContact.putExtra(ContactsContract.Intents.Insert.COMPANY, company);

        email_tv.setText(emails.trim());
        company_tv.setText(company);
        addressList=addressList.trim();
        address_tv.setText(addressList.trim());

    }

}
