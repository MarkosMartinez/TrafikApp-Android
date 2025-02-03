package com.reto.trafikapp.configuracion;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        String savedLanguage = sharedPreferences.getString("idioma", "Español");
        int position = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(savedLanguage)) {
                position = i;
                break;
            }
        }
        spinnerIdioma.setSelection(position);

        buttonAplicarConfig.setOnClickListener(v -> {
            guardarCambios();
            String selectedLanguage = spinnerIdioma.getSelectedItem().toString();
            setIdioma(selectedLanguage);
            Toast.makeText(this, R.string.activity_configuracion_toast_actualizado, Toast.LENGTH_SHORT).show();
            recreate();
        });

    }

    private void guardarCambios() {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("incidenciasFavoritas", checkBoxIncidenciasFavoritas.isChecked());
        editor.putBoolean("incidenciasNuevas", checkBoxIncidenciasNuevas.isChecked());
        editor.putString("idioma", spinnerIdioma.getSelectedItem().toString());
        editor.apply();
    }

    private void setIdioma(String language) {
        Locale locale;
        switch (language) {
            case "English":
            case "Inglés":
            case "Ingelesa":
                locale = new Locale("en");
                break;
            case "Basque":
            case "Vasco":
            case "Euskera":
                locale = new Locale("eu");
                break;
            case "Spanish":
            case "Español":
            case "Gaztelania":
                locale = new Locale("es");
                break;
            default:
                locale = new Locale("es");
                break;
        }
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
        getOnBackPressedDispatcher().onBackPressed();
    }

}