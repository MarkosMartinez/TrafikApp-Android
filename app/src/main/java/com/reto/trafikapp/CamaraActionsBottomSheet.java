package com.reto.trafikapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.reto.trafikapp.BBDD.CamarasFavoritosBBDD;
import com.reto.trafikapp.model.Camara;

import java.io.Serializable;
import java.util.Objects;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class CamaraActionsBottomSheet {
    private final Context context;
    private final CamarasFavoritosBBDD camarasFavoritosBBDD;
    private final Marker marker;
    private final int widthMarcador = 78;
    private final int widthMarcadorFav = 120;
    private final int heightMarcador = 110;
    private final int heightMarcadorFav = 130;
    private ImageView imageViewCamara;
    private TextView nombreCamara;
    private TextView nombreCarretera;
    private TextView ubicacion;

    public CamaraActionsBottomSheet(Context context, CamarasFavoritosBBDD camarasFavoritosBBDD, Marker marker) {
        this.context = context;
        this.camarasFavoritosBBDD = camarasFavoritosBBDD;
        this.marker = marker;
    }

    public void ver(Camara camara) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.camara_actions_bottom_sheet, null);

        View layoutAddFavorite = bottomSheetView.findViewById(R.id.layoutAddFavorito);
        TextView txtAddFavorite = bottomSheetView.findViewById(R.id.txtAddFavorite);
        ImageView imgAddFavorite = bottomSheetView.findViewById(R.id.imgFav);
        imageViewCamara = bottomSheetView.findViewById(R.id.imageViewCamara);
        nombreCamara = bottomSheetView.findViewById(R.id.textViewCamara);
        nombreCarretera = bottomSheetView.findViewById(R.id.textViewCarretera);
        ubicacion = bottomSheetView.findViewById(R.id.textViewUbicacion);

            Glide.with(context)
                    .load(camara.getUrlImage())
                    .placeholder(R.drawable.gif_cargando)
                    .error(R.drawable.no_camara)
                    .transform(new RoundedCornersTransformation(20, 5))
                    .into(imageViewCamara);

        nombreCamara.setText(!Objects.equals(camara.getCameraName(), "null") ? camara.getCameraName() : context.getString(R.string.activity_camara_noDisponible));
        nombreCarretera.setText(!Objects.equals(camara.getRoad(), "null") ? camara.getRoad() : context.getString(R.string.activity_camara_noDisponible));
        ubicacion.setText(!Objects.equals(camara.getAddress(), "null") ? camara.getAddress() : context.getString(R.string.activity_camara_noDisponible));

        if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
            txtAddFavorite.setText(R.string.camara_actions_bottom_sheet_eliminarFavorito);
            imgAddFavorite.setImageResource(R.drawable.fav_seleccionado);
        }

        layoutAddFavorite.setOnClickListener(v -> {
            camarasFavoritosBBDD.alternarFavorito(camara);
            AppConfig.vibrar(context, 100);
            Toast.makeText(context, R.string.activity_main_toast_favoritoAlterado, Toast.LENGTH_SHORT).show();
            int marcadorCamaraIcono = camarasFavoritosBBDD.esFavorito(camara.getCameraId()) ? R.drawable.marcador_camara_fav : R.drawable.marcador_camara;
            int widthCamaraIncidencia = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? widthMarcadorFav : widthMarcador;
            int heightCamaraIncidencia = marcadorCamaraIcono == R.drawable.marcador_camara_fav ? heightMarcadorFav : heightMarcador;
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), marcadorCamaraIcono), widthCamaraIncidencia, heightCamaraIncidencia, false)));
            marker.showInfoWindow();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}