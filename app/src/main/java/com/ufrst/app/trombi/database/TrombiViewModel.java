package com.ufrst.app.trombi.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TrombiViewModel extends AndroidViewModel {

    private TrombiRepository repository;

    public TrombiViewModel(@NonNull Application application){
        super(application);

        repository = new TrombiRepository(application);
    }


    // Trombinoscope________________________________________________________________________________
    public void insert(Trombinoscope trombi){repository.insert(trombi);}
    public void update(Trombinoscope trombi){repository.update(trombi);}
    public void delete(Trombinoscope trombi){repository.delete(trombi);}
    public LiveData<List<Trombinoscope>> getAllTrombis(){return repository.getAllTrombis();}
    public LiveData<Trombinoscope> getTrombiById(long idTrombi){return repository.getTrombiById(idTrombi);}


    // Groupe_______________________________________________________________________________________
    public void insert(Groupe groupe){repository.insert(groupe);}
    public void update(Groupe groupe){repository.update(groupe);}
    public void delete(Groupe groupe){repository.delete(groupe);}
    public LiveData<List<Groupe>> getAllGroupes(){return repository.getAllGroupes();}
    public LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi){return repository.getGroupesByTrombi(idTrombi);}


    // Eleve________________________________________________________________________________________
    public void insert(Eleve eleve){repository.insert(eleve);}
    public void update(Eleve eleve){repository.update(eleve);}
    public void delete(Eleve eleve){repository.delete(eleve);}
    public LiveData<List<Eleve>> getAllEleves(){return repository.getAllEleves();}
    public LiveData<List<Eleve>> getElevesByTrombi(long idTrombi){return repository.getElevesByTrombi(idTrombi);}


    // Eleve Groupe Join____________________________________________________________________________
    public void insert(EleveGroupeJoin eleveGroupeJoin){repository.insert(eleveGroupeJoin);}
    public void update(EleveGroupeJoin eleveGroupeJoin){repository.update(eleveGroupeJoin);}
    public void delete(EleveGroupeJoin eleveGroupeJoin){repository.delete(eleveGroupeJoin);}
    public LiveData<List<GroupeWithEleves>> getGroupeWithEleves(){return repository.getGroupeWithEleves();}
    public LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe) {return repository.getGroupeByIdWithEleves(idGroupe);}
}
