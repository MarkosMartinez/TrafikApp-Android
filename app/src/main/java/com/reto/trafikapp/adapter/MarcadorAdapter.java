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
    private final View mWindow;

    public MarcadorAdapter(LayoutInflater inflater) {
        mWindow = inflater.inflate(R.layout.activity_adaptador_marcador, null);
    }

    private void renderWindowText(Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Incidencia) {
            Incidencia incidencia = (Incidencia) tag;

            TextView tituloText = mWindow.findViewById(R.id.title);
            tituloText.setText("Incidencia - " + incidencia.getIncidenceType());

            TextView causaText = mWindow.findViewById(R.id.causa);
            causaText.setText(incidencia.getCause());
        } else if (tag instanceof Camara) {
            Camara camara = (Camara) tag;

            TextView tituloText = mWindow.findViewById(R.id.title);
            tituloText.setText("Cámara - " + camara.getCameraName());

        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}