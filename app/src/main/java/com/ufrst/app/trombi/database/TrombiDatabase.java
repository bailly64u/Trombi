package com.ufrst.app.trombi.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Trombinoscope.class, Groupe.class, Eleve.class, EleveGroupeJoin.class}, exportSchema = false, version = 1)
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
                    .addCallback(roomCallback)                            // Pour ajouter un mock
                    .build();
        }
        return instance;
    }

    private  static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);

            new PopulateDBAsyncTask(instance).execute();
        }
    };

    // Génère des données factices
    private static class PopulateDBAsyncTask extends AsyncTask<Void, Void, Void> {
        private TrombinoscopeDao trombiDao;

        private PopulateDBAsyncTask(TrombiDatabase db){
            trombiDao = db.trombiDao();
        }

        @Override
        protected Void doInBackground(Void... voids){
            trombiDao.insert(new Trombinoscope("Mon trombinoscope", "Mon premier trombinoscope"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 2", "Mon premier trombinoscope 2"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 3", "Mon premier trombinoscope 3"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 4", "Mon premier trombinoscope 4"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope", "Mon premier trombinoscope"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 2", "Mon premier trombinoscope 2"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 3", "Mon premier trombinoscope 3"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 4", "Mon premier trombinoscope 4"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope", "Mon premier trombinoscope"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 2", "Mon premier trombinoscope 2"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 3", "Mon premier trombinoscope 3"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 4", "Mon premier trombinoscope 4"));

            return null;
        }
    }
}
