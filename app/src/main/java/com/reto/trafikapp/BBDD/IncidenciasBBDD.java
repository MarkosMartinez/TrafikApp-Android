package com.reto.trafikapp.BBDD;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.reto.trafikapp.model.Incidencia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncidenciasBBDD extends SQLiteOpenHelper {

    private static final String BD_NAME = "incidencias.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "Incidencias";

    private static final String ID = "incidenceId";

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME + "(" + ID + " TEXT PRIMARY KEY);";

    private final Context context;

    public IncidenciasBBDD(Context context){
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


    public Set<String> obtenerIncidencias() {
        Set<String> incidencias = new HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT incidenceId FROM " + IncidenciasBBDD.TABLE_NAME, null);

        while (cursor.moveToNext()) {
            incidencias.add(cursor.getString(0));
        }

        cursor.close();
        db.close();
        return incidencias;
    }

    public void vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
}