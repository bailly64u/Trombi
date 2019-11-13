package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// POJO
// Indexe la table idTrombi pour éviter un scan de toutes la table lors de la recherche de la clef primaire
@Entity(indices = {@Index("idTrombi")},
        tableName = "table_groupe",
        foreignKeys = @ForeignKey(entity = Trombinoscope.class,
                                    parentColumns = "idGroupe",
                                    childColumns = "idTrombi"))
public class Groupe {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_groupe")
    private long idGroupe;

    @ColumnInfo(name = "nom_groupe") private String nomGroupe;

    public Groupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
    }

    public long getIdGroupe() { return idGroupe; }
    public void setIdGroupe(long idGroupe) { this.idGroupe = idGroupe; }
    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }
}
