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
                    .addCallback(roomCallback)                              // Pour ajouter un mock
                    .fallbackToDestructiveMigration()                       // A enlever. SI le schema de la BD change, la BD est détruite :'(
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
        private EleveDao eleveDao;
        private GroupeDao groupeDao;
        private EleveGroupeJoinDao joinDao;

        private PopulateDBAsyncTask(TrombiDatabase db){
            trombiDao = db.trombiDao();
            eleveDao = db.eleveDao();
            groupeDao = db.groupeDao();
            joinDao = db.eleveGroupeJoinDao();
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

            Groupe g1 = new Groupe("Mon groupe - TP1");
            Groupe g2 = new Groupe("Mon groupe - TD1");
            Groupe g3 = new Groupe("Mon groupe - TP2");
            Groupe g4 = new Groupe("Mon groupe - TD3");

            groupeDao.insert(g1);
            groupeDao.insert(g2);
            groupeDao.insert(g3);
            groupeDao.insert(g4);

            Eleve e1 = new Eleve("Bailly Louis", "Bruh");
            Eleve e2 = new Eleve("Niclass Maria", "Bruh");
            Eleve e3 = new Eleve("Nom prénom", "Bruh");
            Eleve e4 = new Eleve("Jean Jacques", "Bruh");
            Eleve e5 = new Eleve("Billy the kid", "Bruh");

            eleveDao.insert(e1);
            eleveDao.insert(e2);
            eleveDao.insert(e3);
            eleveDao.insert(e4);
            eleveDao.insert(e5);

            /*joinDao.insert(new EleveGroupeJoin(e1.getIdEleve(), g1.getIdGroupe()));
            joinDao.insert(new EleveGroupeJoin(e2.getIdEleve(), g1.getIdGroupe()));
            joinDao.insert(new EleveGroupeJoin(e2.getIdEleve(), g2.getIdGroupe()));
            joinDao.insert(new EleveGroupeJoin(e3.getIdEleve(), g2.getIdGroupe()));*/

            return null;
        }
    }
}
