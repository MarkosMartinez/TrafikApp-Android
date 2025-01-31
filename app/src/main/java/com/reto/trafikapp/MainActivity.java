package com.reto.trafikapp;

import android.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import pl.droidsonroids.gif.GifImageView;

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


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    IncidenciasFavoritosBBDD incidenciasFavoritosBBDD = new IncidenciasFavoritosBBDD(this);
    CamarasFavoritosBBDD camarasFavoritosBBDD = new CamarasFavoritosBBDD(this);
    LlamadasAPI llamadasAPI = new LlamadasAPI();
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
    private final float opacidad = 0.70f;
    private List<Marker> marcadores = new ArrayList<>();
    private GoogleMap mMap;
    private ImageButton imageButtonFiltro;
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
            SharedPreferences spConfig = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editorConfig = spConfig.edit();
            editorConfig.putBoolean("estaLogueado", false);
            editorConfig.apply();

            //Para eliminar los favoritos al cerrar sesion
            //incidenciasFavoritosBBDD.vaciar();
            //camarasFavoritosBBDD.vaciar();

            //Para restaurar los filtros al cerrar sesion
            SharedPreferences spFiltros = getSharedPreferences("filtro", MODE_PRIVATE);
            SharedPreferences.Editor editorFiltros = spFiltros.edit();
            editorFiltros.putBoolean("camaras", true);
            editorFiltros.putBoolean("incidencias", true);
            editorFiltros.putBoolean("favoritos", true);
            editorFiltros.apply();

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

            popupWindow.showAsDropDown(v, 0, 15);

            checkBoxCamaras = popupView.findViewById(R.id.checkBoxCamaras);
            checkBoxIncidencias = popupView.findViewById(R.id.checkBoxIncidencias);
            checkBoxFavoritos = popupView.findViewById(R.id.checkBoxFavoritos);

            cargarFiltros();

            checkBoxCamaras.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("camaras", checkBoxCamaras.isChecked());
                editor.apply();
                cargarFiltroCamara();
            });

            checkBoxIncidencias.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("incidencias", checkBoxIncidencias.isChecked());
                editor.apply();
                cargarFiltroIncidencias();
            });

            checkBoxFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("favoritos", checkBoxFavoritos.isChecked());
                editor.apply();
                cargarFiltroFavoritos();

            });

        });

    }

    private void cargarFiltros(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        checkBoxCamaras.setChecked(sharedPreferences.getBoolean("camaras", true));
        checkBoxIncidencias.setChecked(sharedPreferences.getBoolean("incidencias", true));
        checkBoxFavoritos.setChecked(sharedPreferences.getBoolean("favoritos", true));

    }

    private void cargarFiltroCamara(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        boolean camarasChecked = sharedPreferences.getBoolean("camaras", true);
        boolean incidenciasChecked = sharedPreferences.getBoolean("incidencias", true);
        boolean favoritosChecked = sharedPreferences.getBoolean("favoritos", true);

        for (Marker marker : marcadores) {
            if(camarasChecked){
                if(marker.getTag() instanceof Camara){
                    Camara camara = (Camara) marker.getTag();
                    if (favoritosChecked) {
                        marker.setVisible(true);
                    }else{
                        marker.setVisible(!camarasFavoritosBBDD.esFavorito(camara.getCameraId()));
                    }
                }
                if(!incidenciasChecked){
                    if(marker.getTag() instanceof Incidencia) {
                        marker.setVisible(false);
                    }
                }
            } else if (favoritosChecked && !incidenciasChecked) {
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
            } else if (favoritosChecked && marker.getTag() instanceof Camara && incidenciasChecked) {
                marker.setVisible(false);
            }else{
                if (marker.getTag() instanceof Camara){
                    marker.setVisible(false);
                }
            }
        }
    }

    private void cargarFiltroIncidencias(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        boolean camarasChecked = sharedPreferences.getBoolean("camaras", true);
        boolean incidenciasChecked = sharedPreferences.getBoolean("incidencias", true);
        boolean favoritosChecked = sharedPreferences.getBoolean("favoritos", true);

        for (Marker marker : marcadores) {
            if(incidenciasChecked){
                if(marker.getTag() instanceof Incidencia) {
                    Incidencia incidencia = (Incidencia) marker.getTag();
                    if (favoritosChecked) {
                        marker.setVisible(true);
                    } else {
                        marker.setVisible(!incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId()));
                    }
                }
                if(!camarasChecked){
                    if(marker.getTag() instanceof Camara) {
                        marker.setVisible(false);
                    }
                }
            } else if (favoritosChecked && !camarasChecked) {
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
            } else if (favoritosChecked && marker.getTag() instanceof Incidencia && camarasChecked) {
                marker.setVisible(false);
            }else{
                if (marker.getTag() instanceof Incidencia){
                    marker.setVisible(false);
                }
            }
        }
    }

    public void cargarFiltroFavoritos(){
        SharedPreferences sharedPreferences = getSharedPreferences("filtro", MODE_PRIVATE);
        boolean camarasChecked = sharedPreferences.getBoolean("camaras", true);
        boolean incidenciasChecked = sharedPreferences.getBoolean("incidencias", true);
        boolean favoritosChecked = sharedPreferences.getBoolean("favoritos", true);

        for (Marker marker : marcadores) {
            if(favoritosChecked && (incidenciasChecked || camarasChecked)){
                if(marker.getTag() instanceof Incidencia && incidenciasChecked){
                    Incidencia incidencia = (Incidencia) marker.getTag();
                    if (incidenciasChecked && incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())) {
                        marker.setVisible(true);
                    }
                } else if (marker.getTag() instanceof Camara && camarasChecked){
                    Camara camara = (Camara) marker.getTag();
                    if (favoritosChecked && camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                        marker.setVisible(true);
                    }

                }
            }else if(favoritosChecked){
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
    protected void onSaveInstanceState(Bundle outState) {
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
        }else if (errorCarga && carga >= 0){
            carga = -100;
            runOnUiThread(() -> {
                gif_loading.setImageResource(R.drawable.gif_advertencia);
                gif_loading.setVisibility(GifImageView.VISIBLE);
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.activity_main_alert_dialog_errorTitulo)
                    .setMessage(R.string.activity_main_alert_dialog_errorCarga)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();

                gif_loading.setOnClickListener(v -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.activity_main_alert_dialog_errorTitulo)
                            .setMessage(R.string.activity_main_alert_dialog_errorCarga)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                });
            });
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
                    cargarFiltroIncidencias();
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
                                cargarFiltroFavoritos();
                            } else if (tag instanceof Camara) {
                                Camara camara = (Camara) tag;
                                AppConfig.vibrar(MainActivity.this, 100);
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
                cargarFiltroCamara();
                cargarFiltroFavoritos();
                ocultarCarga();
            });

        });
    }

}
