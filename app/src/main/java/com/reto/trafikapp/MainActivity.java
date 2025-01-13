package com.reto.trafikapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin;
    EditText editTextEmail;
    EditText editTextPass;
    LlamadasAPI llamadasAPI = new LlamadasAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("estaLogueado", false);

        if (isLoggedIn) {
            Log.d("MainActivity", "Usuario logueado");
        } else {
            Log.d("MainActivity", "Usuario no logueado");
            setContentView(R.layout.activity_main);

            buttonLogin = findViewById(R.id.buttonLogin);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPass = findViewById(R.id.editTextPass);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            buttonLogin.setOnClickListener(v -> {
                llamadasAPI.login(editTextEmail.getText().toString(), editTextPass.getText().toString(), new LlamadasAPI.LoginCallback() {
                    @Override
                    public void onSuccess(boolean isSuccess) {
                        runOnUiThread(() -> {
                            if (isSuccess) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("estaLogueado", true);
                                editor.apply();
                                Log.d("MainActivity", "Usuario logueado");
                            } else {
                                Log.d("MainActivity", "Credenciales no validas");
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        runOnUiThread(() -> {
                            Log.d("MainActivity", "Credenciales no validas");
                        });
                    }
                });
            });
        }
    }
}