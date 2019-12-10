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

    // Utilise des LiveData (observable, pour plus d'infos: https://developer.android.com/topic/libraries/architecture/livedata
    public TrombiRepository(Application application) {
        TrombiDatabase db = TrombiDatabase.getInstance(application);

        // Récupération des DAOs
        trombiDao = db.trombiDao();
        groupeDao = db.groupeDao();
        eleveDao = db.eleveDao();
        joinDao = db.eleveGroupeJoinDao();
    }

    // Trombinoscope________________________________________________________________________________
    public void insert(Trombinoscope trombi) {new InsertTrombiAsyncTask(trombiDao).execute(trombi);}
    public void update(Trombinoscope trombi) {new UpdateTrombiAsyncTask(trombiDao).execute(trombi);}
    public void delete(Trombinoscope trombi) {new DeleteTrombiAsyncTask(trombiDao).execute(trombi);}
    public LiveData<List<Trombinoscope>> getAllTrombis() {return trombiDao.getAllTrombis();}
    public LiveData<List<Groupe>> getAllGroupes() {return groupeDao.getAllGroupes();}
    public LiveData<List<Eleve>> getAllEleves() {return eleveDao.getAllEleves();}


    // Groupe_______________________________________________________________________________________
    public LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi) {return groupeDao.getGroupesByTrombi(idTrombi);}


    // Eleve________________________________________________________________________________________


    // Groupe x Eleve_______________________________________________________________________________
    public LiveData<List<GroupeWithEleves>> getEleveForGroupe() {return joinDao.getElevesForGroupe();}




    // Création de tâches asynchrone pour modifier la BD____________________________________________
    // Trombinoscopes
    private static class InsertTrombiAsyncTask extends AsyncTask<Trombinoscope, Void, Void>{
        private TrombinoscopeDao trombiDao;

        InsertTrombiAsyncTask(TrombinoscopeDao trombiDao){ this.trombiDao = trombiDao; }

        @Override
        protected Void doInBackground(Trombinoscope... trombinoscopes) {
            trombiDao.insert(trombinoscopes[0]);
            return null;
        }
    }

    private static class UpdateTrombiAsyncTask extends AsyncTask<Trombinoscope, Void, Void>{
        private TrombinoscopeDao trombiDao;

        UpdateTrombiAsyncTask(TrombinoscopeDao trombiDao){ this.trombiDao = trombiDao; }

        @Override
        protected Void doInBackground(Trombinoscope... trombinoscopes) {
            trombiDao.update(trombinoscopes[0]);
            return null;
        }
    }

    private static class DeleteTrombiAsyncTask extends AsyncTask<Trombinoscope, Void, Void>{
        private TrombinoscopeDao trombiDao;

        DeleteTrombiAsyncTask(TrombinoscopeDao trombiDao){ this.trombiDao = trombiDao; }

        @Override
        protected Void doInBackground(Trombinoscope... trombinoscopes) {
            trombiDao.delete(trombinoscopes[0]);
            return null;
        }
    }

    /*private static class GetAllTrombisAsyncTask extends AsyncTask<Void, Void, Void>{
        private TrombinoscopeDao trombiDao;

        GetAllTrombisAsyncTask(TrombinoscopeDao trombiDao){ this.trombiDao = trombiDao; }

        @Override
        protected Void doInBackground(Void... voids) {
            trombiDao.getAllTrombis();
            return null;
        }
    }*/
}
