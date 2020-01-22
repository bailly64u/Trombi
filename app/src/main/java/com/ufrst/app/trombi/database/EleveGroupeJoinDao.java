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
    // Comme Room utilisera deux requêtes pour nous dans les coulisses, on annote une Transaction pour s'assurer que cela se passe atomiquement
    /*@Transaction
    @Query("SELECT * FROM table_groupe")
    LiveData<List<GroupeWithEleves>> getGroupesWithEleves();*/

    // GroupeWitheEleves sans Objets représentant chaque groupe et sa liste d'élèves (sans les élèves soft deleted)
    @Query("SELECT * FROM eleve_x_group " +
            "INNER JOIN table_eleve ON table_eleve.id_eleve = eleve_x_group.join_id_eleve " +
            "INNER JOIN table_groupe ON table_groupe.id_groupe = eleve_x_group.join_id_groupe " +
            "WHERE table_eleve.is_deleted = 0")
    LiveData<List<GroupeWithEleves>> getGroupesWithEleves();

    // Retourne un objet GroupeWithEleve avec un groupe d'un certain id
    @Transaction
    @Query("SELECT * FROM table_groupe WHERE id_groupe=:idGroupe")
    LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe);

    // Retourne une liste d'objet contenant un Eleve avec sa liste de groupes
    // Comme Room utilisera deux requêtes pour nous dans les coulisses, on annote une Transaction pour s'assurer que cela se passe atomiquement
    @Transaction
    @Query("SELECT * FROM table_eleve ORDER BY nom_prenom")
    LiveData<List<EleveWithGroups>> getElevesWithGroups();

    // Retourne un objet EleveWithGroups avec un groupe d'un certain id
    @Transaction
    @Query("SELECT * FROM table_eleve WHERE id_eleve=:idEleve")
    LiveData<EleveWithGroups> getEleveByIdWithGroups(long idEleve);
}
