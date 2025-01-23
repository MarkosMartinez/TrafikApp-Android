package com.reto.trafikapp;

import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;

public class AppConfig {
    public static final String BASE_URL = "http://10.8.0.3:8080";

    public static void vibrar(Context context, int ms) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}