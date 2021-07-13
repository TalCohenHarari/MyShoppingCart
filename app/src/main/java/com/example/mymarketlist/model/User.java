package com.example.mymarketlist.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.mymarketlist.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;


@Entity
public class User{
    @PrimaryKey
    @NonNull
    public String id;
    public String email;
    public String password;
    public String name;
    public String phone;
    public String avatar;
    public Long lastUpdated;
    public boolean isDeleted;

    final static String ID = "id";
    final static String NAME = "name";
    final static String PASSWORD = "password";
    final static String EMAIL = "email";
    final static String PHONE = "phone";
    final static String AVATAR = "avatar";
    final static String LAST_UPDATED = "lastUpdated";
    final static String IS_DELETED = "isDeleted";


    //Setters:
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    //Getters:
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public boolean isDeleted() {
        return isDeleted;
    }


    public Map<String,Object> toJson(){
        Map<String, Object> json = new HashMap<>();
        json.put(ID, id);
        json.put(NAME, name);
        json.put(PASSWORD, password);
        json.put(EMAIL, email);
        json.put(PHONE, phone);
        json.put(AVATAR, avatar);
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        json.put(IS_DELETED,isDeleted);

        return json;
    }

    static public User create(Map<String,Object> json) {
        User user = new User();
        user.id = (String)json.get(ID);
        user.name = (String)json.get(NAME);
        user.password = (String)json.get(PASSWORD);
        user.email = (String)json.get(EMAIL);
        user.phone = (String)json.get(PHONE);
        user.avatar = (String)json.get(AVATAR);
        Timestamp ts = (Timestamp) json.get(LAST_UPDATED);

        if(ts!=null)
            user.lastUpdated = new Long(ts.getSeconds());
        else
            user.lastUpdated = new Long(0);

        user.isDeleted = (boolean)json.get(IS_DELETED);

        return user;
    }

    private static final String USER_LAST_UPDATE = "UserLastUpdate";

    static public void setLocalLastUpdateTime(Long ts){
        //Shared preference, saving the ts on the disk (like the db):
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(USER_LAST_UPDATE,ts);
        editor.commit();
    }

    static public Long getLocalLastUpdateTime(){
        //Shared preference, saving the ts in app:
         return MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                 .getLong(USER_LAST_UPDATE,0);
    }

}
