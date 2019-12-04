package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

// Classe intermédiaire pour définir une relation many-to-many
@Entity(indices = {@Index("join_id_eleve"), @Index("join_id_groupe")},
        tableName = "table_eleve_groupe",
        primaryKeys = {"join_id_eleve", "join_id_groupe"},
        foreignKeys = {
                    @ForeignKey(entity = Eleve.class,
                                parentColumns = "id_eleve",
                                childColumns = "join_id_eleve"),
                    @ForeignKey(entity = Groupe.class,
                                parentColumns = "id_groupe",
                                childColumns = "join_id_groupe")
        })
public class EleveGroupeJoin {

    @ColumnInfo(name = "join_id_eleve") private long joinIdEleve;
    @ColumnInfo(name = "join_id_groupe") private long joinIdGroupe;

    public EleveGroupeJoin(long joinIdEleve, long joinIdGroupe){
        this.joinIdEleve = joinIdEleve;
        this.joinIdGroupe = joinIdGroupe;
    }

    public long getJoinIdEleve(){ return joinIdEleve; }
    public void setJoinIdEleve(long joinIdEleve){ this.joinIdEleve = joinIdEleve;}
    public long getJoinIdGroupe(){ return joinIdGroupe; }
    public void setJoinIdGroupe(long joinIdGroupe){ this.joinIdGroupe = joinIdGroupe; }
}
