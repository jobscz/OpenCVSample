package com.mark.opencvsample.utils;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.mark.opencvsample.App;

public class ContentResolverHelper {

    public static Uri buildImageUri(String name) throws Throwable {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, name);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/*");
        return App.getApp().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static Bitmap readBitmap(Uri imageUri) throws Throwable {
        ParcelFileDescriptor parcelFileDescriptor =
                App.getApp().getContentResolver().openFileDescriptor(imageUri, "r");
        return BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
    }

}
