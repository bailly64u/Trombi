package com.ufrst.app.trombi.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Trombinoscope.class, Groupe.class, Eleve.class, EleveGroupeJoin.class}, version = 1)
public abstract class TrombiDatabase extends RoomDatabase {

    private static TrombiDatabase instance;

    // Room s'occupe de ces fonctions automatiquement
    public abstract TrombinoscopeDao trombiDao();
    public abstract GroupeDao groupeDao();
    public abstract EleveDao eleveDao();
    public abstract EleveGroupeJoinDao eleveGroupeJoinDao();

    // Singleton
    public static synchronized TrombiDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    TrombiDatabase.class, "trombi_database.db")
                    //.addCallback(roomCallback)                            // Pour ajouter un mock
                    .build();
        }
        return instance;
    }
}
