package com.example.mymarketlist.ui.myMarketList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class MyMarketListViewModel extends ViewModel {

    private LiveData<List<Item>> itemsList;
    private LiveData<List<ShoppingCart>> shoppingCartList;
    public List<Item> list;
    public List<ShoppingCart> sCList;

    //For unwanted refresh when phone go to side position
    public List<Item> tempList;
    public boolean firstTimeOrInRefresh=true;
    public int oldSize=0;

    //Today date
    DateTimeFormatter dtf;
    LocalDateTime now;
    String todayDate;

    public MyMarketListViewModel() {
        itemsList = Model.instance.getAllItems();
        shoppingCartList =Model.instance.getAllShoppingCarts();
        sCList=shoppingCartList.getValue();
        list = new LinkedList<>();
        tempList = new LinkedList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            now = LocalDateTime.now();
            todayDate =dtf.format(now);
        }

    }

    public LiveData<List<Item>> getData() {

        return itemsList;
    }

    public LiveData<List<ShoppingCart>>  getShoppingCartData() { return shoppingCartList; }

    public List<Item> getGeneralData(int position,List<Item> tempItemList) {
        list = new LinkedList<>();
        if(shoppingCartList.getValue()!=null) {
            ShoppingCart shoppingCart = shoppingCartList.getValue().get(position);
//            if(shoppingCart.getUserOwner().equals(Model.instance.getUser().getId())) {
                for (Item item : tempItemList) {
                    if (item.getUserOwner().equals(Model.instance.getUser().getId())
                            && item.getOwner().equals(shoppingCart.getId())
                            && item.getDatePurchase().equals(shoppingCart.getDatePurchase()))
                        list.add(item);
                }
//            }
        }
        return list;
    }

    public void refresh(){
        Model.instance.getAllItems();
    }
}