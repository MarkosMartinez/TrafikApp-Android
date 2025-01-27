package com.reto.trafikapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.reto.trafikapp.BBDD.CamarasFavoritosBBDD;
import com.reto.trafikapp.model.Camara;

import java.io.Serializable;
import java.util.Objects;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class CamaraActivity extends AppCompatActivity implements Serializable {
    ImageView imgCamara;
    TextView nombreCamara;
    TextView nombreCarretera;
    TextView ubicacion;
    FloatingActionButton floatingActionButtonFav;
    CamarasFavoritosBBDD camarasFavoritosBBDD = new CamarasFavoritosBBDD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        imgCamara = findViewById(R.id.imgCamara);
        nombreCamara = findViewById(R.id.textViewCamara);
        nombreCarretera = findViewById(R.id.textViewCarretera);
        ubicacion = findViewById(R.id.textViewUbicacion);
        floatingActionButtonFav = findViewById(R.id.floatingActionButtonFav);

        Camara camara = (Camara) getIntent().getSerializableExtra("camara");
        if (camara != null) {
            Glide.with(this)
                    .load(camara.getUrlImage())
                    .placeholder(R.drawable.gif_cargando)
                    .error(R.drawable.no_camara)
                    .transform(new RoundedCornersTransformation(20, 5))
                    .into(imgCamara);
            nombreCamara.setText(!Objects.equals(camara.getCameraName(), "null") ? camara.getCameraName() : getString(R.string.activity_camara_noDisponible));
            nombreCarretera.setText(!Objects.equals(camara.getRoad(), "null") ? camara.getRoad() : getString(R.string.activity_camara_noDisponible));
            ubicacion.setText(!Objects.equals(camara.getAddress(), "null") ? camara.getAddress() : getString(R.string.activity_camara_noDisponible));

            if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                floatingActionButtonFav.setImageResource(R.drawable.fav_seleccionado);
            }else{
                floatingActionButtonFav.setImageResource(R.drawable.fav_sinseleccionar);
            }

            floatingActionButtonFav.setOnClickListener(v -> {
                camarasFavoritosBBDD.alternarFavorito(camara);
                AppConfig.vibrar(CamaraActivity.this, 100);
                if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
                    floatingActionButtonFav.setImageResource(R.drawable.fav_seleccionado);
                }else{
                    floatingActionButtonFav.setImageResource(R.drawable.fav_sinseleccionar);
                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("camaraId", getIntent().getSerializableExtra("camara"));
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}
