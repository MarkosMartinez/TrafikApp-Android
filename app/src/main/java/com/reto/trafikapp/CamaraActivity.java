package com.reto.trafikapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.reto.trafikapp.model.Camara;

import java.io.Serializable;
import com.bumptech.glide.Glide;

public class CamaraActivity extends AppCompatActivity implements Serializable {
    ImageView imgCamara;
    TextView nombreCamara;
    TextView nombreCarretera;
    TextView ubicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        imgCamara = findViewById(R.id.imgCamara);
        nombreCamara = findViewById(R.id.textViewCamara);
        nombreCarretera = findViewById(R.id.textViewCarretera);
        ubicacion = findViewById(R.id.textViewUbicacion);

        Camara camara = (Camara) getIntent().getSerializableExtra("camara");
        if (camara != null) {
            Glide.with(this)
                    .load(camara.getUrlImage())
                    .into(imgCamara);
            nombreCamara.setText(camara.getCameraName());
            nombreCarretera.setText(camara.getRoad());
            ubicacion.setText(camara.getAddress());

        }


    }
}
