package com.reto.trafikapp.BBDD;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.reto.trafikapp.model.Camara;

import java.util.ArrayList;
import java.util.List;

public class CamarasFavoritosBBDD extends SQLiteOpenHelper {
    private static final String BD_NAME = "camaras_fav.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "CamarasFav";

    private static final String ID = "camaraId";

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "(" + ID + " TEXT PRIMARY KEY);";

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

    //Para comprobar si las camaras favoritas siguen existiendo en la API
    //Si no existen en la API, se eliminan de la BBDD
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

    //Para alternar el favorito de una camara
    public void alternarFavorito(Camara camara) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(camara.getCameraId())});

        if (cursor.getCount() == 0) {
            db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ID + ") VALUES ('" + camara.getCameraId() + "')");
        }else{
            db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + camara.getCameraId() + "'");
        }

        cursor.close();
        db.close();
    }

    //Para comprobar si una camara es favorita
    public Boolean esFavorito(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        Boolean esFavorito = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return esFavorito;
    }

    //Para vaciar la tabla de camaras favoritas
    public void vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

}
