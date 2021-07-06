package com.example.mymarketlist.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("select * from Item")
    LiveData<List<Item>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Item... items);

    @Delete
    void delete(Item items);
}
