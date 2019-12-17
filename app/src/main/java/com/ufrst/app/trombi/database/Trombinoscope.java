package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// POJO
@Entity(indices = {@Index("id_trombi")}, tableName = "table_trombi")
public class Trombinoscope {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_trombi")
    private long idTrombi;

    @ColumnInfo(name = "nom_trombi")
    private String nomTrombi;

    @ColumnInfo(name = "desc_trombi")
    private String description;

    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted = false;      // Soft delete


    public Trombinoscope(String nomTrombi, String description) {
        this.nomTrombi = nomTrombi;
        this.description = description;
    }

    public long getIdTrombi() { return idTrombi; }
    public void setIdTrombi(long idTrombi) { this.idTrombi = idTrombi; }
    public String getNomTrombi() { return nomTrombi; }
    public void setNomTrombi(String nomTrombi) { this.nomTrombi = nomTrombi; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
