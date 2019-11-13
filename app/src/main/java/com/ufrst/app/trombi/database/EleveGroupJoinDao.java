package com.ufrst.app.trombi.database;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface EleveGroupJoinDao {

    @Insert
    void insert(EleveGroupeJoin eleveGroupeJoin);


}
