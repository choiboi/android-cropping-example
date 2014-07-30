package com.choiboi.imagecroppingexample;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.choiboi.imagecroppingexample.gestures.MoveGestureDetector;
import com.choiboi.imagecroppingexample.gestures.RotateGestureDetector;

public class CropActivity extends Activity implements OnTouchListener {
    
    // Member fields.
    private ImageView mImg;
    private ImageView mTemplateImg;
    private int mScreenWidth;
    private int mScreenHeight;
    private CropHandler mCropHandler;
    private static ProgressDialog mProgressDialog;
    private int mSelectedVersion;
    
    private Matrix mMatrix = new Matrix();
    private float mScaleFactor = 0.8f;
    private float mRotationDegrees = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private int mImageHeight, mImageWidth;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    
    // Constants
    public static final int MEDIA_GALLERY = 1;
    public static final int TEMPLATE_SELECTION = 2;
    public static final int DISPLAY_IMAGE = 3;
    
    private final static int IMG_MAX_SIZE = 1000;
    private final static int IMG_MAX_SIZE_MDPI = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        
        mSelectedVersion = getIntent().getExtras().getInt(MainActivity.CROP_VERSION_SELECTED_KEY, -1);
        
        mImg = (ImageView) findViewById(R.id.cp_img);
        mTemplateImg = (ImageView) findViewById(R.id.cp_face_template);
        mImg.setOnTouchListener(this);
        
        // Get screen size in pixels.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        mScreenWidth = metrics.widthPixels;
        
        // Set template image accordingly to device screen size.
        if (mScreenWidth == 320 && mScreenHeight == 480) {
            Bitmap faceTemplate = BitmapFactory.decodeResource(getResources(), R.drawable.large_template);
            faceTemplate = Bitmap.createScaledBitmap(faceTemplate, 218, 300, true);
            mTemplateImg.setImageBitmap(faceTemplate);
        }
        
        // Load temp image.
        Bitmap photoImg = BitmapFactory.decodeResource(getResources(), R.drawable.temp_image);
        mImg.setImageBitmap(photoImg);
        mImageHeight = photoImg.getHeight();
        mImageWidth = photoImg.getWidth();
        
        // View is scaled by matrix, so scale initially
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mImg.setImageMatrix(mMatrix);
        
        // Setup Gesture Detectors
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());
        mRotateDetector = new RotateGestureDetector(getApplicationContext(), new RotateListener());
        mMoveDetector = new MoveGestureDetector(getApplicationContext(), new MoveListener());
        
        // Instantiate Thread Handler.
        mCropHandler = new CropHandler(this);
    }
    
    public void onCropImageButton(View v) {
        // Create progress dialog and display it.
        mProgressDialog = new ProgressDialog(v.getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Cropping Image\nPlease Wait.....");
        mProgressDialog.show();

        // Setting values so that we can retrive the image from 
        // ImageView multiple times.
        mImg.buildDrawingCache(true);
        mImg.setDrawingCacheEnabled(true);
        mTemplateImg.buildDrawingCache(true);
        mTemplateImg.setDrawingCacheEnabled(true);
        
        // Create new thread to crop.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Crop image using the correct template size.
                Bitmap croppedImg = null;
                if (mScreenWidth == 320 && mScreenHeight == 480) {
                    if (mSelectedVersion == MainActivity.VERSION_1) {
                        croppedImg = ImageProcess.cropImage(mImg.getDrawingCache(true), mTemplateImg.getDrawingCache(true), 218, 300);
                    } else {
                        croppedImg = ImageProcess.cropImageVer2(mImg.getDrawingCache(true), mTemplateImg.getDrawingCache(true), 218, 300);
                    }
                } else {
                    // TODO: Change template size.
                    if (mSelectedVersion == MainActivity.VERSION_1) {
                        croppedImg = ImageProcess.cropImage(mImg.getDrawingCache(true), mTemplateImg.getDrawingCache(true), 650, 894);
                    } else {
                        croppedImg = ImageProcess.cropImageVer2(mImg.getDrawingCache(true), mTemplateImg.getDrawingCache(true), 650, 894);
                    }
                }
                mImg.setDrawingCacheEnabled(false);
                mTemplateImg.setDrawingCacheEnabled(false);
                
                // Send a message to the Handler indicating the Thread has finished.
                mCropHandler.obtainMessage(DISPLAY_IMAGE, -1, -1, croppedImg).sendToTarget();
            }
        }).start();
    }
    
    public void onChangeTemplateButton(View v) {
        Intent intent = new Intent(this, TemplateSelectDialog.class);
        startActivityForResult(intent, TEMPLATE_SELECTION);
    }
    
    public void onChangeImageButton(View v) {
        // Start Gallery App.
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MEDIA_GALLERY);
    }
    
    /*
     * Adjust the size of bitmap before loading it to memory.
     * This will help the phone by not taking up a lot memory.
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
        mImageHeight = photoImg.getHeight();
        mImageWidth = photoImg.getWidth();
        mImg.setImageBitmap(photoImg);
    }

    /*
     * Retrieves the path to the selected image from the Gallery app.
     */
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
        } 
        return filePath;
    }
    
    /*
     * Calculation used to determine by what factor images need to be reduced by.
     * Images with its longest side below the threshold will not be resized.
     */
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
            if (requestCode == MEDIA_GALLERY) {
                String path = getGalleryImagePath(data);
                setSelectedImage(path);
            } else if (requestCode == TEMPLATE_SELECTION) {
                int pos = data.getExtras().getInt(TemplateSelectDialog.POSITION);
                Bitmap templateImg = null;
                
                // Change template according to what the user has selected.
                switch(pos) {
                case 0:
                    templateImg = BitmapFactory.decodeResource(getResources(), R.drawable.face_oblong);
                    break;
                case 1:
                    templateImg = BitmapFactory.decodeResource(getResources(), R.drawable.face_oval);
                    break;
                case 2:
                    templateImg = BitmapFactory.decodeResource(getResources(), R.drawable.face_round);
                    break;
                case 3:
                    templateImg = BitmapFactory.decodeResource(getResources(), R.drawable.face_square);
                    break;
                case 4:
                    templateImg = BitmapFactory.decodeResource(getResources(), R.drawable.face_triangular);
                    break;
                }
                
                // Resize template if necessary.
                if (mScreenWidth == 320 && mScreenHeight == 480) {
                    templateImg = Bitmap.createScaledBitmap(templateImg, 218, 300, true);
                }
                mTemplateImg.setImageBitmap(templateImg);
            }
        }
    }
    
    private static class CropHandler extends Handler {
        WeakReference<CropActivity> mThisCA;
        
        CropHandler(CropActivity ca) {
            mThisCA = new WeakReference<CropActivity>(ca);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            CropActivity ca = mThisCA.get();
            if (msg.what == DISPLAY_IMAGE) {
                mProgressDialog.dismiss();
                Bitmap cropImg = (Bitmap) msg.obj;
                Log.d("TAG", "final image size x: " + cropImg.getWidth() + " y: " + cropImg.getHeight());
                
                // Setup an AlertDialog to display cropped image.
                AlertDialog.Builder builder = new AlertDialog.Builder(ca);
                builder.setTitle("Final Cropped Image");
                builder.setIcon(new BitmapDrawable(ca.getResources(), cropImg));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                          dialog.cancel();
                      }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
    
    
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);

        float scaledImageCenterX = (mImageWidth * mScaleFactor) / 2;
        float scaledImageCenterY = (mImageHeight * mScaleFactor) / 2;

        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees, scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);

        ImageView view = (ImageView) v;
        view.setImageMatrix(mMatrix);
        return true;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            mRotationDegrees -= detector.getRotationDegreesDelta();
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            mFocusX += d.x;
            mFocusY += d.y;

            return true;
        }
    }
}
