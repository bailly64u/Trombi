package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;

// Classe intermédiaire pour définir une relation many-to-many
@Entity(tableName = "table_eleve_groupe",
        primaryKeys = {"idEleve", "idGroupe"},
        foreignKeys = {
                    @ForeignKey(entity = Eleve.class,
                                parentColumns = "idEleve",
                                childColumns = "joinIdEleve"),
                    @ForeignKey(entity = Groupe.class,
                                parentColumns = "idGroupe",
                                childColumns = "joinIdGroupe")
        })
public class EleveGroupeJoin {

    @ColumnInfo(name = "join_id_eleve") private long joinIdEleve;
    @ColumnInfo(name = "join_id_groupe") private long joinIdGroupe;
}
