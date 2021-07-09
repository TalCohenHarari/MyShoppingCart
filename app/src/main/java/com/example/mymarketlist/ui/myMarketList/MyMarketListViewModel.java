package com.example.mymarketlist.ui.myMarketList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;

import java.util.LinkedList;
import java.util.List;

public class MyMarketListViewModel extends ViewModel {

    private LiveData<List<Item>> itemsList;
    private LiveData<List<ShoppingCart>> shoppingCartList;
    public List<Item> list;

    //For unwanted refresh when phone go to side position
    public List<Item> tempList;
    public boolean firstTimeOrInRefresh=true;


    public MyMarketListViewModel() {
        itemsList = Model.instance.getAllItems();
        shoppingCartList =Model.instance.getAllShoppingCarts();
        list = new LinkedList<>();
        tempList = new LinkedList<>();

    }

    public LiveData<List<Item>>  getData() {
        return itemsList;
    }
    public LiveData<List<ShoppingCart>>  getShoppingCartData() {
        return shoppingCartList;
    }

    public List<Item> getGeneralData(int position,List<Item> tempItemList) {
        list = new LinkedList<>();
        if(shoppingCartList.getValue()!=null) {
            ShoppingCart shoppingCart = shoppingCartList.getValue().get(position);
            for (Item item : tempItemList) {
                if (item.getOwner().equals(shoppingCart.getId()))
                    list.add(item);
            }
        }
        return list;
    }

    public void refresh(){
        Model.instance.getAllItems();
    }
}