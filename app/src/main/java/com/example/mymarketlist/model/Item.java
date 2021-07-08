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
public class Item{
    @PrimaryKey
    @NonNull
    private String id;
    private String owner;
    private String name;
    private String category;
    private String price="0";
    private String note="";
    private String datePurchase;
    private boolean isChecked;
    private String count="0";
    private String image;
    private Long lastUpdated;
    private boolean isDeleted;

    final static String ID = "id";
    final static String OWNER = "owner";
    final static String NAME = "name";
    final static String CATEGORY = "category";
    final static String PRICE = "price";
    final static String NOTE = "note";
    final static String DATE_PURCHASE = "datePurchase";
    final static String IS_CHECKED = "isChecked";
    final static String COUNT = "count";
    final static String IMAGE = "image";
    final static String LAST_UPDATED = "lastUpdated";
    final static String IS_DELETED = "isDeleted";


    public Item(){}
    @Ignore
    public Item(Item item){
        this.id = UUID.randomUUID().toString();
        this.setOwner(item.getOwner());
        this.name=item.getName();
        this.category = item.getCategory();
        this.price=item.getPrice();
        this.count=item.getCount();
        this.datePurchase=item.getDatePurchase();
        this.image=item.getImage();
        this.isDeleted=false;
    }


    @Ignore
    public Item(String name,String category,String price, String datePurchase,String image,boolean isDeleted ){
        this.id = UUID.randomUUID().toString();
        this.owner="";
        this.name=name;
        this.category=category;
        this.price=price;
        this.datePurchase=datePurchase;
        this.image=image;
        this.isDeleted=isDeleted;
    }

    //Setters:
    public void setId(@NonNull String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public void setDatePurchase(String datePurchase) {
        this.datePurchase = datePurchase;
    }
    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    public void setCount(String count) {
        this.count = count;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public void setCategory(String category) { this.category = category; }
    public String getNote() { return note; }

    //Getters:
    @NonNull
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPrice() {
        return price;
    }
    public String getDatePurchase() {
        return datePurchase;
    }
    public boolean isChecked() {
        return isChecked;
    }
    public String getCount() {
        return count;
    }
    public String getImage() {
        return image;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public Long getLastUpdated() {
        return lastUpdated;
    }
    public String getOwner() {
        return owner;
    }
    public String getCategory() { return category; }
    public void setNote(String note) { this.note = note; }

    public Map<String,Object> toJson(){
        Map<String, Object> json = new HashMap<>();
        json.put(ID, id);
        json.put(OWNER, owner);
        json.put(NAME, name);
        json.put(CATEGORY, category);
        json.put(PRICE, price);
        json.put(NOTE, note);
        json.put(DATE_PURCHASE, datePurchase);
        json.put(IS_CHECKED, isChecked);
        json.put(COUNT, count);
        json.put(IMAGE, image);
        json.put(IS_DELETED,isDeleted);
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());


        return json;
    }

    static public Item create(Map<String,Object> json) {
        Item item = new Item();
        item.id = (String)json.get(ID);
        item.owner = (String)json.get(OWNER);
        item.name = (String)json.get(NAME);
        item.category = (String)json.get(CATEGORY);
        item.price = (String)json.get(PRICE);
        item.note = (String)json.get(NOTE);
        item.datePurchase = (String)json.get(DATE_PURCHASE);
        item.isChecked = (boolean)json.get(IS_CHECKED);
        item.count = (String)json.get(COUNT);
        item.image = (String)json.get(IMAGE);
        item.isDeleted = (boolean)json.get(IS_DELETED);
        Timestamp ts = (Timestamp) json.get(LAST_UPDATED);

        if(ts!=null)
            item.lastUpdated = new Long(ts.getSeconds());
        else
            item.lastUpdated = new Long(0);


        return item;
    }

    private static final String ITEM_LAST_UPDATE = "ItemLastUpdate";

    static public void setLocalLastUpdateTime(Long ts){
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(ITEM_LAST_UPDATE,ts);
        editor.commit();
    }

    static public Long getLocalLastUpdateTime(){
         return MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                 .getLong(ITEM_LAST_UPDATE,0);
    }

}
