package com.ufrst.app.trombi.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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


    //______________________________________________________________________________________________
    // Trombinoscope________________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(Trombinoscope trombi){Executors.newSingleThreadExecutor().execute(() -> trombiDao.insert(trombi));}
    void update(Trombinoscope trombi){Executors.newSingleThreadExecutor().execute(() -> trombiDao.update(trombi));}
    void delete(Trombinoscope trombi){Executors.newSingleThreadExecutor().execute(() -> trombiDao.delete(trombi));}

    LiveData<List<Trombinoscope>> getAllTrombis(){return trombiDao.getAllTrombis();}
    LiveData<Trombinoscope> getTrombiById(long idTrombi){return trombiDao.getTrombiById(idTrombi);}
    void softDeleteTrombi(long idTrombi){Executors.newSingleThreadExecutor().execute(() -> trombiDao.softDeleteTrombi(idTrombi));}
    void deleteSoftDeletedTrombis(){Executors.newSingleThreadExecutor().execute(() -> trombiDao.deleteSoftDeletedTrombis());}

    // trombiDao.insert() retourne un long (qui correspond à l'id du trombinoscope inséré).
    // Mais il est ignoré dans la méthode insert de cette classe (pour des raisons de performances)
    // Cette méthode permet de récupérer l'ID de l'élément inséré sur le champ.
    long insertAndRetrieveId(Trombinoscope trombi){
        Callable<Long> callable = () -> trombiDao.insert(trombi);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(callable);
        long id = 0;

        try {
            id = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return id;
    }


    //______________________________________________________________________________________________
    // Groupe_______________________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(Groupe groupe){Executors.newSingleThreadExecutor().execute(() -> groupeDao.insert(groupe));}
    void update(Groupe groupe){Executors.newSingleThreadExecutor().execute(() -> groupeDao.update(groupe));}
    void delete(Groupe groupe){Executors.newSingleThreadExecutor().execute(() -> groupeDao.delete(groupe));}

    LiveData<List<Groupe>> getAllGroupes(){return groupeDao.getAllGroupes();}
    LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi){return groupeDao.getGroupesByTrombi(idTrombi);}
    void deleteGroupesForTrombi(long idTrombi){Executors.newSingleThreadExecutor().execute(() -> groupeDao.deleteGroupesForTrombi(idTrombi));}


    //______________________________________________________________________________________________
    // Eleve________________________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(Eleve eleve){Executors.newSingleThreadExecutor().execute(() -> eleveDao.insert(eleve));}
    void update(Eleve eleve){Executors.newSingleThreadExecutor().execute(() -> eleveDao.update(eleve));}
    void delete(Eleve eleve){Executors.newSingleThreadExecutor().execute(() -> eleveDao.delete(eleve));}

    LiveData<List<Eleve>> getAllEleves(){return eleveDao.getAllEleves();}
    LiveData<List<Eleve>> getElevesByTrombi(long idTrombi) {return eleveDao.getElevesByTrombi(idTrombi);}
    void deleteElevesForTrombi(long idTrombi){Executors.newSingleThreadExecutor().execute(() -> eleveDao.deleteElevesForTrombi(idTrombi));}
    void softDeleteEleve(long idEleve){Executors.newSingleThreadExecutor().execute(() -> eleveDao.softDeleteEleve(idEleve));}
    void softDeleteElevesForTrombi(long idTrombi){Executors.newSingleThreadExecutor().execute(() -> eleveDao.softDeleteElevesForTrombi(idTrombi));}
    void deleteSoftDeletedEleves(){Executors.newSingleThreadExecutor().execute(() -> eleveDao.deleteSoftDeletedEleves());}

    int getElevesNumberByTrombi(long idTrombi){return eleveDao.getElevesNumberByTrombi(idTrombi);}

    // Voir TrombiRepository#insertAndRetrieveId(Trombinoscope trombi)
    long insertAndRetrieveId(Eleve eleve){
        Callable<Long> callable = () -> eleveDao.insert(eleve);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(callable);
        long id = 0;

        try {
            id = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return id;
    }


    //______________________________________________________________________________________________
    // Groupe x Eleve_______________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(EleveGroupeJoin eleveGroupeJoin){Executors.newSingleThreadExecutor().execute(() -> joinDao.insert(eleveGroupeJoin));}
    void update(EleveGroupeJoin eleveGroupeJoin){Executors.newSingleThreadExecutor().execute(() -> joinDao.update(eleveGroupeJoin));}
    void delete(EleveGroupeJoin eleveGroupeJoin){Executors.newSingleThreadExecutor().execute(() -> joinDao.delete(eleveGroupeJoin));}

    LiveData<List<GroupeWithEleves>> getGroupesWithEleves(){return joinDao.getGroupesWithEleves();}
    LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe){return joinDao.getGroupeByIdWithEleves(idGroupe);}
    LiveData<EleveWithGroups> getEleveByIdWithGroups(long idEleve){return joinDao.getEleveByIdWithGroups(idEleve);}



    //A changer : supprimer, remplacement par Executors.newSingleThreadExecutor()
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
