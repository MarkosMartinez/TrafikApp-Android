package com.reto.trafikapp.configuration;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.reto.trafikapp.R;

import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {
    private CheckBox checkBoxIncidenciasFavoritas;
    private CheckBox checkBoxIncidenciasNuevas;
    private Spinner spinnerIdioma;
    private Button buttonAplicarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String language = sharedPreferences.getString("idioma", "Español");
        setIdioma(language);

        setContentView(R.layout.activity_config);

        checkBoxIncidenciasFavoritas = findViewById(R.id.checkBoxIncidenciasFavoritas);
        checkBoxIncidenciasNuevas = findViewById(R.id.checkBoxIncidenciasNuevas);
        spinnerIdioma = findViewById(R.id.spinnerIdioma);
        buttonAplicarConfig = findViewById(R.id.buttonAplicarConfig);

        checkBoxIncidenciasFavoritas.setChecked(sharedPreferences.getBoolean("incidenciasFavoritas", false));
        checkBoxIncidenciasNuevas.setChecked(sharedPreferences.getBoolean("incidenciasNuevas", false));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.idiomas_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdioma.setAdapter(adapter);

        String idiomaGuardado = sharedPreferences.getString("idioma", "Español");
        String idiomaNormalizado = idiomaGuardado.toLowerCase(Locale.ROOT);
        int position = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            String idiomaAdaptador = adapter.getItem(i).toString().toLowerCase(Locale.ROOT);
            if (idiomaAdaptador.equals(idiomaNormalizado) || idiomaAdaptador.contains(idiomaNormalizado)) {
                position = i;
                break;
            }
        }
        spinnerIdioma.setSelection(position);

        checkBoxIncidenciasFavoritas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                    }
                }
            }
        });

        checkBoxIncidenciasNuevas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                    }
                }
            }
        });

        buttonAplicarConfig.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("incidenciasFavoritas", checkBoxIncidenciasFavoritas.isChecked());
            editor.putBoolean("incidenciasNuevas", checkBoxIncidenciasNuevas.isChecked());
            editor.putString("idioma", spinnerIdioma.getSelectedItemPosition() == 0 ? "es" : spinnerIdioma.getSelectedItemPosition() == 1 ? "en" : "eu");
            editor.apply();

            String idiomaSeleccionado = spinnerIdioma.getSelectedItem().toString();
            setIdioma(idiomaSeleccionado);
            Toast.makeText(this, R.string.activity_configuracion_toast_actualizado, Toast.LENGTH_SHORT).show();
            recreate();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.activity_configuracion_toast_permisoNotificaciones , Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.activity_configuracion_toast_sinPermisoNotificaciones, Toast.LENGTH_SHORT).show();
                checkBoxIncidenciasFavoritas.setChecked(false);
                checkBoxIncidenciasNuevas.setChecked(false);
            }
        }
    }

    private void setIdioma(String language) {
        Locale locale;
        locale = new Locale(language);
        AppConfig.vibrar(this, 100);

        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

}