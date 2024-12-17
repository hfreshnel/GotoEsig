package com.example.gotoesig.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;

public class BitmapHandler {
    /**
     * Encode une image Bitmap en Base64.
     * @param bitmap Image à encoder.
     * @return Chaîne Base64 représentant l'image.
     */
    public String encodeImageToBase64(@NonNull Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Décoder une chaîne Base64 en une image Bitmap.
     * @param base64String Chaîne Base64 à décoder.
     * @return Image Bitmap décodée.
     */
    public Bitmap decodeBase64ToImage(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
