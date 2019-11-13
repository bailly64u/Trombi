package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// POJO
@Entity(tableName = "table_trombi")
public class Trombinoscope {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_trombi")
    private long idTrombi;

    private String nomTrombi, description;

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
}
