package com.ufrst.app.trombi.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

// Couche supplémentaire entre les données et le ViewModel (Architecture MVVM)
// pour que ce dernier n'ai pas à se préoccuper de la source des données (BD, éventuellement Internet...)
public class TrombiRepository {

    // DAOs
    private TrombinoscopeDao trombiDao;
    private GroupeDao groupeDao;
    private EleveDao eleveDao;
    private EleveGroupeJoinDao joinDao;

    // LiveData (observable, pour plus d'infos: https://developer.android.com/topic/libraries/architecture/livedata
    private LiveData<List<Trombinoscope>> allTrombis;

    public TrombiRepository(Application application) {
        TrombiDatabase db = TrombiDatabase.getInstance(application);
        trombiDao = db.trombiDao();
        groupeDao = db.groupeDao();
        eleveDao = db.eleveDao();
        joinDao = db.eleveGroupeJoinDao();
    }

    // Trombinoscope________________________________________________________________________________
    public void insert(Trombinoscope trombi) {new InsertTrombiAsyncTask(trombiDao).execute(trombi);}




    // Création de tâches asynchrone pour modifier la BD____________________________________________
    // Trombinoscopes
    private static class InsertTrombiAsyncTask extends AsyncTask<Trombinoscope, Void, Void>{
        private TrombinoscopeDao trombiDao;

        InsertTrombiAsyncTask(TrombinoscopeDao trombiDao){ this.trombiDao = trombiDao; }

        @Override
        protected Void doInBackground(Trombinoscope... trombinoscopes) {
            trombiDao.update(trombinoscopes[0]);
            return null;
        }
    }
}
