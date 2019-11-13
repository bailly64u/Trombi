package com.ufrst.app.trombi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface EleveDao {

    @Insert
    void insert(Eleve eleve);

    @Update
    void update(Eleve eleve);

    @Delete
    void delete(Eleve eleve);
}
