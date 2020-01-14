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

    // Récupère tous les Trombinoscopes dans l'ordre alphabétique
    @Query("SELECT * FROM table_trombi WHERE is_deleted = 0 ORDER BY nom_trombi")
    LiveData<List<Trombinoscope>> getAllTrombis();

    // Récupère un trombinoscope selon un id
    @Query("SELECT * FROM table_trombi WHERE id_trombi=:idTrombi")
    LiveData<Trombinoscope> getTrombiById(long idTrombi);

    // Soft delete - Inverse la valeur du booleen isDeleted du Trombi
    @Query("UPDATE table_trombi SET is_deleted = NOT is_deleted WHERE id_trombi=:idTrombi")
    void softDeleteTrombi(long idTrombi);

    // Supprime réellement les Trombis qui ont étés soft delete
    @Query("DELETE FROM table_trombi WHERE is_deleted = 1")
    void deleteSoftDeletedTrombis();
}
