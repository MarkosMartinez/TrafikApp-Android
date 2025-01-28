package com.reto.trafikapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
    private boolean errorCarga = false;
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
    private ImageButton imageButtonFiltro;
    public static final int REQUEST_CODE_CAMERA_VIEW = 1;
    IncidenciasFavoritosBBDD incidenciasFavoritosBBDD = new IncidenciasFavoritosBBDD(this);
    CamarasFavoritosBBDD camarasFavoritosBBDD = new CamarasFavoritosBBDD(this);
    private CheckBox checkBoxCamaras;
    private CheckBox checkBoxIncidencias;
    private CheckBox checkBoxFavoritos;


    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Llamarlo antes de los findViewById
        gif_loading = findViewById(R.id.gif_loading);
        gif_loading.setVisibility(pl.droidsonroids.gif.GifImageView.VISIBLE);
        imageButtonFiltro = findViewById(R.id.imageButtonFiltro);
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
                errorCarga = true;
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
                errorCarga = true;
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

        imageButtonFiltro.setOnClickListener(v -> {
            AppConfig.vibrar(MainActivity.this, 100);

            View popupView = getLayoutInflater().inflate(R.layout.popup_filtro, null);
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
            );

            checkBoxCamaras = popupView.findViewById(R.id.checkBoxCamaras);
            checkBoxIncidencias = popupView.findViewById(R.id.checkBoxIncidencias);
            checkBoxFavoritos = popupView.findViewById(R.id.checkBoxFavoritos);

            cargarFiltros();

            checkBoxCamaras.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (Marker marker : marcadores) {
                    if(isChecked){
                        if(marker.getTag() instanceof Camara){
                            Camara camara = (Camara) marker.getTag();
                            if (checkBoxFavoritos.isChecked()) {
                                    marker.setVisible(true);
                            }else{
                                marker.setVisible(!camarasFavoritosBBDD.esFavorito(camara.getCameraId()));
                            }
                        }
                        if(!checkBoxIncidencias.isChecked()){
                            if(marker.getTag() instanceof Incidencia) {
                                marker.setVisible(false);
                            }
                        }
                    } else if (checkBoxFavoritos.isChecked() && !checkBoxIncidencias.isChecked()) {
                        if(marker.getTag() instanceof Incidencia) {
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                                marker.setVisible(true);
                            } else if (marker.getTag() instanceof Incidencia) {
                                marker.setVisible(false);
                            }
                        } else if (marker.getTag() instanceof Camara){
                            marker.setVisible(false);
                            Camara camara = (Camara) marker.getTag();
                            if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                                marker.setVisible(true);
                            } else if (marker.getTag() instanceof Camara) {
                                marker.setVisible(false);
                            }
                        }
                    } else if (checkBoxFavoritos.isChecked() && marker.getTag() instanceof Camara && checkBoxIncidencias.isChecked()) {
                        marker.setVisible(false);
                    }else{
                        if (marker.getTag() instanceof Camara){
                            marker.setVisible(false);
                        }
                    }
                }
                guardarFiltros();
            });

            checkBoxIncidencias.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (Marker marker : marcadores) {
                    if(isChecked){
                        if(marker.getTag() instanceof Incidencia) {
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (checkBoxFavoritos.isChecked()) {
                                marker.setVisible(true);
                            } else {
                                marker.setVisible(!incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId()));
                            }
                        }
                        if(!checkBoxCamaras.isChecked()){
                            if(marker.getTag() instanceof Camara) {
                                marker.setVisible(false);
                            }
                        }
                    } else if (checkBoxFavoritos.isChecked() && !checkBoxCamaras.isChecked()) {
                        if(marker.getTag() instanceof Incidencia) {
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                                marker.setVisible(true);
                            } else if (marker.getTag() instanceof Incidencia) {
                                marker.setVisible(false);
                            }
                        } else if (marker.getTag() instanceof Camara){
                            marker.setVisible(false);
                            Camara camara = (Camara) marker.getTag();
                            if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                                marker.setVisible(true);
                            } else if (marker.getTag() instanceof Camara) {
                                marker.setVisible(false);
                            }
                        }
                    } else if (checkBoxFavoritos.isChecked() && marker.getTag() instanceof Incidencia && checkBoxCamaras.isChecked()) {
                        marker.setVisible(false);
                    }else{
                        if (marker.getTag() instanceof Incidencia){
                            marker.setVisible(false);
                        }
                    }
                }
                guardarFiltros();
            });

            checkBoxFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (Marker marker : marcadores) {
                    if(isChecked && (checkBoxIncidencias.isChecked() || checkBoxCamaras.isChecked())){
                        if(marker.getTag() instanceof Incidencia){
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (checkBoxIncidencias.isChecked() && incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                                marker.setVisible(true);
                            }
                        } else if (marker.getTag() instanceof Camara){
                            Camara camara = (Camara) marker.getTag();
                            if (checkBoxFavoritos.isChecked() && camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                                marker.setVisible(true);
                            }

                        }
                    }else if(isChecked){
                        if(marker.getTag() instanceof Incidencia){
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                                marker.setVisible(true);
                            }else{
                                marker.setVisible(false);
                            }
                        } else if (marker.getTag() instanceof Camara){
                            Camara camara = (Camara) marker.getTag();
                            if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                                marker.setVisible(true);
                            }else{
                                marker.setVisible(false);
                            }
                        }
                    }else{
                        if(marker.getTag() instanceof Incidencia){
                            Incidencia incidencia = (Incidencia) marker.getTag();
                            if (incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                                marker.setVisible(false);
                            }
                        } else if (marker.getTag() instanceof Camara){
                            Camara camara = (Camara) marker.getTag();
                            if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                                marker.setVisible(false);
                            }
                        }
                    }

                }
                guardarFiltros();
            });

            popupWindow.showAsDropDown(v);
        });

    }

    private void guardarFiltros(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("camaras", checkBoxCamaras.isChecked());
        editor.putBoolean("incidencias", checkBoxIncidencias.isChecked());
        editor.putBoolean("favoritos", checkBoxFavoritos.isChecked());
        editor.apply();
    }

    private void cargarFiltros(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        checkBoxCamaras.setChecked(sharedPreferences.getBoolean("camaras", true));
        checkBoxIncidencias.setChecked(sharedPreferences.getBoolean("incidencias", true));
        checkBoxFavoritos.setChecked(sharedPreferences.getBoolean("favoritos", true));

        if (!sharedPreferences.contains("camaras") || !sharedPreferences.contains("incidencias") || !sharedPreferences.contains("favoritos")) {
            guardarFiltros();
        }
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
                    errorCarga = true;
                    Log.e("MainActivity", "Error al cambiar el estilo del mapa.");
                }
            } catch (Resources.NotFoundException e) {
                errorCarga = true;
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
        if(++carga >= cargamax && !errorCarga) {
            gif_loading.setVisibility(GifImageView.INVISIBLE);
        }else if (errorCarga){
            gif_loading.setImageResource(R.drawable.advertencia);
            gif_loading.setVisibility(GifImageView.VISIBLE);
        }
    }

    public void addIncidencias() {
        runOnUiThread(() -> {
            mapView.getMapAsync(googleMap -> {
                if (incidenciasFavoritosBBDD.getReadableDatabase().isOpen()) {
                    for (Incidencia incidencia : incidencias) {
                        LatLng latLng = new LatLng(incidencia.getLatitude(), incidencia.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marcador_incidencia), widthMarcador, heightMarcador, false)))
                                .alpha(opacidad);
                        Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(incidencia);
                        marcadores.add(marker);
                    }

                    // Actualizar los iconos de los favoritos
                    for (Marker marker : marcadores) {
                        Incidencia incidencia = (Incidencia) marker.getTag();
                        if (incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marcador_incidencia_fav), widthMarcadorFav, heightMarcadorFav, false)));
                        }
                    }

                    googleMap.setOnMapClickListener(mapClickLatLng -> {
                        for (Marker m : marcadores) {
                            m.setAlpha(opacidad);
                        }
                    });
                } else {
                    Log.e("MainActivity", "Database connection is closed.");
                }
            });
        });
    }

    public void addCamaras(){
        runOnUiThread(() -> {
            for (Camara camara : camaras) {
                LatLng latLng = new LatLng(camara.getLatitude(), camara.getLongitude());
                mapView.getMapAsync(googleMap -> {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marcador_camara), widthMarcador, heightMarcador, false)))
                            .alpha(opacidad);
                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag(camara);
                    marcadores.add(marker);

                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Object tag = marker.getTag();
                            if (tag instanceof Incidencia) {
                                Incidencia incidencia = (Incidencia) tag;
                                incidenciasFavoritosBBDD.alternarFavorito(incidencia);
                                AppConfig.vibrar(MainActivity.this, 100);
                                Toast.makeText(getApplicationContext(), R.string.activity_main_toast_favoritoAlterado, Toast.LENGTH_SHORT).show();
                                int marcadorIncidenciaIcono = incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId()) ? R.drawable.marcador_incidencia_fav : R.drawable.marcador_incidencia;
                                int widthMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? widthMarcadorFav : widthMarcador;
                                int heightMarcadorIncidencia = marcadorIncidenciaIcono == R.drawable.marcador_incidencia_fav ? heightMarcadorFav : heightMarcador;
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), marcadorIncidenciaIcono), widthMarcadorIncidencia, heightMarcadorIncidencia, false)));
                                marker.showInfoWindow();
                            } else if (tag instanceof Camara) {
                                Camara camara = (Camara) tag;
                                new CamaraActionsBottomSheet(MainActivity.this, camarasFavoritosBBDD, marker).ver(camara);
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

            // Actualizar los iconos de los favoritos
            mapView.getMapAsync(googleMap -> {
                for (Marker marker : marcadores) {
                    Object tag = marker.getTag();
                    if (tag instanceof Camara) {
                        Camara camara = (Camara) tag;
                        if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marcador_camara_fav), widthMarcadorFav, heightMarcadorFav, false)));
                        }
                    }
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA_VIEW && resultCode == RESULT_OK) {
            Camara camara = (Camara) data.getSerializableExtra("camaraId");
            if (camara != null) {
                for (Marker marker : marcadores) {
                    Object tag = marker.getTag();
                    if (tag instanceof Camara) {
                        Camara markerCamara = (Camara) tag;
                        if (markerCamara.getCameraId() == camara.getCameraId()) {
                            int marcadorCamaraIcono = camarasFavoritosBBDD.esFavorito(camara.getCameraId()) ? R.drawable.marcador_camara_fav : R.drawable.marcador_camara;
                            int widthCamaraIncidencia = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? widthMarcadorFav : widthMarcador;
                            int heightCamaraIncidencia = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? heightMarcadorFav : heightMarcador;
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                    Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), marcadorCamaraIcono),widthCamaraIncidencia,heightCamaraIncidencia,false)));
                            break;
                        }
                    }
                }
            }
        }
    }

}
