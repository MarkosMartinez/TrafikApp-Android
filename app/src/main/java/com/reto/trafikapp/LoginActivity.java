package com.reto.trafikapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

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
            Log.d("LoginActivity", "Usuario logueado");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Log.d("LoginActivity", "Usuario no logueado");
            setContentView(R.layout.activity_login);

            buttonLogin = findViewById(R.id.buttonLogin);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPass = findViewById(R.id.editTextPass);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            buttonLogin.setOnClickListener(v -> {
                buttonLogin.setEnabled(false);
                llamadasAPI.login(editTextEmail.getText().toString(), editTextPass.getText().toString(), new LlamadasAPI.LoginCallback() {
                    @Override
                    public void onSuccess(boolean isSuccess) {
                        runOnUiThread(() -> {
                            if (isSuccess) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("estaLogueado", true);
                                editor.apply();
                                Log.d("LoginActivity", "Usuario logueado");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish(); //Para evitar que vuelva a la pantalla del loguin pulsando el boton de atras
                            } else {
                                Log.d("LoginActivity", "Credenciales no validas");
                                Toast.makeText(LoginActivity.this, R.string.activity_login_toast_errorCredenciales, Toast.LENGTH_SHORT).show();
                                buttonLogin.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        runOnUiThread(() -> {
                            Log.d("LoginActivity", "Credenciales no validas");
                            Toast.makeText(LoginActivity.this, R.string.activity_login_toast_errorCredenciales, Toast.LENGTH_SHORT).show();
                            buttonLogin.setEnabled(true);
                        });
                    }
                });
            });
        }
    }
}