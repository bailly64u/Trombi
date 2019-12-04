package com.ufrst.app.trombi.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

// Pour les requêtes nécessitant une jointure
@Dao
public interface EleveGroupeJoinDao {

    @Insert
    void insert(EleveGroupeJoin eleveGroupeJoin);

    // Retourne une liste d'élèves appartenant à un certain groupe
    @Query("SELECT * FROM table_eleve INNER JOIN table_eleve_groupe " +
            "ON table_eleve.id_eleve=table_eleve_groupe.join_id_eleve " +
            "WHERE table_eleve_groupe.join_id_groupe=:groupId")
    LiveData<List<Eleve>> getElevesForGroupe(long groupId);

}
