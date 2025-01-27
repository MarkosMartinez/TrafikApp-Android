package com.reto.trafikapp.BBDD;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.reto.trafikapp.model.Camara;
import com.reto.trafikapp.model.Incidencia;

import java.util.ArrayList;
import java.util.List;

public class CamarasFavoritosBBDD extends SQLiteOpenHelper {
    private ArrayList<Incidencia> camaras;
    private static final String BD_NAME = "camaras.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "Camara";

    private static final String ID = "camaraId";
    private static final String NOMBRE = "cameraName";
    private static final String URLIMG = "urlImage";
    private static final String LATITUD = "latitude";
    private static final String LONGITUD = "longitude";

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "(" + ID + " TEXT PRIMARY KEY, "
            + NOMBRE + " TEXT NOT NULL, " + URLIMG + " TEXT NOT NULL,"
            + LATITUD + " DOUBLE NOT NULL," + LONGITUD + " DOUBLE NOT NULL);";

    private final Context context;

    public CamarasFavoritosBBDD(Context context){
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

    public void comprobarCamaras(List<Camara> camarasApi) {
        if (camarasApi == null) {
            camarasApi = new ArrayList<>();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
            boolean existsInApi = false;

            for (Camara camara : camarasApi) {
                if (camara.getCameraId() == id) {
                    existsInApi = true;
                    break;
                }
            }

            if (!existsInApi) {
                Log.d("CamarasFavoritosBBDD", "Borrando camara con la id: " + id);
                db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'");
            }
        }

        cursor.close();
        db.close();
    }

    public void alternarFavorito(Camara camara) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(camara.getCameraId())});

        if (cursor.getCount() == 0) {
            db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ID + ", " + NOMBRE + ", " + URLIMG + ", " + LATITUD + ", " + LONGITUD + ") VALUES ('" + camara.getCameraId() + "', '" + camara.getCameraName() + "', '" + camara.getUrlImage() + "', '" + camara.getLatitude() + "', '" + camara.getLongitude() + "')");
        }else{
            db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + camara.getCameraId() + "'");
        }

        cursor.close();
        db.close();
    }

    public Boolean esFavorito(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        Boolean esFavorito = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return esFavorito;
    }

    public void vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

}
