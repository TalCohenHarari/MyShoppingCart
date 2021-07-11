package com.example.mymarketlist.model;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Model {

    final public static Model instance = new Model();
    ExecutorService executorService = Executors.newCachedThreadPool();

    private Model() {}

    public interface OnCompleteListener {
        void onComplete();
    }

    public enum LoadingState {
        loaded,
        loading,
    }

    public MutableLiveData<LoadingState> loadingStateDialog =
            new MutableLiveData<LoadingState>(LoadingState.loaded);

    public MutableLiveData<LoadingState> loadingState =
            new MutableLiveData<LoadingState>(LoadingState.loaded);



    //---------------------------------------Item---------------------------------------------

    LiveData<List<Item>> allItems =   AppLocalDB.db.itemDao().getAll();

    public LiveData<List<Item>> getAllItems() {
        loadingState.setValue(LoadingState.loading);
        Long localLastUpdate = Item.getLocalLastUpdateTime();
        ModelFirebase.getAllItems(localLastUpdate,(items)->{
            executorService.execute(()->
            {
                Long lastUpdate = new Long(0);
                for (Item item: items)
                {
                    if(item.isDeleted())
                    {
                        AppLocalDB.db.itemDao().delete(item);
                    }
                    else{
                        AppLocalDB.db.itemDao().insertAll(item);
                    }
                    if(lastUpdate < item.getLastUpdated())
                    {
                        lastUpdate = item.getLastUpdated();
                    }
                }
                Item.setLocalLastUpdateTime(lastUpdate);
                loadingState.postValue(LoadingState.loaded);
            });
        });

        return allItems;
    }


    public void saveItem(Item item, OnCompleteListener listener) {
        loadingState.setValue(LoadingState.loading);
        ModelFirebase.saveItem(item, () -> {
            getAllItems();
            listener.onComplete();
        });
    }

    public void updateInLiveItem(Item item, OnCompleteListener listener) {
        ModelFirebase.updateInLiveItem(item, () -> {
            listener.onComplete();
        });
    }

    public void updateInLiveShoppingCart(ShoppingCart shoppingCart, OnCompleteListener listener) {
        ModelFirebase.updateInLiveShoppingCart(shoppingCart, () -> {
            listener.onComplete();
        });
    }


    //---------------------------------------General Items---------------------------------------------


    LiveData<List<GeneralItem>> allGeneralItems =   AppLocalDB.db.generalItemDao().getAll();

    public LiveData<List<GeneralItem>> getAllGeneralItems() {
        loadingState.setValue(LoadingState.loading);
        Long localLastUpdate = GeneralItem.getLocalLastUpdateTime();
        ModelFirebase.getAllGeneralItems(localLastUpdate,(generalItems)->{
            executorService.execute(()->
            {
                Long lastUpdate = new Long(0);
                for (GeneralItem gi: generalItems)
                {
                    if(gi.isDeleted())
                    {
                        AppLocalDB.db.generalItemDao().delete(gi);
                    }
                    else{
                        AppLocalDB.db.generalItemDao().insertAll(gi);
                    }
                    if(lastUpdate < gi.getLastUpdated())
                    {
                        lastUpdate = gi.getLastUpdated();
                    }
                }
                GeneralItem.setLocalLastUpdateTime(lastUpdate);
                loadingState.postValue(LoadingState.loaded);
            });
        });

        return allGeneralItems;
    }

    public void saveGeneralItem(GeneralItem generalItem, OnCompleteListener listener) {
        loadingState.setValue(LoadingState.loading);
        ModelFirebase.saveGeneralItem(generalItem, () -> {
            getAllGeneralItems();
            listener.onComplete();
        });
    }

    //---------------------------------------ShoppingCart---------------------------------------------

    LiveData<List<ShoppingCart>> allShoppingCart =   AppLocalDB.db.shoppingCartDao().getAll();

    public LiveData<List<ShoppingCart>> getAllShoppingCarts() {
        loadingState.setValue(LoadingState.loading);
        Long localLastUpdate = ShoppingCart.getLocalLastUpdateTime();
        ModelFirebase.getAllShoppingCarts(localLastUpdate,(shoppingCarts)->{
            executorService.execute(()->
            {
                Long lastUpdate = new Long(0);
                for (ShoppingCart s: shoppingCarts)
                {
                    if(s.isDeleted())
                    {
                        AppLocalDB.db.shoppingCartDao().delete(s);
                    }
                    else{
                        AppLocalDB.db.shoppingCartDao().insertAll(s);
                    }
                    if(lastUpdate < s.getLastUpdated())
                    {
                        lastUpdate = s.getLastUpdated();
                    }
                }
                Item.setLocalLastUpdateTime(lastUpdate);
                loadingState.postValue(LoadingState.loaded);
            });
        });

        return allShoppingCart;
    }

    public void saveShoppingCart(ShoppingCart shoppingCart, OnCompleteListener listener) {
        loadingState.setValue(LoadingState.loading);
        ModelFirebase.saveShoppingCart(shoppingCart, () -> {
            getAllShoppingCarts();
            listener.onComplete();
        });
    }

    //----------------------------Save Images In Firebase----------------------------------
    public interface  UpLoadImageListener{
        void onComplete(String url);
    }

    public static void uploadImage(Bitmap imageBmp, String name, final UpLoadImageListener listener) {
        ModelFirebase.uploadImage(imageBmp,name,listener);
    }

}
