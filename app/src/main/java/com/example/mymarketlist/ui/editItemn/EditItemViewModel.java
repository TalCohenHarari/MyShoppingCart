package com.example.mymarketlist.ui.editItemn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Category;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;

import java.util.LinkedList;
import java.util.List;

public class EditItemViewModel extends ViewModel {

    private LiveData<List<Item>> itemsList;
    Item item;

    public EditItemViewModel() { itemsList = Model.instance.getAllItems(); item=new Item(); }

    public LiveData<List<Item>>  getData() {
        return itemsList;
    }

    public Item getItem(String itemId) {

        if(itemsList.getValue()!=null){
            for (Item i: itemsList.getValue()) {
                if ((i.getId().equals(itemId))){
                    item=i;
                    break;
                }
            }
        }
        return item;
    }


}