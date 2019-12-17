package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

// POJO
@Entity(tableName = "table_groupe"
        /*foreignKeys = @ForeignKey(
                entity = Trombinoscope.class,
                parentColumns = "id_trombi",
                childColumns = "id_groupe",
                onDelete = CASCADE
        )*/
)
public class Groupe {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_groupe")
    private long idGroupe;

    @ColumnInfo(name = "nom_groupe")
    private String nomGroupe;

    @ColumnInfo(name = "id_trombi")
    private long idTrombi;

    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted = false;      // Soft delete


    public Groupe(String nomGroupe, long idTrombi) {
        this.nomGroupe = nomGroupe;
        this.idTrombi = idTrombi;
    }

    public long getIdGroupe() { return idGroupe; }
    public void setIdGroupe(long idGroupe) { this.idGroupe = idGroupe; }
    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }
    public long getIdTrombi() { return idTrombi; }
    public void setIdTrombi(long idTrombi) { this.idTrombi = idTrombi; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
