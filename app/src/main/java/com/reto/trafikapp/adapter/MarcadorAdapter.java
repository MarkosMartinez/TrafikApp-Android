package com.reto.trafikapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.reto.trafikapp.R;
import com.reto.trafikapp.model.Camara;
import com.reto.trafikapp.model.Incidencia;

public class MarcadorAdapter implements GoogleMap.InfoWindowAdapter{
    private final View vista;

    public MarcadorAdapter(LayoutInflater inflater) {
        vista = inflater.inflate(R.layout.activity_adaptador_marcador, null);
    }

    private void renderWindowText(Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Incidencia) {
            Incidencia incidencia = (Incidencia) tag;

            TextView tituloText = vista.findViewById(R.id.title);
            tituloText.setText(incidencia.getIncidenceType());

            TextView causaText = vista.findViewById(R.id.causa);
            causaText.setText(incidencia.getCause());
        } else if (tag instanceof Camara) {
            Camara camara = (Camara) tag;

            TextView tituloText = vista.findViewById(R.id.title);
            tituloText.setText(camara.getCameraName());

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