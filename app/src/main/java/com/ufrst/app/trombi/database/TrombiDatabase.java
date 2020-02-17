package com.ufrst.app.trombi.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Trombinoscope.class,Groupe.class, Eleve.class, EleveGroupeJoin.class}, exportSchema = false, version = 11)
public abstract class TrombiDatabase extends RoomDatabase {

    private static TrombiDatabase instance;

    // Room s'occupe de ces fonctions automatiquement
    public abstract TrombinoscopeDao trombiDao();
    public abstract GroupeDao groupeDao();
    public abstract EleveDao eleveDao();
    public abstract EleveGroupeJoinDao eleveGroupeJoinDao();

    // Singleton représentant la BD
    static synchronized TrombiDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    TrombiDatabase.class, "trombi_database.db")
                    .addCallback(roomCallback)                              // Pour ajouter un mock
                    .fallbackToDestructiveMigration()                       // A enlever. Si le schema de la BD change, la BD est détruite :'(
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
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
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 5", "Mon premier trombinoscope"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 6", "Mon premier trombinoscope 2"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 7", "Mon premier trombinoscope 3"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 8", "Mon premier trombinoscope 4"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 9", "Mon premier trombinoscope"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 10", "Mon premier trombinoscope 2"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 11", "Mon premier trombinoscope 3"));
            trombiDao.insert(new Trombinoscope("Mon trombinoscope 12", "Mon premier trombinoscope 4"));

            Groupe g1 = new Groupe("Mon groupe - TP1", 1);
            Groupe g2 = new Groupe("Mon groupe - TD1", 1);
            Groupe g3 = new Groupe("Mon groupe - TP2", 1);
            Groupe g4 = new Groupe("Mon groupe - TD3", 1);
            Groupe g5 = new Groupe("Mon groupe - TP3", 1);

            Groupe g6 = new Groupe("Mon groupe - TD1", 2);
            Groupe g7 = new Groupe("Mon groupe - TD2", 2);
            Groupe g8 = new Groupe("Mon groupe - TP1", 2);

            groupeDao.insert(g1);
            groupeDao.insert(g2);
            groupeDao.insert(g3);
            groupeDao.insert(g4);
            groupeDao.insert(g5);
            groupeDao.insert(g6);
            groupeDao.insert(g7);
            groupeDao.insert(g8);

            Eleve e1 = new Eleve("Henry Dupont", 1, "");
            Eleve e2 = new Eleve("Isabelle Henry", 1, "");
            Eleve e3 = new Eleve("Nom prénom", 1, "");
            Eleve e4 = new Eleve("Jean Jacques", 1, "");
            Eleve e5 = new Eleve("Billy the kid", 1, "");
            Eleve e6 = new Eleve("Patrick", 1, "");
            Eleve e7 = new Eleve("Maria Durand", 1, "");

            Eleve e8 = new Eleve("Bailly Louis", 2, "");
            Eleve e9 = new Eleve("Niclass Maria", 2, "");
            Eleve e10 = new Eleve("Nom prénom", 2, "");

            eleveDao.insert(e1);
            eleveDao.insert(e2);
            eleveDao.insert(e3);
            eleveDao.insert(e4);
            eleveDao.insert(e5);
            eleveDao.insert(e6);
            eleveDao.insert(e7);
            eleveDao.insert(e8);
            eleveDao.insert(e9);
            eleveDao.insert(e10);

            joinDao.insert(new EleveGroupeJoin(1, 1, 1));
            joinDao.insert(new EleveGroupeJoin(2, 1, 1));
            joinDao.insert(new EleveGroupeJoin(3, 2, 1));
            joinDao.insert(new EleveGroupeJoin(4, 2, 1));
            joinDao.insert(new EleveGroupeJoin(1, 4, 1));
            joinDao.insert(new EleveGroupeJoin(2, 4, 1));
            joinDao.insert(new EleveGroupeJoin(3, 4, 1));
            joinDao.insert(new EleveGroupeJoin(4, 4, 1));

            joinDao.insert(new EleveGroupeJoin(8, 6, 2));
            joinDao.insert(new EleveGroupeJoin(9, 6, 2));
            joinDao.insert(new EleveGroupeJoin(10, 7, 2));
            joinDao.insert(new EleveGroupeJoin(8, 8, 2));
            joinDao.insert(new EleveGroupeJoin(9, 8, 2));
            joinDao.insert(new EleveGroupeJoin(10, 8, 2));

            return null;
        }
    }
}
