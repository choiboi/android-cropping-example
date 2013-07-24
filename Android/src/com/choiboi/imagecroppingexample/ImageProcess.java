package com.choiboi.imagecroppingexample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class ImageProcess {
    
    /*
     * This method gets the image inside the template area and returns that bitmap.
     * Process of how this method works:
     *     1) Combine img and templateImage together with templateImage being on the top.
     *     2) Create a new blank bitmap using the given width and height, which is the 
     *        size of the template on the screen.
     *     3) Starting in the center go through the image quadrant by quadrant copying 
     *        the pixel value onto the new blank bitmap.
     *     4) Once it hits the pixel colour values of the template lines, then set the pixel
     *        values from that point on transparent.
     *     5) Return the cropped bitmap.
     */
    public static Bitmap cropImage(Bitmap img, Bitmap templateImage, int width, int height) {
        // Merge two images together.
        Bitmap bm = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas combineImg = new Canvas(bm);
        combineImg.drawBitmap(img, 0f, 0f, null);
        combineImg.drawBitmap(templateImage, 0f, 0f, null);
        
        // Create new blank ARGB bitmap.
        Bitmap finalBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        // Get the coordinates for the middle of combineImg.
        int hMid = bm.getHeight() / 2;
        int wMid = bm.getWidth() / 2;
        int hfMid = finalBm.getHeight() / 2;
        int wfMid = finalBm.getWidth() / 2;

        int y2 = hfMid;
        int x2 = wfMid;

        for (int y = hMid; y >= 0; y--) {
            boolean template = false;
            // Check Upper-left section of combineImg.
            for (int x = wMid; x >= 0; x--) {
                if (x2 < 0) {
                    break;
                }

                int px = bm.getPixel(x, y);
                if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                    template = true;
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else if (template) {
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else {
                    finalBm.setPixel(x2, y2, px);
                }
                x2--;
            }
            // Check upper-right section of combineImage.
            x2 = wfMid;
            template = false;
            for (int x = wMid; x < bm.getWidth(); x++) {
                if (x2 >= finalBm.getWidth()) {
                    break;
                }

                int px = bm.getPixel(x, y);
                if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                    template = true;
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else if (template) {
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else {
                    finalBm.setPixel(x2, y2, px);
                }
                x2++;
            }

            // Once we reach the top-most part on the template line, set pixel value transparent
            // from that point on.
            int px = bm.getPixel(wMid, y);
            if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                for (int y3 = y2; y3 >= 0; y3--) {
                    for (int x3 = 0; x3 < finalBm.getWidth(); x3++) {
                        finalBm.setPixel(x3, y3, Color.TRANSPARENT);
                    }
                }
                break;
            }

            x2 = wfMid;
            y2--;
        }

        x2 = wfMid;
        y2 = hfMid;
        for (int y = hMid; y <= bm.getHeight(); y++) {
            boolean template = false;
            // Check bottom-left section of combineImage.
            for (int x = wMid; x >= 0; x--) {
                if (x2 < 0) {
                    break;
                }

                int px = bm.getPixel(x, y);
                if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                    template = true;
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else if (template) {
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else {
                    finalBm.setPixel(x2, y2, px);
                }
                x2--;
            }

            // Check bottom-right section of combineImage.
            x2 = wfMid;
            template = false;
            for (int x = wMid; x < bm.getWidth(); x++) {
                if (x2 >= finalBm.getWidth()) {
                    break;
                }

                int px = bm.getPixel(x, y);
                if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                    template = true;
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else if (template) {
                    finalBm.setPixel(x2, y2, Color.TRANSPARENT);
                } else {
                    finalBm.setPixel(x2, y2, px);
                }
                x2++;
            }

            // Once we reach the bottom-most part on the template line, set pixel value transparent
            // from that point on.
            int px = bm.getPixel(wMid, y);
            if (Color.red(px) == 234 && Color.green(px) == 157 && Color.blue(px) == 33) {
                for (int y3 = y2; y3 < finalBm.getHeight(); y3++) {
                    for (int x3 = 0; x3 < finalBm.getWidth(); x3++) {
                        finalBm.setPixel(x3, y3, Color.TRANSPARENT);
                    }
                }
                break;
            }

            x2 = wfMid;
            y2++;
        }
        
        // Get rid of images that we finished with to save memory.
        img.recycle();
        templateImage.recycle();
        bm.recycle();
        return finalBm;
    }
}
