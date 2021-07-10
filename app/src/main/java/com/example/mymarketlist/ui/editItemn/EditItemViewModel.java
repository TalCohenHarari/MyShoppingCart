package com.example.mymarketlist.ui.editItemn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.GeneralItem;
import com.example.mymarketlist.model.Model;

import java.util.List;

public class EditItemViewModel extends ViewModel {

    private LiveData<List<GeneralItem>> generalItemsList;
    GeneralItem item;

    public EditItemViewModel() { generalItemsList = Model.instance.getAllGeneralItems(); item=new GeneralItem(); }

    public LiveData<List<GeneralItem>>  getData() {
        return generalItemsList;
    }

    public GeneralItem getItem(String itemId) {

        if(generalItemsList.getValue()!=null){
            for (GeneralItem i: generalItemsList.getValue()) {
                if ((i.getName().equals(itemId))){
                    item=i;
                    break;
                }
            }
        }
        return item;
    }


}