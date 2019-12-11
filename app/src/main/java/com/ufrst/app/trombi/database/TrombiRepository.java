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
    public LiveData<Trombinoscope> getTrombiById(long idTrombi) {return trombiDao.getTrombiById(idTrombi);}


    // Groupe_______________________________________________________________________________________
    public void insert(Groupe groupe) {new InsertGroupeAsyncTask(groupeDao).execute(groupe);}
    public void update(Groupe groupe) {new UpdateGroupeAsyncTask(groupeDao).execute(groupe);}
    public void delete(Groupe groupe) {new DeleteGroupeAsyncTask(groupeDao).execute(groupe);}
    public LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi) {return groupeDao.getGroupesByTrombi(idTrombi);}


    // Eleve________________________________________________________________________________________
    public void insert(Eleve eleve) {new InsertEleveAsyncTask(eleveDao).execute(eleve);}
    public void update(Eleve eleve) {new UpdateEleveAsyncTask(eleveDao).execute(eleve);}
    public void delete(Eleve eleve) {new DeleteEleveAsyncTask(eleveDao).execute(eleve);}
    public LiveData<List<Eleve>> getElevesByTrombi(long idTrombi) {return eleveDao.getElevesByTrombi(idTrombi);}


    // Groupe x Eleve_______________________________________________________________________________
    public void insert(EleveGroupeJoin eleveGroupeJoin) {new InsertEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}
    public void update(EleveGroupeJoin eleveGroupeJoin) {new UpdateEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}
    public void delete(EleveGroupeJoin eleveGroupeJoin) {new DeleteEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}
    public LiveData<List<GroupeWithEleves>> getGroupeWithEleves() {return joinDao.getGroupeWithEleves();}
    public LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe) {return joinDao.getGroupeByIdWithEleves(idGroupe);}




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


    // Groupes
    private static class InsertGroupeAsyncTask extends AsyncTask<Groupe, Void, Void>{
        private GroupeDao groupeDao;

        InsertGroupeAsyncTask(GroupeDao groupeDao){ this.groupeDao = groupeDao; }

        @Override
        protected Void doInBackground(Groupe... groupes) {
            groupeDao.insert(groupes[0]);
            return null;
        }
    }

    private static class UpdateGroupeAsyncTask extends AsyncTask<Groupe, Void, Void>{
        private GroupeDao groupeDao;

        UpdateGroupeAsyncTask(GroupeDao groupeDao){ this.groupeDao = groupeDao; }

        @Override
        protected Void doInBackground(Groupe... groupes) {
            groupeDao.update(groupes[0]);
            return null;
        }
    }

    private static class DeleteGroupeAsyncTask extends AsyncTask<Groupe, Void, Void>{
        private GroupeDao groupeDao;

        DeleteGroupeAsyncTask(GroupeDao groupeDao){ this.groupeDao = groupeDao; }

        @Override
        protected Void doInBackground(Groupe... groupes) {
            groupeDao.delete(groupes[0]);
            return null;
        }
    }


    //Eleve
    private static class InsertEleveAsyncTask extends AsyncTask<Eleve, Void, Void>{
        private EleveDao eleveDao;

        InsertEleveAsyncTask(EleveDao eleveDao){ this.eleveDao = eleveDao; }

        @Override
        protected Void doInBackground(Eleve... eleves) {
            eleveDao.insert(eleves[0]);
            return null;
        }
    }

    private static class UpdateEleveAsyncTask extends AsyncTask<Eleve, Void, Void>{
        private EleveDao eleveDao;

        UpdateEleveAsyncTask(EleveDao eleveDao){ this.eleveDao = eleveDao; }

        @Override
        protected Void doInBackground(Eleve... eleves) {
            eleveDao.update(eleves[0]);
            return null;
        }
    }

    private static class DeleteEleveAsyncTask extends AsyncTask<Eleve, Void, Void>{
        private EleveDao eleveDao;

        DeleteEleveAsyncTask(EleveDao eleveDao){ this.eleveDao = eleveDao; }

        @Override
        protected Void doInBackground(Eleve... eleves) {
            eleveDao.delete(eleves[0]);
            return null;
        }
    }


    // EleveGroupeJoin
    private static class InsertEleveXGroupeAsyncTask extends AsyncTask<EleveGroupeJoin, Void, Void>{
        private EleveGroupeJoinDao joinDao;

        InsertEleveXGroupeAsyncTask(EleveGroupeJoinDao joinDao){ this.joinDao = joinDao; }

        @Override
        protected Void doInBackground(EleveGroupeJoin... eleveGroupeJoins) {
            joinDao.delete(eleveGroupeJoins[0]);
            return null;
        }
    }

    private static class UpdateEleveXGroupeAsyncTask extends AsyncTask<EleveGroupeJoin, Void, Void>{
        private EleveGroupeJoinDao joinDao;

        UpdateEleveXGroupeAsyncTask(EleveGroupeJoinDao joinDao){ this.joinDao = joinDao; }

        @Override
        protected Void doInBackground(EleveGroupeJoin... eleveGroupeJoins) {
            joinDao.update(eleveGroupeJoins[0]);
            return null;
        }
    }

    private static class DeleteEleveXGroupeAsyncTask extends AsyncTask<EleveGroupeJoin, Void, Void>{
        private EleveGroupeJoinDao joinDao;

        DeleteEleveXGroupeAsyncTask(EleveGroupeJoinDao joinDao){ this.joinDao = joinDao; }

        @Override
        protected Void doInBackground(EleveGroupeJoin... eleveGroupeJoins) {
            joinDao.delete(eleveGroupeJoins[0]);
            return null;
        }
    }
}
