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

    // Récupère tous les Eleves dans l'ordre alphabétique
    @Query("SELECT * FROM table_eleve WHERE is_deleted = 0 ORDER BY nom_prenom")
    LiveData<List<Eleve>> getAllEleves();

    // Récupère les Eleves d'un trombinoscope
    @Query("SELECT * FROM table_eleve WHERE id_trombi=:idTrombi ORDER BY nom_prenom")
    LiveData<List<Eleve>> getElevesByTrombi(long idTrombi);

    // Supprime les Eleves appertenant à un certain Trombinoscope
    @Query("DELETE FROM table_eleve WHERE id_trombi=:idTrombi")
    void deleteElevesForTrombi(long idTrombi);

    // Soft delete - Change la valeur du booleen isDeleted de l'Eleve
    @Query("UPDATE table_eleve SET is_deleted = 1 WHERE id_eleve=:idEleve")
    void softDeleteEleve(long idEleve);

    // Soft delete - Change la valeur du booleen isDeleted des Eleves d'un Trombi
    @Query("UPDATE table_eleve SET is_deleted = 1 WHERE id_trombi=:idTrombi")
    void softDeleteElevesForTrombi(long idTrombi);

    // Supprime réellement les Eleves qui ont étés soft delete
    @Query("DELETE FROM table_eleve WHERE is_deleted = 1")
    void deleteSoftDeletedEleves();
}
