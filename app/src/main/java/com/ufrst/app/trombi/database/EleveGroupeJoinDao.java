package com.ufrst.app.trombi.database;

import androidx.room.Dao;
import androidx.room.Insert;

// Pour les requêtes nécessitant une jointure
@Dao
public interface EleveGroupeJoinDao {

    @Insert
    void insert(EleveGroupeJoin eleveGroupeJoin);


}
