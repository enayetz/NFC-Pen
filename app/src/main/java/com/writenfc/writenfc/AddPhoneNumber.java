package com.writenfc.writenfc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class AddPhoneNumber extends RelativeLayout{

    private int position;
    private int layout_id,top_view_id,right_of_id;
    private int editText_id,spinner_id;

    private Context context;
    private EditText editText;
    private Spinner spinner;

    private String[] items_phone = new String[]{"Mobile", "Work", "Home", "Pager","Fax"};
    private ArrayAdapter<String> adapter;
    private String phone_type="",editText_text="";

    private ImageView imageView;
    private boolean imageView_check;
    private String setType="",setText="";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public AddPhoneNumber(Context context,int top_view_id,int right_of_id,int position) {
        super(context);
        this.context = context;
        this.top_view_id = top_view_id;
        this.right_of_id = right_of_id;
        this.position = position;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public AddPhoneNumber(Context context,String setText,String setType,boolean check) {
        super(context);
        this.context = context;
        this.setText = setText;
        this.setType = setType;
        this.imageView_check = check;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void init() {
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (top_view_id!=0)
            params.addRule(RelativeLayout.BELOW, top_view_id);
        setLayoutParams(params);
        layout_id = generateViewId();

        RelativeLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,dpToPx(30),0);
        editText = new EditText(context);
        editText.setLayoutParams(layoutParams);
        editText.setId(EditText.generateViewId());
        editText.setTextSize(16);
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        editText.setHint("Phone Number");
        editText.getBackground().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
        if (!setText.isEmpty()) editText.setText(setText);

        RelativeLayout.LayoutParams layoutParams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.BELOW, editText.getId());
        spinner = new Spinner(context);
        spinner.setLayoutParams(layoutParams2);
        spinner.setId(Spinner.generateViewId());

        adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item, items_phone);
        spinner.setAdapter(adapter);
        spinner.setAdapter(adapter);
        if (!setType.isEmpty())
            spinner.setSelection(findPosition());

        RelativeLayout.LayoutParams layoutParams3 = new LayoutParams(dpToPx(20), dpToPx(20));
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams3.setMargins(0,dpToPx(10),0,0);
        imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams3);
        imageView.setId(ImageView.generateViewId());
        if(!imageView_check)
            imageView.setImageResource(R.drawable.baseline_add_box_black_18dp);
        else
            imageView.setImageResource(R.drawable.baseline_clear_black_18dp);

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imageView_check){
                    imageView_check=true;
                    imageView.setImageResource(R.drawable.baseline_clear_black_18dp);
                    WriteTAGFragment.addPhoneNumber(position);
                    MainActivity.viewPager.postInvalidate();

                }else{
                    WriteTAGFragment.RemovePhoneNumber(position);
                }
            }
        });

        addView(editText);
        addView(spinner);
        addView(imageView);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null) {
                    phone_type = item.toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Object item = parent.getItemAtPosition(position);
                if (item != null) {
                    phone_type = item.toString();
                }
            }
        });

    }

    public int dpToPx(int dp) {
        Resources r = context.getResources();
        int i=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return i;
    }

    private int findPosition() {
        for (int i=0;i<items_phone.length;i++) if (items_phone[i].toLowerCase().equals(setType.toLowerCase())) return i;
        return 0;
    }

    public int getEditText_id() {
        editText_id = editText.getId();
        return editText_id;
    }

    public int getSpinner_id() {
        spinner_id = spinner.getId();
        return spinner_id;
    }

    public String getPhone_type() {
        return phone_type;
    }

    public String getEditText_text() {
        editText_text = editText.getText().toString();
        return editText_text;
    }

    public int getLayout_id() {
        return getId();
    }

    public int getPosition() {
        return position;
    }

    public boolean isImageView_check() {
        return imageView_check;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
