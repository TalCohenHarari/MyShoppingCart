package com.example.mymarketlist.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mymarketlist.MyApplication;


@Database(entities = {User.class,GeneralItem.class,Item.class,ShoppingCart.class}, version = 4)
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract GeneralItemDao generalItemDao();
    public abstract ItemDao itemDao();
    public abstract ShoppingCartDao shoppingCartDao();
}

public class AppLocalDB{
    public final static  AppLocalDbRepository db =
            Room.databaseBuilder(MyApplication.context,
                    AppLocalDbRepository.class,
                    "dbMyMarketList.db")
                    .fallbackToDestructiveMigration()
                    .build();
}

