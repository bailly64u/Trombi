package com.ufrst.app.trombi.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

// Couche supplémentaire entre les données et le ViewModel (Architecture MVVM)
// pour que ce dernier n'ai pas à se préoccuper de la source des données (BD, éventuellement Internet...)
public class TrombiRepository {

    // DAOs
    private TrombinoscopeDao trombiDao;
    private GroupeDao groupeDao;
    private EleveDao eleveDao;
    private EleveGroupeJoinDao joinDao;

    // Utilise des LiveData (observable, pour plus d'infos: https://developer.android.com/topic/libraries/architecture/livedata
    public TrombiRepository(Application application){
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
    void insert(Trombinoscope trombi){
        execute(() -> trombiDao.insert(trombi));
    }

    void update(Trombinoscope trombi){
        execute(() -> trombiDao.update(trombi));
    }

    void delete(Trombinoscope trombi){
        execute(() -> trombiDao.delete(trombi));
    }

    LiveData<List<Trombinoscope>> getAllTrombis(){
        return trombiDao.getAllTrombis();
    }

    LiveData<Trombinoscope> getTrombiById(long idTrombi){
        return trombiDao.getTrombiById(idTrombi);
    }

    void softDeleteTrombi(long idTrombi){
        execute(() -> trombiDao.softDeleteTrombi(idTrombi));
    }

    void deleteSoftDeletedTrombis(){
        execute(() -> trombiDao.deleteSoftDeletedTrombis());
    }

    // trombiDao.insert() retourne un long (qui correspond à l'id du trombinoscope inséré).
    // Mais il est ignoré dans la méthode insert de cette classe (pour des raisons de performances)
    // Cette méthode permet de récupérer l'ID de l'élément inséré sur le champ.
    long insertAndRetrieveId(Trombinoscope trombi){
        return trombiDao.insert(trombi);
    }


    //______________________________________________________________________________________________
    // Groupe_______________________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(Groupe groupe){
        execute(() -> groupeDao.insert(groupe));
    }

    void update(Groupe groupe){
        execute(() -> groupeDao.update(groupe));
    }

    void delete(Groupe groupe){
        execute(() -> groupeDao.delete(groupe));
    }

    LiveData<List<Groupe>> getAllGroupes(){
        return groupeDao.getAllGroupes();
    }

    LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi){
        return groupeDao.getGroupesByTrombi(idTrombi);
    }

    void deleteGroupesForTrombi(long idTrombi){
        execute(() -> groupeDao.deleteGroupesForTrombi(idTrombi));
    }

    void softDeleteGroupe(long idGroupe){
        execute(() -> groupeDao.softDeleteGroupe(idGroupe));
    }

    void softDeleteGroupesForTrombi(long idTrombi, int isDeleted){
        execute(() -> groupeDao.softDeleteGroupesForTrombi(idTrombi, isDeleted));
    }

    void deleteSoftDeletedGroupes(){
        execute(() -> groupeDao.deleteSoftDeletedGroupes());
    }

    long insertAndRetrieveId(Groupe groupe){
        return groupeDao.insert(groupe);
    }


    //______________________________________________________________________________________________
    // Eleve________________________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(Eleve eleve){
        execute(() -> eleveDao.insert(eleve));
    }

    void update(Eleve eleve){
        execute(() -> eleveDao.update(eleve));
    }

    void delete(Eleve eleve){
        execute(() -> eleveDao.delete(eleve));
    }

    LiveData<List<Eleve>> getAllEleves(){
        return eleveDao.getAllEleves();
    }

    LiveData<List<Eleve>> getElevesByTrombi(long idTrombi){
        return eleveDao.getElevesByTrombi(idTrombi);
    }

    LiveData<Eleve> getEleveById(long idEleve){
        return eleveDao.getEleveById(idEleve);
    }

    void deleteElevesForTrombi(long idTrombi){
        execute(() -> eleveDao.deleteElevesForTrombi(idTrombi));
    }

    void softDeleteEleve(long idEleve){
        execute(() -> eleveDao.softDeleteEleve(idEleve));
    }

    void softDeleteElevesForTrombi(long idTrombi, int isDeleted){
        execute(() -> eleveDao.softDeleteElevesForTrombi(idTrombi, isDeleted));
    }

    void deleteSoftDeletedEleves(){
        execute(() -> eleveDao.deleteSoftDeletedEleves());
    }

    int getElevesNumberByTrombi(long idTrombi){
        return eleveDao.getElevesNumberByTrombi(idTrombi);
    }

    // Voir TrombiRepository#insertAndRetrieveId(Trombinoscope trombi)
    long insertAndRetrieveId(Eleve eleve){
        return eleveDao.insert(eleve);
    }


    //______________________________________________________________________________________________
    // Groupe x Eleve_______________________________________________________________________________
    //______________________________________________________________________________________________
    void insert(EleveGroupeJoin eleveGroupeJoin){
        execute(() -> joinDao.insert(eleveGroupeJoin));
    }

    void update(EleveGroupeJoin eleveGroupeJoin){
        execute(() -> joinDao.update(eleveGroupeJoin));
    }

    void delete(EleveGroupeJoin eleveGroupeJoin){
        execute(() -> joinDao.delete(eleveGroupeJoin));
    }

    LiveData<List<GroupeWithEleves>> getGroupesWithEleves(){
        return joinDao.getGroupesWithEleves();
    }

    LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe){
        return joinDao.getGroupeByIdWithEleves(idGroupe);
    }

    LiveData<EleveWithGroups> getEleveByIdWithGroups(long idEleve){
        return joinDao.getEleveByIdWithGroups(idEleve);
    }

    // Pas de LiveData, ne pas exécuter sur le ThreadUI
    EleveWithGroups getEleveByIdWithGroupsNotLive(long idEleve){
        return joinDao.getEleveByIdWithGroupsNotLive(idEleve);
    }

    List<EleveWithGroups> getEleveWithGroupsByTrombiNotLive(long idTrombi){
        return joinDao.getEleveWithGroupsByTrombiNotLive(idTrombi);
    }

    // Soft delete et suppressions
    void softDeleteXRefsByGroupe(long idGroupe, int isDeleted){
        execute(() -> joinDao.softDeleteXRefsByGroupe(idGroupe, isDeleted));
    }

    void softDeleteXRefsByTrombi(long idTrombi, int isDeleted){
        execute(() -> joinDao.softDeleteXRefsByTrombi(idTrombi, isDeleted));
    }

    void softDeleteXRefsByEleve(long idEleve, int isDeleted){
        execute(() -> joinDao.softDeleteXRefsByEleve(idEleve, isDeleted));
    }

    void deleteSoftDeletedXRefs(){
        execute(() -> joinDao.deleteSoftDeletedXRefs());
    }

    private void execute(Runnable runnable){
        Executors.newSingleThreadExecutor().execute(runnable);
    }
    
}