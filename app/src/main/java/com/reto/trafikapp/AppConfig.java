package com.reto.trafikapp;

import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;

public class AppConfig {
    public static final String BASE_URL = "https://grateful-aile-trafikapp-6e4aea32.koyeb.app";

    public static void vibrar(Context context, int ms) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}