package com.example.mymarketlist.ui.allMyShoppingCarts;

import androidx.core.net.ParseException;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AllMyShoppingCartsViewModel extends ViewModel {

    private LiveData<List<ShoppingCart>> shoppingCartList;
    public List<ShoppingCart> list;


    public AllMyShoppingCartsViewModel() {
        shoppingCartList = Model.instance.getAllShoppingCarts();
        list = new LinkedList<>();
    }

    public LiveData<List<ShoppingCart>>  getData() {
        return shoppingCartList;
    }

    public void refresh(){
        Model.instance.getAllShoppingCarts();
    }

    public void sortByDate(List<ShoppingCart> data) {

//        list =data;

        //Remove all shoppingCarts that not belong to current user
//        for (int i=0;i<list.size();++i) {
//            if(!(list.get(i).getUserOwner().equals(Model.instance.getUser().getId())))
//                list.remove(i);
//        }
        list=new LinkedList<>();
        for (int i=0;i<data.size();++i) {
            if(data.get(i).getUserOwner().equals(Model.instance.getUser().getId()))
                list.add(data.get(i));
        }

        Collections.sort(list, (item1, item2) -> {
            Date date1 = stringToDate(item1.getDatePurchase());
            Date date2 = stringToDate(item2.getDatePurchase());

            if (date1 != null && date2 != null) {
                boolean b1;
                boolean b2;
                b1 = date2.after(date1);
                b2 = date2.before(date1);
                if (b1 != b2) {
                    if (b1) {
                        return -1;
                    }
                    if (!b1) {
                        return 1;
                    }
                }
            }
            return 0;
        });
    }
    public static Date stringToDate(String strDate) {
        if (strDate == null)
            return null;

        // change the date format whatever you have used in your model class.
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = format.parse(strDate);
            System.out.println(date);
        } catch (ParseException | java.text.ParseException e) { e.printStackTrace(); }
        return date;
    }

}