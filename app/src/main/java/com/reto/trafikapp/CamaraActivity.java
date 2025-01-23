package com.reto.trafikapp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.reto.trafikapp.model.Camara;

import java.io.Serializable;
import com.bumptech.glide.Glide;

public class CamaraActivity extends AppCompatActivity implements Serializable {
    ImageView imgCamara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        imgCamara = findViewById(R.id.imgCamara);
        Camara camara = (Camara) getIntent().getSerializableExtra("camara");
        if (camara != null) {
            Glide.with(this)
                    .load(camara.getUrlImage())
                    .into(imgCamara);
        }


    }
}
