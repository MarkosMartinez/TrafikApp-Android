package com.reto.trafikapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reto.trafikapp.adapter.MarcadorAdapter;
import com.reto.trafikapp.model.Camara;
import com.reto.trafikapp.model.Incidencia;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private pl.droidsonroids.gif.GifImageView gif_loading;
    private final int cargamax = 3;
    private int carga = 0;
    private List<Incidencia> incidencias;
    private List<Camara> camaras;
    LlamadasAPI llamadasAPI = new LlamadasAPI();
    private final float opacidad = 0.70f;
    private List<Marker> marcadores = new ArrayList<>();


    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Llamarlo antes de los findViewById
        gif_loading = findViewById(R.id.gif_loading);
        gif_loading.setVisibility(pl.droidsonroids.gif.GifImageView.VISIBLE);
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        // Inicia la carga del mapa
        mapView.getMapAsync(this);

        ImageButton ibtnLogout = findViewById(R.id.imageButtonLogout);

        //Obtener las incidencias
        llamadasAPI.getIncidencias(new LlamadasAPI.IncidenciasCallback() {
            @Override
            public void onSuccess(List<Incidencia> incidencias) {
                MainActivity.this.incidencias = incidencias;
                Log.d("MainActivity", "Incidencias: " + incidencias);
                addIncidencias();
                ocultarCarga();
            }

            @Override
            public void onFailure() {
                Log.d("MainActivity", "Error al obtener las incidencias.");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.activity_main_toast_error_incidencias, Toast.LENGTH_LONG).show());
                ocultarCarga();
            }
        });

        //Obtener las camaras
        llamadasAPI.getCamaras(new LlamadasAPI.CamarasCallback() {
            @Override
            public void onSuccess(List<Camara> camaras) {
                MainActivity.this.camaras = camaras;
                Log.d("MainActivity", "Camaras: " + camaras);
                addCamaras();
                ocultarCarga();
            }

            @Override
            public void onFailure() {
                Log.d("MainActivity", "Error al obtener las camaras.");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.activity_main_toast_error_camaras, Toast.LENGTH_LONG).show());
                ocultarCarga();
            }
        });


        ibtnLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("estaLogueado", false);
            editor.apply();
            Toast.makeText(MainActivity.this, R.string.activity_main_toast_logout, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng euskadi = new LatLng(43.010365, -2.609979);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(euskadi, 7));

        googleMap.setInfoWindowAdapter(new MarcadorAdapter(getLayoutInflater()));

        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow(); // Muestra la ventana de información al hacer clic en el marcador
            return true; // Devuelve true para indicar que has manejado el evento
        });

        // Ocultando la brujula, icono de carga y iconos de abajo
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        ocultarCarga();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    public void ocultarCarga(){
        if(++carga >= cargamax) {
            gif_loading.setVisibility(GifImageView.INVISIBLE);
        }
    }

    public void addIncidencias(){
        runOnUiThread(() -> {
            for (Incidencia incidencia : incidencias) {
                LatLng latLng = new LatLng(incidencia.getLatitude(), incidencia.getLongitude());
                mapView.getMapAsync(googleMap -> {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .alpha(opacidad);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(incidencia);
                    marcadores.add(marker);
                    googleMap.setOnMarkerClickListener(clickedMarker -> {
                        for (Marker m : marcadores) {
                            m.setAlpha(opacidad);
                        }
                        clickedMarker.setAlpha(1.0f);
                        return false;
                    });

                    googleMap.setOnMapClickListener(mapClickLatLng -> {
                        for (Marker m : marcadores) {
                            m.setAlpha(opacidad);
                        }
                    });
                });
            }
        });
    }

    public void addCamaras(){
        runOnUiThread(() -> {
            for (Camara camara : camaras) {
                LatLng latLng = new LatLng(camara.getLatitude(), camara.getLongitude());
                mapView.getMapAsync(googleMap -> {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title("Camara - " + camara.getCameraName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .alpha(opacidad);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(camara);
                    marcadores.add(marker);
                    googleMap.setOnMarkerClickListener(clickedMarker -> {
                        for (Marker m : marcadores) {
                            m.setAlpha(opacidad);
                        }
                        clickedMarker.setAlpha(1.0f);
                        return false;
                    });

                    googleMap.setOnMapClickListener(mapClickLatLng -> {
                        for (Marker m : marcadores) {
                            m.setAlpha(opacidad);
                        }
                    });
                });
            }
        });
    }

}
