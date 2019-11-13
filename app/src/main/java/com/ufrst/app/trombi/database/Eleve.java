package com.ufrst.app.trombi.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

// POJO
@Entity(tableName = "table_eleve")
public class Eleve {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_eleve")
    private long idEleve;

    @ColumnInfo(name = "nom_prenom") private String nomPrenom;
    private String photo;

    public Eleve(String nomPrenom, String photo) {
        this.nomPrenom = nomPrenom;
        this.photo = photo;
    }

    public long getIdEleve() { return idEleve; }
    public void setIdEleve(long idEleve) { this.idEleve = idEleve; }
    public String getNomPrenom() { return nomPrenom; }
    public void setNomPrenom(String nomPrenom) { this.nomPrenom = nomPrenom; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
