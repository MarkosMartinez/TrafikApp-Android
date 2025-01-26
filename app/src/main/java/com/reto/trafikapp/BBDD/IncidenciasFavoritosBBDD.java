package com.reto.trafikapp.BBDD;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.reto.trafikapp.model.Incidencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IncidenciasFavoritosBBDD extends SQLiteOpenHelper {

    private ArrayList<Incidencia> incidencias;
    private static final String BD_NAME = "incidencias.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "Incidencia";

    private static final String ID = "incidenceId";
    private static final String TIPO = "incidenceType";
    private static final String CAUSA = "cause";
    private static final String LATITUD = "latitude";
    private static final String LONGITUD = "longitude";

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "(" + ID + " TEXT PRIMARY KEY, "
            + TIPO + " TEXT NOT NULL, " + CAUSA + " TEXT NOT NULL,"
            + LATITUD + " DOUBLE NOT NULL," + LONGITUD + " DOUBLE NOT NULL);";

    private final Context context;

    public IncidenciasFavoritosBBDD(Context context){
        super(context, BD_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void comprobarIncidencias(List<Incidencia> incidenciasApi) {
        if (incidenciasApi == null) {
            incidenciasApi = new ArrayList<>();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ID));
            boolean existsInApi = false;

            for (Incidencia incidencia : incidenciasApi) {
                if (Objects.equals(incidencia.getIncidenceId(), id)) {
                    existsInApi = true;
                    break;
                }
            }

            if (!existsInApi) {
                Log.d("IncidenciasFavoritosBBDD", "Borrando incidencia con id: " + id);
                db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'");
            }
        }

        cursor.close();
        db.close();
    }

    public void alternarFavorito(Incidencia incidencia) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(incidencia.getIncidenceId())});

        if (cursor.getCount() == 0) {
            db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ID + ", " + TIPO + ", " + CAUSA + ", " + LATITUD + ", " + LONGITUD + ") VALUES ('" + incidencia.getIncidenceId() + "', '" + incidencia.getIncidenceType() + "', '" + incidencia.getCause() + "', '" + incidencia.getLatitude() + "', '" + incidencia.getLongitude() + "')");
        }else{
            db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + incidencia.getIncidenceId() + "'");
        }

        cursor.close();
        db.close();
    }

    public Boolean esFavorito(String id){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'";
            cursor = db.rawQuery(query, null);
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    public void vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
}
