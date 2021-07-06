package com.example.mymarketlist.model;

public class Category {

    private String name;
    private Integer image;

    public Category(String name, Integer image){
        this.name=name;
        this.image=image;
    }

    //Setters:
    public void setName(String name) { this.name = name; }
    public void setImage(Integer image) { this.image = image; }

    //Getters:
    public String getName() { return name; }
    public Integer getImage() { return image; }


}
