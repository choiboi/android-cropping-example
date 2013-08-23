package com.choiboi.imagecroppingexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    
    public static final String CROP_VERSION_SELECTED_KEY = "crop";
    
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onStartCropButton(View v) {
        Intent intent = new Intent(this, CropActivity.class);
        
        if (v.getId() == R.id.ma_crop_button1) {
            intent.putExtra(CROP_VERSION_SELECTED_KEY, VERSION_1);
        } else if (v.getId() == R.id.ma_crop_button2) {
            intent.putExtra(CROP_VERSION_SELECTED_KEY, VERSION_2);
        }
        startActivity(intent);
    }
}
