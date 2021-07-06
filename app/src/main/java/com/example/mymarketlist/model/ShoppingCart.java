package com.example.mymarketlist.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.mymarketlist.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Entity
public class ShoppingCart {
    @PrimaryKey
    @NonNull
    private String id;
    private String totalPrice="0";
    private String datePurchase;
    private Long lastUpdated;
    private boolean isDeleted;

    final static String ID = "id";
    final static String TOTAL_PRICE = "totalPrice";
    final static String DATE_PURCHASE = "datePurchase";
    final static String LAST_UPDATED = "lastUpdated";
    final static String IS_DELETED = "isDeleted";


    public ShoppingCart(){}

    //Setters:
    public void setId(@NonNull String id) {
        this.id = id;
    }
    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void setDatePurchase(String datePurchase) {
        this.datePurchase = datePurchase;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    //Getters:
    @NonNull
    public String getId() {
        return id;
    }
    public String getTotalPrice() {
        return totalPrice;
    }
    public String getDatePurchase() {
        return datePurchase;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public Long getLastUpdated() {
        return lastUpdated;
    }


    public Map<String,Object> toJson(){
        Map<String, Object> json = new HashMap<>();
        json.put(ID, id);
        json.put(TOTAL_PRICE, totalPrice);
        json.put(DATE_PURCHASE, datePurchase);
        json.put(IS_DELETED,isDeleted);
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());


        return json;
    }

    static public ShoppingCart create(Map<String,Object> json) {
        ShoppingCart item = new ShoppingCart();
        item.id = (String)json.get(ID);
        item.totalPrice = (String)json.get(TOTAL_PRICE);
        item.datePurchase = (String)json.get(DATE_PURCHASE);
        item.isDeleted = (boolean)json.get(IS_DELETED);
        Timestamp ts = (Timestamp) json.get(LAST_UPDATED);

        if(ts!=null)
            item.lastUpdated = new Long(ts.getSeconds());
        else
            item.lastUpdated = new Long(0);


        return item;
    }

    private static final String SHOPPING_CART_LAST_UPDATE = "ShoppingCartLastUpdate";

    static public void setLocalLastUpdateTime(Long ts){
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(SHOPPING_CART_LAST_UPDATE,ts);
        editor.commit();
    }

    static public Long getLocalLastUpdateTime(){
         return MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                 .getLong(SHOPPING_CART_LAST_UPDATE,0);
    }

}
