package com.reto.trafikapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.reto.trafikapp.R;

public class FiltroAdapter extends RecyclerView.Adapter<FiltroAdapter.FiltroViewHolder> {

    @NonNull
    @Override
    public FiltroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_adaptador_filtro, parent, false);
        return new FiltroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FiltroViewHolder holder, int position) {
        // Aquí puedes configurar los CheckBox según sea necesario
    }

    @Override
    public int getItemCount() {
        return 1; // Ajusta esto según tus necesidades
    }

    static class FiltroViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxCamara;
        CheckBox checkBoxIncidencias;
        CheckBox checkBoxFavoritos;

        FiltroViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCamara = itemView.findViewById(R.id.checkbox_camara);
            checkBoxIncidencias = itemView.findViewById(R.id.checkbox_incidencias);
            checkBoxFavoritos = itemView.findViewById(R.id.checkbox_favoritos);
        }
    }
}