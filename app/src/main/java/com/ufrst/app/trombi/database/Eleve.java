package com.ufrst.app.trombi.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

// POJO
@Entity(tableName = "table_eleve")
public class Eleve {

    // Champs présents dans la BD
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_eleve")
    private long idEleve;

    @ColumnInfo(name = "nom_prenom")
    private String nomPrenom;

    @ColumnInfo(name = "id_trombi")
    private long idTrombi;

    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted = false;      // Soft delete

    @NonNull
    private String photo;

    
    public Eleve(String nomPrenom, long idTrombi, String photo) {
        this.nomPrenom = nomPrenom;
        this.idTrombi = idTrombi;
        this.photo = photo;
    }

    public long getIdEleve() { return idEleve; }
    public void setIdEleve(long idEleve) { this.idEleve = idEleve; }
    public String getNomPrenom() { return nomPrenom; }
    public void setNomPrenom(String nomPrenom) { this.nomPrenom = nomPrenom; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public long getIdTrombi() { return idTrombi; }
    public void setIdTrombi(long idTrombi) { this.idTrombi = idTrombi; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
