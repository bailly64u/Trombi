package com.ufrst.app.trombi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public interface GroupeDao {

    @Insert
    void insert(Groupe groupe);

    @Update
    void update(Groupe groupe);

    @Delete
    void delete(Groupe groupe);
}
