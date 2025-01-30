package com.reto.trafikapp.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.reto.trafikapp.BBDD.CamarasFavoritosBBDD;
import com.reto.trafikapp.BBDD.IncidenciasFavoritosBBDD;
import com.reto.trafikapp.R;
import com.reto.trafikapp.model.Camara;
import com.reto.trafikapp.model.Incidencia;

public class MarcadorAdapter implements GoogleMap.InfoWindowAdapter{
    private final View vista;
    private final IncidenciasFavoritosBBDD incidenciasFavoritosBBDD;
    private final CamarasFavoritosBBDD camarasFavoritosBBDD;


    public MarcadorAdapter(LayoutInflater inflater, IncidenciasFavoritosBBDD incidenciasFavoritosBBDD, CamarasFavoritosBBDD camarasFavoritosBBDD) {
        vista = inflater.inflate(R.layout.activity_adaptador_marcador, null);
        this.incidenciasFavoritosBBDD = incidenciasFavoritosBBDD;
        this.camarasFavoritosBBDD = camarasFavoritosBBDD;
    }

    private void renderWindowText(Marker marker) {
        Object tag = marker.getTag();
        TextView tituloText = vista.findViewById(R.id.titulo);
        TextView descripcionText = vista.findViewById(R.id.descripcion);
        ImageView imageView = vista.findViewById(R.id.imgFav);

        if (tag instanceof Incidencia) {
            Incidencia incidencia = (Incidencia) tag;
            tituloText.setText(incidencia.getIncidenceType());
            descripcionText.setText(incidencia.getCause());
            descripcionText.setTypeface(null, Typeface.NORMAL);

            if(incidenciasFavoritosBBDD.esFavorito(incidencia.getIncidenceId())){
                imageView.setImageResource(R.drawable.fav_seleccionado);
            }else{
                imageView.setImageResource(R.drawable.fav_sinseleccionar);
            }
        } else if (tag instanceof Camara) {
            Camara camara = (Camara) tag;
            tituloText.setText(camara.getCameraName());
            descripcionText.setText("Pulsa para ver mas informacion");
            descripcionText.setTypeface(null, android.graphics.Typeface.ITALIC);


            if(camarasFavoritosBBDD.esFavorito(camara.getCameraId())){
                imageView.setImageResource(R.drawable.fav_seleccionado);
            }else{
                imageView.setImageResource(R.drawable.fav_sinseleccionar);
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker);
        return vista;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker);
        return vista;
    }
}