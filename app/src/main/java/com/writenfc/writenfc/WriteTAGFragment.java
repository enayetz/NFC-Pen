package com.writenfc.writenfc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;
import static android.app.Activity.RESULT_OK;

public class WriteTAGFragment extends Fragment {
    private static final int PICK_CONTACT = 1005;
    private static final int PERMISSION_REQUEST_CONTACT = 101;

    static Context context;
    NfcHelper nfcHelper;

    static EditText txtFirstName,name_prefix,given_name,middle_name,
            family_name,nick_name, edttxtCompany, edttxtAddress,street,po_box,city,state,zip_code;

    Button write_tag,openContact;
    Boolean isWritingTag = false;
    ProgressDialog writingProgressDialog;
    private Intent intent;
    ScrollView scrollView;
    static ImageView name_dropdown_img,addreass_dropdown_img;

    static boolean is_name_dropdown_img=false,is_addreass_dropdown_img=false;

    static LinearLayout name_more_fields_LL,address_more_LL,phone_Inner_Linear_layout,Email_Inner_Linear_layout;

    AddPhoneNumber addPhoneNumber;

    static ArrayList<AddPhoneNumber> addPhoneNumbers = new ArrayList<>(); ;
    static ArrayList<AddEmailAddress> addEmailAddresses = new ArrayList<>(); ;

    public WriteTAGFragment() {}

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_write_tag, container, false);
        context = view.getContext();
        nfcHelper = new NfcHelper(getActivity());

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        //scrollView.setSmoothScrollingEnabled(true);

        txtFirstName = (EditText) view.findViewById(R.id.idETFirstName);
        //txtFirstName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        name_prefix = (EditText) view.findViewById(R.id.name_prefix);
       // name_prefix.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        given_name = (EditText) view.findViewById(R.id.given_name);
       // given_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        middle_name = (EditText) view.findViewById(R.id.middle_name);
        //middle_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        family_name = (EditText) view.findViewById(R.id.family_name);
        //family_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        nick_name = (EditText) view.findViewById(R.id.nick_name);
        //nick_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        edttxtCompany = (EditText) view.findViewById(R.id.idETCompany);
        //edttxtCompany.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        edttxtAddress = (EditText) view.findViewById(R.id.idETAddress);
      //  edttxtAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        street = (EditText) view.findViewById(R.id.street);
       // street.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        po_box = (EditText) view.findViewById(R.id.po_box);
       // po_box.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        city = (EditText) view.findViewById(R.id.city);
       // city.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        state = (EditText) view.findViewById(R.id.state);
       // state.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        zip_code = (EditText) view.findViewById(R.id.zip_code);
       // zip_code.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        name_more_fields_LL = (LinearLayout) view.findViewById(R.id.name_more_fileds_linear_layout);
        address_more_LL = (LinearLayout) view.findViewById(R.id.extara_address_linear_layout);

        name_dropdown_img= (ImageView) view.findViewById(R.id.name_dropdown);
        addreass_dropdown_img = (ImageView) view.findViewById(R.id.address_dropdown);
        name_dropdown_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    name_dropdown_img.setBackgroundColor(Color.GRAY);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    name_dropdown_img.setBackgroundColor(Color.TRANSPARENT);
                    if (!is_name_dropdown_img) {
                        name_dropdown_img.setImageResource(R.drawable.baseline_keyboard_arrow_up_black_18dp);
                        String s  = txtFirstName.getText().toString();
                        txtFirstName.setVisibility(View.GONE);
                        name_more_fields_LL.setVisibility(View.VISIBLE);
                        given_name.setText(s);
                        is_name_dropdown_img=true;
                    }else{
                        name_dropdown_img.setImageResource(R.drawable.baseline_keyboard_arrow_down_black_18dp);
                        String s  = given_name.getText().toString();
                        txtFirstName.setVisibility(View.VISIBLE);
                        txtFirstName.setText(s);
                        name_more_fields_LL.setVisibility(View.GONE);
                        is_name_dropdown_img=false;
                    }
                }
                return true;
            }
        });
        addreass_dropdown_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addreass_dropdown_img.setBackgroundColor(Color.GRAY);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    addreass_dropdown_img.setBackgroundColor(Color.TRANSPARENT);
                    if (!is_addreass_dropdown_img) {
                        addreass_dropdown_img.setImageResource(R.drawable.baseline_keyboard_arrow_up_black_18dp);
                        edttxtAddress.setVisibility(View.GONE);
                        address_more_LL.setVisibility(View.VISIBLE);
                        is_addreass_dropdown_img=true;
                    }else{
                        addreass_dropdown_img.setImageResource(R.drawable.baseline_keyboard_arrow_down_black_18dp);
                        edttxtAddress.setVisibility(View.VISIBLE);
                        address_more_LL.setVisibility(View.GONE);
                        is_addreass_dropdown_img=false;
                    }
                }
                return true;
            }
        });
        phone_Inner_Linear_layout = (LinearLayout) view.findViewById(R.id.phone_Inner_Linear_layout);
        Email_Inner_Linear_layout = (LinearLayout) view.findViewById(R.id.Email_Inner_Linear_layout);

        //TODO:::
        addPhoneNumber = new AddPhoneNumber(context,0, R.id.imageview2, 0);
        phone_Inner_Linear_layout.addView(addPhoneNumber);
        addPhoneNumbers.add(addPhoneNumber);

        AddEmailAddress addEmailAddress = new AddEmailAddress(context,0,0,0);
        Email_Inner_Linear_layout.addView(addEmailAddress);
        addEmailAddresses.add(addEmailAddress);

        write_tag = (Button) view.findViewById(R.id.idBTNnfc);
        openContact = (Button) view.findViewById(R.id.idBTNOpencontact);
        write_tag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    write_tag.setBackgroundResource(R.drawable.button_click_bg);
                    write_tag.setTextColor(Color.WHITE);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    write_tag.setBackgroundResource(R.drawable.button_bg);
                    write_tag.setTextColor(Color.BLACK);
                    onBtnWriteTagClicked(view);
                }
                return true;
            }
        });
        openContact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    openContact.setBackgroundResource(R.drawable.button_click_bg);
                    openContact.setTextColor(Color.WHITE);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    openContact.setBackgroundResource(R.drawable.button_bg);
                    openContact.setTextColor(Color.BLACK);
                    askForContactPermission();
                }
                return true;
            }
        });
        return view;
    }

    public void setIntent(Intent intent){
        this.intent = intent;
        VCard vcard = new VCard();
        StructuredName strdName = new StructuredName();
        if (is_name_dropdown_img) {
            strdName.setFamily(family_name.getText().toString());
            strdName.setGiven(given_name.getText().toString());
            String fn = name_prefix.getText().toString()+";"+middle_name.getText().toString();
            vcard.setFormattedName(new FormattedName(fn));
            vcard.setNickname(nick_name.getText().toString());
            Log.i("vCard","inside is_name_dropdown_img" );
            Log.i("vCard","FN:"+fn);

        }else{
            strdName.setGiven(txtFirstName.getText().toString());
            Log.i("vCard", txtFirstName.getText().toString());
        }
        vcard.setStructuredName(strdName);

        for (AddPhoneNumber addPhoneNumber:addPhoneNumbers){
            TelephoneType telephoneType = TelephoneType.HOME;
            if (addPhoneNumber.getPhone_type()=="Mobile")telephoneType = TelephoneType.CELL;
            if (addPhoneNumber.getPhone_type()=="Work")telephoneType = TelephoneType.WORK;
            if (addPhoneNumber.getPhone_type()=="Home")telephoneType = TelephoneType.HOME;
            if (addPhoneNumber.getPhone_type()=="Fax")telephoneType = TelephoneType.FAX;
            vcard.addTelephoneNumber(addPhoneNumber.getEditText_text(),telephoneType);
        }
        for (AddEmailAddress addEmailAddress:addEmailAddresses){
            EmailType emailType = EmailType.HOME;
            if (addEmailAddress.getPhone_type()=="Work")emailType = EmailType.WORK;
            vcard.addEmail(addEmailAddress.getEditText_text().toString(),emailType);
        }

        Organization org = new Organization();
        org.getValues().add(edttxtCompany.getText().toString());
        vcard.addOrganization(org);

        //set address
        Address adr = new Address();
        if (is_addreass_dropdown_img) {
            adr.setStreetAddress(street.getText().toString());
            adr.setPoBox(po_box.getText().toString());
            adr.setPostalCode(zip_code.getText().toString());
            adr.setRegion(state.getText().toString());
            adr.setLocality(city.getText().toString());
            //adr.setCountry();
        }else{
            adr.setExtendedAddress(edttxtAddress.getText().toString());
        }

        vcard.addAddress(adr);

        String finalVcardInfo = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();
        finalVcardInfo = finalVcardInfo.replace("\\","");
        //Log.i("vCard","final-V-Card"+);

                NdefMessage ndefMessage = null;
                try {
                    ndefMessage = nfcHelper.createVcardNdefMessage(finalVcardInfo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
        Log.i("vCard","final-V-Card"+(ndefMessage==null));
                if (nfcHelper.writeNdefMessage(intent, ndefMessage)) {
                    Toast.makeText(context, "write successful",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "write fail", Toast.
                            LENGTH_LONG).show();
                }
                isWritingTag = false;
                writingProgressDialog.dismiss();
    }

    public void onBtnWriteTagClicked(View view) {
        if (validateField()) {
            showWaitDialog();
        }else{
            new AlertDialog.Builder(context)
                    .setTitle("Empty Field")
                    .setMessage("Please input the required field")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    private boolean checkEmpty(EditText et){
        if (et.getText().toString().equals("")) return true;
        return false;
    }
    private boolean validateField(){
        /*if(txtFirstName.getText().equals("")
                &&name_prefix.getText().equals("")
                &&given_name.getText().equals("")&&
                middle_name.getText().equals("")&&
                family_name.getText().equals("")&&
                nick_name.getText().equals("")&&
                edttxtCompany.getText().equals("")&&
                edttxtAddress.getText().equals("")&&
                street.getText().equals("")&&
                po_box.getText().equals("") &&
                city.getText().equals("")&&
                state.getText().equals("")&&
                zip_code.getText().equals("")){*/
        boolean b = true;
        if(checkEmpty(txtFirstName) &&
                checkEmpty(name_prefix) && checkEmpty(given_name) && checkEmpty(middle_name)
        && checkEmpty(family_name) && checkEmpty(nick_name)) {
            Log.i("validation","every Field Empty");
            b=false;
        }else{
            for (AddPhoneNumber addPhoneNumber:addPhoneNumbers){
                if (addPhoneNumber.getEditText_text().equals("")) b = false;
            }
        }
        Log.i("validation","some Field has value");

        return b;
    }

    private void showWaitDialog() {
        writingProgressDialog = ProgressDialog.show(context, "",
                getString(R.string.dialog_tap_on_tag), false, true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface arg0) {
                                isWritingTag = false;
                            }
                        });
        isWritingTag = true;
    }


    //Checking permission for Read Contact
    public void askForContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.WRITE_CONTACTS)) {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                    builder.setTitle("Contacts access needed!");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Allow to Read Contact");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions( getActivity(),
                                    new String[]{Manifest.permission.WRITE_CONTACTS},
                                    PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else{
                try {
                    Intent it= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(it, PICK_CONTACT);
                } catch (Exception e) {
                    Toast.makeText(context, "Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            try {
                Intent it= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(it, PICK_CONTACT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_CONTACT:
                    pickedContact(data);
                    break;
            }
        } else {
            Log.e("Failed", "Not able to pick contact");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void pickedContact(Intent data) {
        Uri result = data.getData();
// get the contact id from the Uri
        String id = result.getLastPathSegment();

        ArrayList<String> numbers = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();

// query for phone numbers for the selected contact id
        Cursor number_query = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                new String[]{id}, null);
        int phoneIdx = number_query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int phoneType = number_query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
        if(number_query.moveToFirst()) {
            while(!number_query.isAfterLast()) { // for each phone number, add it to the numbers array
                String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(),
                        number_query.getInt(phoneType), "");
                String number =number_query.getString(phoneIdx)+"\n"+type;
                numbers.add(number);
                number_query.moveToNext();
            }
        } else Log.w("TAG ", "No results");
        number_query.close();
//query for emails for the selected contact
        Cursor email_query = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[]{id}, null);
        int emailIndx = email_query.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
        int emailTyp = email_query.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
        if(email_query.moveToFirst()) {
            while(!email_query.isAfterLast()) { // for each phone number, add it to the numbers array
                String type = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(this.getResources(),
                        email_query.getInt(emailTyp), "");
                String email =email_query.getString(emailIndx)+"\n"+type;
                emails.add(email);
                email_query.moveToNext();
            }
        } else Log.w("TAG ", "No results");
        email_query.close();

//query for names for the selected contact
        String namePrefix="",givenName="",middleName="",familyName="",nickName="";
        String[] nameProjection = new String[] {
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.PREFIX
        };
        Cursor nameCursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                nameProjection,
                ContactsContract.Data.MIMETYPE + " = '" +
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "' AND " +
                        ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID
                        + " = ?", new String[] { id }, null);
        if(nameCursor.moveToNext()) {
            givenName = nameCursor.getString(nameCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            middleName = nameCursor.getString(nameCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
            familyName = nameCursor.getString(nameCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            namePrefix = nameCursor.getString(nameCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.PREFIX));

        }nameCursor.close();

//getting nickname from contact
        Cursor nickNameCursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Nickname.NAME},
                ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "= ?",
                new String[]{id, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE},
                null);
        while (nickNameCursor.moveToNext()) {
            nickName = nickNameCursor.getString(nickNameCursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
        }
        nickNameCursor.close();

 //get address
        String street,city,state,po_box,zip,address="";
        try {
            Cursor cursor = context.getContentResolver().query(data.getData(), null, null, null, null);
            cursor.moveToFirst();
            long id2 = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            cursor.close();
            cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE},
                    ContactsContract.Data.CONTACT_ID + "=? AND " +
                            ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE + "=?",
                    new String[]{String.valueOf(id2), ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE},
                    null);
            if(cursor.moveToNext()) {
               // street,city,state,po_box,zip;
                String Street = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                String Postcode = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                String City = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                address = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            }
        } catch (Exception e) {  e.printStackTrace();}
// getaing company
        String company="";
        try {
            Cursor cursor = context.getContentResolver().query(data.getData(), null, null, null, null);
            cursor.moveToFirst();
            long id3 = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            cursor.close();
            cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Organization.COMPANY,},
                    ContactsContract.Data.CONTACT_ID + "=? AND " +
                            ContactsContract.CommonDataKinds.Organization.MIMETYPE + "=?",
                    new String[]{String.valueOf(id3), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE},
                    null);
            if(cursor.moveToNext()) {
                company = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
            }
        } catch (Exception e) {  e.printStackTrace();}

        //namePrefix="",givenName="",middleName="",familyName="",nickName="";
        if(namePrefix==null)namePrefix ="";
        if(givenName==null)givenName ="";
        if(familyName==null)familyName ="";
        if(middleName==null)middleName ="";
        if(nickName==null)nickName ="";
        if(address==null)address ="";
        if(company==null)company ="";

        Bundle bundle = new Bundle();
        bundle.putString("name_prefex",namePrefix);
        bundle.putString("given_name",givenName);
        bundle.putString("family_name",familyName);
        bundle.putString("middle_name",middleName);
        bundle.putString("nick_name",nickName);
        bundle.putString("address",address);
        bundle.putString("company",company);

        //set contact info into fields
        setViewfromRead(bundle,emails,numbers);
    }

    public boolean getIsWritingTag(){return isWritingTag;}

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setViewfromRead(Bundle bundle, ArrayList<String> emailList, ArrayList<String> phoneList){
        name_dropdown_img.setImageResource(R.drawable.baseline_keyboard_arrow_up_black_18dp);
        txtFirstName.setVisibility(View.GONE);
        name_more_fields_LL.setVisibility(View.VISIBLE);
        is_name_dropdown_img=true;

        family_name.setText("");
        given_name.setText("");
        name_prefix.setText("");
        middle_name.setText("");
        nick_name.setText("");
        edttxtCompany.setText("");
        edttxtAddress.setText("");

        //company="",="";
        if(!bundle.getString("family_name").isEmpty()) family_name.setText(bundle.getString("family_name"));
        if(!bundle.getString("given_name").isEmpty()) given_name.setText(bundle.getString("given_name"));
        if(!bundle.getString("name_prefex").isEmpty()) name_prefix.setText(bundle.getString("name_prefex"));
        if(!bundle.getString("middle_name").isEmpty()) middle_name.setText(bundle.getString("middle_name"));
        if(!bundle.getString("nick_name").isEmpty()) nick_name.setText(bundle.getString("nick_name"));
        if(!bundle.getString("company").isEmpty()) edttxtCompany.setText(bundle.getString("company"));
        if(!bundle.getString("address").isEmpty()) edttxtAddress.setText(bundle.getString("address"));
        bundle.clear();

        //adding emails

        Email_Inner_Linear_layout.removeAllViews();
        addEmailAddresses.clear();
        if(emailList.size()==0){
            AddEmailAddress addEmailAddress = new AddEmailAddress(context,0,0,0);
            Email_Inner_Linear_layout.addView(addEmailAddress);
            addEmailAddresses.add(addEmailAddress);
        }
        for (int i=0;i<emailList.size();i++){
            String adds="",type="";
            String[] s = emailList.get(i).split("\n");
            for(int j=0;j<s.length;j++){
                if (j==0) adds=s[j];
                else type = s[j];
            }
            AddEmailAddress addEmailAddress = new AddEmailAddress(context,adds,type,true);
            Email_Inner_Linear_layout.addView(addEmailAddress);
            addEmailAddresses.add(addEmailAddress);
        }

        // adding phones
        phone_Inner_Linear_layout.removeAllViews();
        addPhoneNumbers.clear();
        if (phoneList.size()==0){
            AddPhoneNumber addPhoneNumber = new AddPhoneNumber(context,0,0,0);
            phone_Inner_Linear_layout.addView(addPhoneNumber);
            addPhoneNumbers.add(addPhoneNumber);
        }
        String pastAdds="";
        for (int i=0;i<phoneList.size();i++){
            String adds="",type="";
            String[] s = phoneList.get(i).split("\n");
            for(int j=0;j<s.length;j++){
                if (j==0) adds=s[j];
                else type = s[j];
            }
            adds = adds.replace("-","");
            adds = adds.replace(" ","");
            Log.i("phoneNO","new"+adds);
            Log.i("phoneNO","past"+pastAdds);
            if (pastAdds.equals(adds)) {
                continue;
            }

            pastAdds = adds;
            AddPhoneNumber addPhoneNumber = new AddPhoneNumber(context,adds,type,true);
            phone_Inner_Linear_layout.addView(addPhoneNumber);
            addPhoneNumbers.add(addPhoneNumber);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void addPhoneNumber(int position){
        AddPhoneNumber addPhoneNumber = new AddPhoneNumber(context,addPhoneNumbers.get(position).getLayout_id(),
                R.id.imageview2,addPhoneNumbers.size());
        addPhoneNumbers.add(addPhoneNumber);
        phone_Inner_Linear_layout.addView(addPhoneNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void RemovePhoneNumber(int position){
        addPhoneNumbers.remove(position);
        int i =0;
        phone_Inner_Linear_layout.removeAllViews();
        for (AddPhoneNumber phoneNumber:addPhoneNumbers){
            phone_Inner_Linear_layout.addView(phoneNumber);
            phoneNumber.setPosition(i);
            i++;
        }
        if (addPhoneNumbers.isEmpty() || addPhoneNumbers ==null){
            AddPhoneNumber addPhoneNumber = new AddPhoneNumber(context,0,0,0);
            phone_Inner_Linear_layout.addView(addPhoneNumber);
            addPhoneNumbers.add(addPhoneNumber);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void addEmailAddress(int position){
        AddEmailAddress addEmailAddress = new AddEmailAddress(context,0,0,addEmailAddresses.size());
        addEmailAddresses.add(addEmailAddress);
        Email_Inner_Linear_layout.addView(addEmailAddress);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void removeEmailAddress(int position){
        addEmailAddresses.remove(position);
        int i =0;
        Email_Inner_Linear_layout.removeAllViews();
        for (AddEmailAddress addEmailAddress:addEmailAddresses){
            Email_Inner_Linear_layout.addView(addEmailAddress);
            addEmailAddress.setPosition(i);
            i++;
        }
        if (addEmailAddresses.isEmpty() || addEmailAddresses ==null){
            AddEmailAddress addEmailAddress = new AddEmailAddress(context,0,0,0);
            addEmailAddresses.add(addEmailAddress);
            Email_Inner_Linear_layout.addView(addEmailAddress);
        }
    }

    public void startContactPicker() {
        Intent it= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(it, PICK_CONTACT);
    }
}
