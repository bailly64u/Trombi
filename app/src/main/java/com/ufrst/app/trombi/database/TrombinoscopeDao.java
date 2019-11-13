package com.ufrst.app.trombi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public interface TrombinoscopeDao {

    @Insert
    void insert(Trombinoscope trombinoscope);

    @Update
    void update(Trombinoscope trombinoscope);

    @Delete
    void delete(Trombinoscope trombinoscope);
}
