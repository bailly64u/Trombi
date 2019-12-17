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
    void insert(Trombinoscope trombi) {new InsertTrombiAsyncTask(trombiDao).execute(trombi);}
    void update(Trombinoscope trombi) {new UpdateTrombiAsyncTask(trombiDao).execute(trombi);}
    void delete(Trombinoscope trombi) {new DeleteTrombiAsyncTask(trombiDao).execute(trombi);}

    LiveData<List<Trombinoscope>> getAllTrombis() {return trombiDao.getAllTrombis();}
    LiveData<Trombinoscope> getTrombiById(long idTrombi) {return trombiDao.getTrombiById(idTrombi);}
    void softDeleteTrombi(long idTrombi) {}


    // Groupe_______________________________________________________________________________________
    void insert(Groupe groupe) {new InsertGroupeAsyncTask(groupeDao).execute(groupe);}
    void update(Groupe groupe) {new UpdateGroupeAsyncTask(groupeDao).execute(groupe);}
    void delete(Groupe groupe) {new DeleteGroupeAsyncTask(groupeDao).execute(groupe);}

    LiveData<List<Groupe>> getAllGroupes() {return groupeDao.getAllGroupes();}
    LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi) {return groupeDao.getGroupesByTrombi(idTrombi);}
    void deleteGroupesForTrombi(long idTrombi){new DeleteGroupesForTrombiAsyncTask(groupeDao).execute(idTrombi);}


    // Eleve________________________________________________________________________________________
    void insert(Eleve eleve) {new InsertEleveAsyncTask(eleveDao).execute(eleve);}
    void update(Eleve eleve) {new UpdateEleveAsyncTask(eleveDao).execute(eleve);}
    void delete(Eleve eleve) {new DeleteEleveAsyncTask(eleveDao).execute(eleve);}

    LiveData<List<Eleve>> getAllEleves() {return eleveDao.getAllEleves();}
    LiveData<List<Eleve>> getElevesByTrombi(long idTrombi) {return eleveDao.getElevesByTrombi(idTrombi);}
    void deleteElevesForTrombi(long idTrombi){new DeleteElevesForTrombiAsyncTask(eleveDao).execute(idTrombi);}


    // Groupe x Eleve_______________________________________________________________________________
    void insert(EleveGroupeJoin eleveGroupeJoin) {new InsertEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}
    void update(EleveGroupeJoin eleveGroupeJoin) {new UpdateEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}
    void delete(EleveGroupeJoin eleveGroupeJoin) {new DeleteEleveXGroupeAsyncTask(joinDao).execute(eleveGroupeJoin);}

    LiveData<List<GroupeWithEleves>> getGroupesWithEleves() {return joinDao.getGroupesWithEleves();}
    LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe) {return joinDao.getGroupeByIdWithEleves(idGroupe);}




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

    private static class DeleteGroupesForTrombiAsyncTask extends AsyncTask<Long, Void, Void>{
        private GroupeDao groupeDao;

        DeleteGroupesForTrombiAsyncTask(GroupeDao groupeDao){ this.groupeDao = groupeDao; }

        @Override
        protected Void doInBackground(Long... longs) {
            groupeDao.deleteGroupesForTrombi(longs[0]);
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

    private static class DeleteElevesForTrombiAsyncTask extends AsyncTask<Long, Void, Void>{
        private EleveDao eleveDao;

        DeleteElevesForTrombiAsyncTask(EleveDao eleveDao){ this.eleveDao = eleveDao; }

        @Override
        protected Void doInBackground(Long... longs) {
            eleveDao.deleteElevesForTrombi(longs[0]);
            return null;
        }
    }

    // EleveGroupeJoin
    private static class InsertEleveXGroupeAsyncTask extends AsyncTask<EleveGroupeJoin, Void, Void>{
        private EleveGroupeJoinDao joinDao;

        InsertEleveXGroupeAsyncTask(EleveGroupeJoinDao joinDao){ this.joinDao = joinDao; }

        @Override
        protected Void doInBackground(EleveGroupeJoin... eleveGroupeJoins) {
            joinDao.insert(eleveGroupeJoins[0]);
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
