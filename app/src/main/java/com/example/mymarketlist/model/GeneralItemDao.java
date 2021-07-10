package com.example.mymarketlist.model;

import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GeneralItemDao {
    @Query("select * from GeneralItem")
    LiveData<List<GeneralItem>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(GeneralItem... generalItems);

    @Delete
    void delete(GeneralItem generalItems);
}
