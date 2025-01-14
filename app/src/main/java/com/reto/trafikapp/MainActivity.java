package com.reto.trafikapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Llamarlo antes de los findViewById
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        // Inicia la carga del mapa
        mapView.getMapAsync(this);

        ImageButton ibtnLogout = findViewById(R.id.imageButtonLogout);

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

        LatLng ejemplo = new LatLng(43.347160, -1.793184);  // Ejemplo: Latitud y Longitud de Sydney
        googleMap.addMarker(new MarkerOptions().position(ejemplo).title("Marcador en Plaiaundi"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ejemplo));
        googleMap.getUiSettings().setMapToolbarEnabled(false); // Ocultar los iconos de maps de abajo a la derecha
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
}
