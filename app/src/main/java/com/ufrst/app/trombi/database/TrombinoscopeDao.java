package com.ufrst.app.trombi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrombinoscopeDao {

    @Insert
    void insert(Trombinoscope trombinoscope);

    @Update
    void update(Trombinoscope trombinoscope);

    @Delete
    void delete(Trombinoscope trombinoscope);

    // Récupère tous les trombinoscopes dans l'ordre alphabétique
    @Query("SELECT * FROM table_trombi ORDER BY nomTrombi")
    LiveData<List<Trombinoscope>> getAllTrombis();
}
