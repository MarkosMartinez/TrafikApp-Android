package com.reto.trafikapp.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.reto.trafikapp.LlamadasAPI;
import com.reto.trafikapp.MainActivity;
import com.reto.trafikapp.R;
import com.reto.trafikapp.BBDD.IncidenciasFavoritosBBDD;
import com.reto.trafikapp.model.Incidencia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class IncidenciasWorker extends Worker {
    private static final String CHANNEL_ID = "TRAFIKAPP";
    private final Context context;
    private final IncidenciasFavoritosBBDD incidenciasFavoritosBBDD;
    private final LlamadasAPI llamadasAPI;

    public IncidenciasWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.incidenciasFavoritosBBDD = new IncidenciasFavoritosBBDD(context);
        this.llamadasAPI = new LlamadasAPI();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("IncidenciasWorker", "Comprobando incidencias favoritas eliminadas");
        createNotificationChannel();

        Set<String> favoritosActuales = incidenciasFavoritosBBDD.obtenerFavoritosActuales();
        if (favoritosActuales.isEmpty()) {
            return Result.success();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final Set<String> incidenciasEliminadas = new HashSet<>(favoritosActuales);

        llamadasAPI.getIncidencias(new LlamadasAPI.IncidenciasCallback() {
            @Override
            public void onSuccess(List<Incidencia> incidencias) {
                for (Incidencia incidencia : incidencias) {
                    incidenciasEliminadas.remove(incidencia.getIncidenceId());
                }

                if (!incidenciasEliminadas.isEmpty()) {
                    // Eliminar las incidencias que ya no existen de la base de datos
                    SQLiteDatabase db = incidenciasFavoritosBBDD.getWritableDatabase();
                    for (String idEliminada : incidenciasEliminadas) {
                        db.execSQL("DELETE FROM " + IncidenciasFavoritosBBDD.TABLE_NAME +
                                " WHERE incidenceId = '" + idEliminada + "'");
                    }
                    db.close();

                    // Mostrar notificación
                    showNotification("Incidencias resueltas", context.getResources().getQuantityString(R.plurals.incidencia_resuelta, incidenciasEliminadas.size(), incidenciasEliminadas.size()));
                }

                latch.countDown();
            }

            @Override
            public void onFailure() {
                Log.e("IncidenciasWorker", "Error al obtener incidencias");
                latch.countDown();
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("IncidenciasWorker", "Error en la espera de la llamada API", e);
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "TrafikApp Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para notificaciones de incidencias resueltas");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.marcador_incidencia)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "TrafikApp Notificaciones",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}