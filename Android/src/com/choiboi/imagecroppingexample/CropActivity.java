package com.choiboi.imagecroppingexample;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

public class CropActivity extends Activity {
    
    private final String TEMP_JPEG_FILENAME = "img_temp.jpg";
    private final String APP_NAME = "ImageCropExample";
    private final String STORAGE_MISSING_MSG = "Your Phone is Currently Not Connected to Any Storage Device!";
    public static final String SELECTED_MEDIA = "SELECTED_MEDIA";
    
    public static final int MEDIA_SELECT_DIALOG = 0;
    public static final int MEDIA_CAMERA = 1;
    public static final int MEDIA_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
    }
    
    public void onCropImageButton(View v) {
        
    }
    
    public void onChangeTemplateButton(View v) {
        
    }
    
    public void onChangeImageButton(View v) {
        Intent intent = new Intent(this, MediaSelectDialog.class);
        startActivityForResult(intent, MEDIA_SELECT_DIALOG);
    }
    
    private File getOutputMediaFile(String filename) {
        return new File(getOutputLink(filename));
    }

    private String getOutputLink(String filename) {
        String directory = "";

        // Check if storage is mounted.
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_NAME);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            directory = mediaStorageDir.getPath() + File.separator + filename;
        }
        return directory;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_SELECT_DIALOG) {
                String mediaSelected = data.getExtras().getString(SELECTED_MEDIA);
                
                if (mediaSelected.equals(MediaSelectDialog.CAMERA)) {
                    File dir = getOutputMediaFile(TEMP_JPEG_FILENAME);
                    if (dir == null) {
                        // Signal user is external storage is not connected.
                        Toast.makeText(this, STORAGE_MISSING_MSG, Toast.LENGTH_SHORT).show();
                    } else {
                        // Start Camera App.
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(dir));
                        startActivityForResult(cameraIntent, MEDIA_CAMERA);
                    }
                } else if (mediaSelected.equals(MediaSelectDialog.GALLERY)) {
                    // Start Gallery App.
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent , MEDIA_GALLERY);
                }
            } else if (requestCode == MEDIA_CAMERA) {
                
            } else if (requestCode == MEDIA_GALLERY) {
                
            }
        }
    }
}
