package com.ufrst.app.trombi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroupeDao {

    @Insert
    void insert(Groupe groupe);

    @Update
    void update(Groupe groupe);

    @Delete
    void delete(Groupe groupe);

    // Récupère tous les trombinoscopes dans l'ordre alphabétique
    @Query("SELECT * FROM table_groupe ORDER BY nom_groupe")
    LiveData<List<Groupe>> getAllGroupes();
}
