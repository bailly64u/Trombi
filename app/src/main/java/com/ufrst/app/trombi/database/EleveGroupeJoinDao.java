package com.ufrst.app.trombi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

// Pour les requêtes nécessitant une jointure
@Dao
public interface EleveGroupeJoinDao {

    @Insert
    void insert(EleveGroupeJoin eleveGroupeJoin);

    @Update
    void update(EleveGroupeJoin eleveGroupeJoin);

    @Delete
    void delete(EleveGroupeJoin eleveGroupeJoin);

    // Retourne une liste d'objet contenant un Groupe avec sa liste d'Eleves
    @Transaction                                        // Comme Room utilisera deux requêtes pour nous dans les coulisses, on annote une Transaction pour s'assurer que cela se passe atomiquement
    @Query("SELECT * FROM table_groupe")
    LiveData<List<GroupeWithEleves>> getElevesForGroupe();

}
