package com.ufrst.app.trombi.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TrombiViewModel extends AndroidViewModel {

    private TrombiRepository repository;
    private LiveData<List<Trombinoscope>> allTrombis;

    public TrombiViewModel(@NonNull Application application){
        super(application);

        repository = new TrombiRepository(application);
        allTrombis = repository.getAllTrombis();
    }

    public void insert(Trombinoscope trombi){ repository.insert(trombi); }
    public void update(Trombinoscope trombi){ repository.update(trombi); }
    public void delete(Trombinoscope trombi){ repository.delete(trombi); }
    public LiveData<List<Trombinoscope>> getAllTrombis(){ return allTrombis; }
}
