package com.reto.trafikapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reto.trafikapp.BBDD.CamarasFavoritosBBDD;
import com.reto.trafikapp.BBDD.IncidenciasFavoritosBBDD;
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
    private final int widthMarcador = 78;
    private final int widthMarcadorFav = 120;
    private final int heightMarcador = 110;
    private final int heightMarcadorFav = 130;
    private List<Incidencia> incidencias;
    private List<Camara> camaras;
    LlamadasAPI llamadasAPI = new LlamadasAPI();
    private final float opacidad = 0.70f;
    private List<Marker> marcadores = new ArrayList<>();
    private GoogleMap mMap;
    IncidenciasFavoritosBBDD incidenciasFavoritosBBDD = new IncidenciasFavoritosBBDD(this);
    CamarasFavoritosBBDD camarasFavoritosBBDD = new CamarasFavoritosBBDD(this);


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

        ImageButton imgBtnLogout = findViewById(R.id.imageButtonLogout);

        //Obtener las incidencias
        llamadasAPI.getIncidencias(new LlamadasAPI.IncidenciasCallback() {
            @Override
            public void onSuccess(List<Incidencia> incidencias) {
                MainActivity.this.incidencias = incidencias;
                Log.d("MainActivity", "Incidencias: " + incidencias);
                addIncidencias();
                ocultarCarga();

                //Comprobar las incidencias favoritas
                incidenciasFavoritosBBDD.comprobarIncidencias(incidencias);
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

                //Comprobar las camaras favoritas
                camarasFavoritosBBDD.comprobarCamaras(camaras);
            }

            @Override
            public void onFailure() {
                Log.d("MainActivity", "Error al obtener las camaras.");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.activity_main_toast_error_camaras, Toast.LENGTH_LONG).show());
                ocultarCarga();
            }
        });

        imgBtnLogout.setOnClickListener(v -> {
            AppConfig.vibrar(MainActivity.this, 200);
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("estaLogueado", false);
            editor.apply();
            incidenciasFavoritosBBDD.vaciar();
            camarasFavoritosBBDD.vaciar();
            Toast.makeText(MainActivity.this, R.string.activity_main_toast_logout, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        imgBtnLogout.setOnLongClickListener(v -> {
            AppConfig.vibrar(MainActivity.this, 100);
            Toast.makeText(MainActivity.this, R.string.activity_main_toast_actualizandoMapa, Toast.LENGTH_SHORT).show();
            recreate();
            return true;
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng euskadi = new LatLng(43.189985,-2.407536);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(euskadi, 9));

        // Configurar el adaptador de InfoWindow
        mMap.setInfoWindowAdapter(new MarcadorAdapter(getLayoutInflater(), incidenciasFavoritosBBDD, camarasFavoritosBBDD));


        int modoOscuroFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (modoOscuroFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            try {
                boolean correcto = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
                if (!correcto) {
                    Log.e("MainActivity", "Error al cambiar el estilo del mapa.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("MainActivity", "Error al buscar el estilo. Error: ", e);
            }
        }

        // Ocultando la brujula, icono de carga y iconos de abajo
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
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

    public void addIncidencias() {
        runOnUiThread(() -> {
            mapView.getMapAsync(googleMap -> {
                for (Incidencia incidencia : incidencias) {
                    LatLng latLng = new LatLng(incidencia.getLatitude(), incidencia.getLongitude());
                    int marcadorIncidenciaIcono = incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId()) ? R.drawable.marcador_incidencia_fav : R.drawable.marcador_incidencia;
                    int widthMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? widthMarcadorFav : widthMarcador;
                    int heightMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? heightMarcadorFav : heightMarcador;
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), marcadorIncidenciaIcono), widthMarcadorIncidencia, heightMarcadorIncidencia, false)))
                            .alpha(opacidad);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(incidencia);
                    marcadores.add(marker);
                }

                googleMap.setOnMapClickListener(mapClickLatLng -> {
                    for (Marker m : marcadores) {
                        m.setAlpha(opacidad);
                    }
                });
            });
        });
    }

    public void addCamaras(){
        runOnUiThread(() -> {
            for (Camara camara : camaras) {
                LatLng latLng = new LatLng(camara.getLatitude(), camara.getLongitude());
                mapView.getMapAsync(googleMap -> {
                    int marcadorCamaraIcono = camarasFavoritosBBDD.esFavorito(camara.getCameraId()) ? R.drawable.marcador_camara_fav : R.drawable.marcador_camara;
                    int widthMarcadorCamara = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? widthMarcadorFav : widthMarcador;
                    int heightMarcadorCamara = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? heightMarcadorFav : heightMarcador;
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), marcadorCamaraIcono), widthMarcadorCamara, heightMarcadorCamara, false)))
                            .alpha(opacidad);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(camara);
                    marcadores.add(marker);

                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            // Obtener la incidencia del marcador
                            Object tag = marker.getTag();
                            if (tag instanceof Incidencia) {
                                Incidencia incidencia = (Incidencia) tag;

                                // Alternar favoritos la incidencia
                                incidenciasFavoritosBBDD.alternarFavorito(incidencia);
                                AppConfig.vibrar(MainActivity.this, 100);
                                Toast.makeText(getApplicationContext(), R.string.activity_main_toast_favoritoAlterado, Toast.LENGTH_SHORT).show();
                                int marcadorIncidenciaIcono = incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId()) ? R.drawable.marcador_incidencia_fav : R.drawable.marcador_incidencia;
                                int widthMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? widthMarcadorFav : widthMarcador;
                                int heightMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? heightMarcadorFav : heightMarcador;
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), marcadorIncidenciaIcono), widthMarcadorIncidencia, heightMarcadorIncidencia, false)));
                                marker.showInfoWindow();
                            }else{
                                if (tag instanceof Camara) {
                                    Camara camara = (Camara) tag;
                                    new CameraActionsBottomSheet(MainActivity.this, camarasFavoritosBBDD, marker).ver(camara);
                                }
                            }

                        }
                    });

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
