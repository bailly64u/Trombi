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
    long insert(Groupe groupe);

    @Update
    void update(Groupe groupe);

    @Delete
    void delete(Groupe groupe);

    // Récupère tous les Groupes dans l'ordre alphabétique
    @Query("SELECT * FROM table_groupe WHERE is_deleted = 0 ORDER BY nom_groupe")
    LiveData<List<Groupe>> getAllGroupes();

    // Récupère tous les groupes pour un certain trombinoscope, dans l'ordre alphabétique
    @Query("SELECT * FROM table_groupe WHERE id_trombi=:idTrombi AND is_deleted = 0 ORDER BY nom_groupe")
    LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi);

    // Supprime les Groupes appertenant à un certain Trombinoscope
    @Query("DELETE FROM table_groupe WHERE id_trombi=:idTrombi")
    void deleteGroupesForTrombi(long idTrombi);

    // Soft delete - Change la valeur du booleen isDeleted du Groupe
    @Query("UPDATE table_groupe SET is_deleted = NOT is_deleted WHERE id_groupe=:idGroupe")
    void softDeleteGroupe(long idGroupe);

    // Soft delete - Change la valeur du booleen isDeleted des Groupes d'un Trombi
    @Query("UPDATE table_groupe SET is_deleted=:isDeleted WHERE id_trombi=:idTrombi")
    void softDeleteGroupesForTrombi(long idTrombi, int isDeleted);

    // Supprime réellement les Groupes qui ont étés soft delete
    @Query("DELETE FROM table_groupe WHERE is_deleted = 1")
    void deleteSoftDeletedGroupes();
}
