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


    // Groupe_______________________________________________________________________________________
    public LiveData<List<Groupe>> getAllGroupes(){return repository.getAllGroupes();}
    public LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi){ return repository.getGroupesByTrombi(idTrombi);}


    // Eleve________________________________________________________________________________________
    public LiveData<List<Eleve>> getAllEleves(){return repository.getAllEleves();}


    // Eleve Groupe Join____________________________________________________________________________
    public LiveData<List<GroupeWithEleves>> getEleveForGroupe(){return repository.getEleveForGroupe();}
}
