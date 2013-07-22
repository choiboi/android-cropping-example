package com.choiboi.imagecroppingexample;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class CropActivity extends Activity {
    
    private ImageView mImg;
    private ImageView mTemplateImg;
    private int mScreenWidth;
    private int mScreenHeight;
    
    // Constants
    private final String TEMP_JPEG_FILENAME = "img_temp.jpg";
    private final String APP_NAME = "ImageCropExample";
    private final String STORAGE_MISSING_MSG = "Your Phone is Currently Not Connected to Any Storage Device!";
    public static final String SELECTED_MEDIA = "SELECTED_MEDIA";
    
    public static final int MEDIA_SELECT_DIALOG = 0;
    public static final int MEDIA_CAMERA = 1;
    public static final int MEDIA_GALLERY = 2;
    
    private final static int IMG_MAX_SIZE = 1000;
    private final static int IMG_MAX_SIZE_MDPI = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        
        mImg = (ImageView) findViewById(R.id.cp_img);
        mTemplateImg = (ImageView) findViewById(R.id.cp_face_template);
        
        // Get screen size in pixels.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        mScreenWidth = metrics.widthPixels;
    }
    
    public void onCropImageButton(View v) {
        
    }
    
    public void onChangeTemplateButton(View v) {
        
    }
    
    public void onChangeImageButton(View v) {
        Intent intent = new Intent(this, MediaSelectDialog.class);
        startActivityForResult(intent, MEDIA_SELECT_DIALOG);
    }
    
    /*
     * Adjust the size of bitmap before loading it to memory.
     */
    private void setSelectedImage(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (mScreenWidth == 320 && mScreenHeight == 480) {
            options.inSampleSize = calculateImageSize(options, IMG_MAX_SIZE_MDPI);
        } else {
            options.inSampleSize = calculateImageSize(options, IMG_MAX_SIZE);
        }
        options.inJustDecodeBounds = false;
        Bitmap photoImg = BitmapFactory.decodeFile(path, options);
        mImg.setImageBitmap(photoImg);
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
    
    private String getGalleryImagePath(Intent data) {
        Uri imgUri = data.getData();
        String filePath = "";
        if (data.getType() == null) {
            // For getting images from gallery.
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(imgUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else if (data.getType().equals("image/jpeg") || data.getType().equals("image/png")) {
            // For getting images from dropbox.
            filePath = imgUri.getPath();
        }
        return filePath;
    }
    
    private int calculateImageSize(BitmapFactory.Options opts, int threshold) {
        int scaleFactor = 1;
        final int height = opts.outHeight;
        final int width = opts.outWidth;

        if (width >= height) {
            scaleFactor = Math.round((float) width / threshold);
        } else {
            scaleFactor = Math.round((float) height / threshold);
        }

        return scaleFactor;
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
                String path = getOutputLink(TEMP_JPEG_FILENAME);
                setSelectedImage(path);
            } else if (requestCode == MEDIA_GALLERY) {
                String path = getGalleryImagePath(data);
                setSelectedImage(path);
            }
        }
    }
}
