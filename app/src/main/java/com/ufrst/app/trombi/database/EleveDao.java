package com.ufrst.app.trombi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EleveDao {

    @Insert
    void insert(Eleve eleve);

    @Update
    void update(Eleve eleve);

    @Delete
    void delete(Eleve eleve);

    // Récupère tous les trombinoscopes dans l'ordre alphabétique
    @Query("SELECT * FROM table_eleve ORDER BY nom_prenom")
    LiveData<List<Eleve>> getAllEleves();

    // Récupère les élèves d'un trombinoscope
    @Query("SELECT * FROM table_eleve WHERE id_trombi=:idTrombi ORDER BY nom_prenom")
    LiveData<List<Eleve>> getElevesByTrombi(long idTrombi);
}
