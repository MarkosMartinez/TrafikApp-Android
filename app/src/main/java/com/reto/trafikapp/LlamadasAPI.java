package com.reto.trafikapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LlamadasAPI {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void login(String email, String pass, LoginCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(AppConfig.BASE_URL + "/api/login");
                Log.d("LlamadasAPI", "Connecting to " + url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                // Prepare JSON data
                String jsonInputString = "{\"email\": \"" + email + "\", \"contrasena\": \"" + pass + "\"}";

                // Send data
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read response
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("LlamadasAPI", "Login successful. Response: " + response.toString());
                        // Assuming the server returns "true" for successful login
                        callback.onSuccess(true);
                    }
                } else {
                    Log.d("LlamadasAPI", "Login failed. Response Code: " + responseCode + ", envio: " + jsonInputString);
                    callback.onFailure();
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    public interface LoginCallback {
        void onSuccess(boolean isSuccess);
        void onFailure();
    }
}