package com.reto.trafikapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.reto.trafikapp.BBDD.CamarasFavoritosBBDD;
import com.reto.trafikapp.model.Camara;

import java.io.Serializable;

public class CameraActionsBottomSheet {
    private final Context context;
    private final CamarasFavoritosBBDD camarasFavoritosBBDD;
    private final Marker marker;

    public CameraActionsBottomSheet(Context context, CamarasFavoritosBBDD camarasFavoritosBBDD, Marker marker) {
        this.context = context;
        this.camarasFavoritosBBDD = camarasFavoritosBBDD;
        this.marker = marker;
    }

    public void ver(Camara camara) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.camara_actions_bottom_sheet, null);

        View layoutAddFavorite = bottomSheetView.findViewById(R.id.layoutAddFavorito);
        View layoutMasInfo = bottomSheetView.findViewById(R.id.layoutMasInfo);
        TextView txtAddFavorite = bottomSheetView.findViewById(R.id.txtAddFavorite);
        ImageView imgAddFavorite = bottomSheetView.findViewById(R.id.imgFav);


        if (camarasFavoritosBBDD.esFavorito(camara.getCameraId())) {
            txtAddFavorite.setText(R.string.camara_actions_bottom_sheet_eliminarFavorito);
            imgAddFavorite.setImageResource(R.drawable.fav_seleccionado);
        }

        layoutAddFavorite.setOnClickListener(v -> {
            camarasFavoritosBBDD.alternarFavorito(camara);
            AppConfig.vibrar(context, 100);
            Toast.makeText(context, R.string.activity_main_toast_favoritoAlterado, Toast.LENGTH_SHORT).show();
            marker.showInfoWindow();
            bottomSheetDialog.dismiss();
        });

        layoutMasInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, CamaraActivity.class);
            intent.putExtra("camara", (Serializable) camara);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
            marker.hideInfoWindow();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}