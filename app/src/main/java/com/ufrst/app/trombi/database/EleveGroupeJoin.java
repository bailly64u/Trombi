package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

// Classe intermédiaire pour définir une relation many-to-many
@Entity(tableName = "eleve_x_group", primaryKeys = {"join_id_eleve", "join_id_groupe"},
        indices = {@Index("join_id_eleve"), @Index("join_id_groupe")})
public class EleveGroupeJoin {

    @ColumnInfo(name = "join_id_eleve")
    private long joinIdEleve;

    @ColumnInfo(name = "join_id_groupe")
    private long joinIdGroupe;

    @ColumnInfo(name = "join_id_trombi")
    private long joinIdTrombi;

    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted = false;      // Soft delete

    public EleveGroupeJoin(long joinIdEleve, long joinIdGroupe, long joinIdTrombi){
        this.joinIdEleve = joinIdEleve;
        this.joinIdGroupe = joinIdGroupe;
        this.joinIdTrombi = joinIdTrombi;
    }

    public long getJoinIdEleve(){ return joinIdEleve; }
    public void setJoinIdEleve(long joinIdEleve){ this.joinIdEleve = joinIdEleve;}
    public long getJoinIdGroupe(){ return joinIdGroupe; }
    public void setJoinIdGroupe(long joinIdGroupe){ this.joinIdGroupe = joinIdGroupe; }
    public boolean isDeleted(){ return isDeleted; }
    public void setDeleted(boolean deleted){ isDeleted = deleted; }
    public long getJoinIdTrombi(){ return joinIdTrombi; }
    public void setJoinIdTrombi(long joinIdTrombi){ this.joinIdTrombi = joinIdTrombi; }
}
