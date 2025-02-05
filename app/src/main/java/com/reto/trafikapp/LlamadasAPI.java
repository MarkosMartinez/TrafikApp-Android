package com.reto.trafikapp;

import android.util.Log;

import com.reto.trafikapp.configuration.AppConfig;
import com.reto.trafikapp.model.Camara;
import com.reto.trafikapp.model.Incidencia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LlamadasAPI {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    //Método para realizar el login en la aplicación
    public void login(String email, String pass, LoginCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(AppConfig.BASE_URL + "/api/login");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                String jsonInputString = "{\"email\": \"" + email + "\", \"contrasena\": \"" + pass + "\"}";

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Leer la respuesta
                int codigoRespuesta = urlConnection.getResponseCode();
                if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("LlamadasAPI", "Logueo correcto. Respuesta: " + response.toString());
                        callback.onSuccess(true);
                    }
                } else {
                    Log.d("LlamadasAPI", "Logueo erroneo. Codigo de respuesta: " + codigoRespuesta + ", envio: " + jsonInputString);
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
        void onSuccess(boolean esCorrecto);
        void onFailure();
    }

    //Método para obtener las incidencias
    public void getIncidencias(IncidenciasCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(AppConfig.BASE_URL + "/api/incidencias");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int codigoRespuesta = urlConnection.getResponseCode();
                if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("LlamadasAPI", "Incidencias obtenidas correctamente. Respuesta: " + response.toString());

                        // Convertir la respuesta JSON a una lista de incidencias
                        JSONArray jsonArray = new JSONArray(response.toString());
                        List<Incidencia> incidencias = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Incidencia incidencia = new Incidencia(
                                    jsonObject.getString("incidenceId"),
                                    jsonObject.getInt("sourceId"),
                                    jsonObject.getString("incidenceType"),
                                    jsonObject.getString("province"),
                                    jsonObject.getString("cause"),
                                    jsonObject.getString("cityTown"),
                                    jsonObject.getString("startDate"),
                                    jsonObject.getString("road"),
                                    jsonObject.getString("pkStart"),
                                    jsonObject.getString("pkEnd"),
                                    jsonObject.getString("direction"),
                                    jsonObject.getDouble("latitude"),
                                    jsonObject.getDouble("longitude"),
                                    jsonObject.getBoolean("creada")
                            );
                            incidencias.add(incidencia);
                        }
                        callback.onSuccess(incidencias);
                    }
                } else {
                    Log.d("LlamadasAPI", "Error al obtener las incidencias. Codigo de respuesta: " + codigoRespuesta);
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

    public interface IncidenciasCallback {
        void onSuccess(List<Incidencia> incidencias);
        void onFailure();
    }

    //Método para obtener las cámaras
    public void getCamaras(CamarasCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(AppConfig.BASE_URL + "/api/camaras");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int codigoRespuesta = urlConnection.getResponseCode();
                if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("LlamadasAPI", "Camaras obtenidas correctamente. Respuesta: " + response.toString());

                        // Convertir la respuesta JSON a una lista de incidencias
                        JSONArray jsonArray = new JSONArray(response.toString());
                        List<Camara> camaras = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Camara camara = new Camara(
                                    jsonObject.getInt("id"),
                                    jsonObject.getInt("cameraId"),
                                    jsonObject.getInt("sourceId"),
                                    jsonObject.getString("cameraName"),
                                    jsonObject.getString("urlImage"),
                                    jsonObject.getDouble("latitude"),
                                    jsonObject.getDouble("longitude"),
                                    jsonObject.getString("road"),
                                    jsonObject.getString("kilometer"),
                                    jsonObject.getString("address")
                            );
                            camaras.add(camara);
                        }
                        callback.onSuccess(camaras);
                    }
                } else {
                    Log.d("LlamadasAPI", "Error al obtener las camaras. Codigo de respuesta: " + codigoRespuesta);
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

    public interface CamarasCallback {
        void onSuccess(List<Camara> camaras);
        void onFailure();
    }

}