package com.assessbyphone.zambia.ImageLibrary;

import android.content.Context;
import android.graphics.Bitmap;

public class BitmapImage implements SmartImage {
    private final Bitmap bitmap;

    public BitmapImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(Context context) {
        return bitmap;
    }
}