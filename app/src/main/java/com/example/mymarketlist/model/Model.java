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
    User user;
    public User getUser() {
        return user;
    }
    public void setUser(User user,isLoginSuccessCompleteListener listener) { this.user = user; listener.onComplete(true); }

    private Model() {}

    public interface OnCompleteListener {
        void onComplete();
    }

    public interface onResentPasswordCompleteListener {
        void onComplete(boolean isSuccess);
    }

    public interface isLoginSuccessCompleteListener {
        void onComplete(boolean isSuccess);
    }

    public enum LoadingState {
        loaded,
        loading,
    }

    public MutableLiveData<LoadingState> loadingStateDialog =
            new MutableLiveData<LoadingState>(LoadingState.loaded);

    public MutableLiveData<LoadingState> loadingState =
            new MutableLiveData<LoadingState>(LoadingState.loaded);


    //---------------------------------------User---------------------------------------------

    LiveData<List<User>> allUsers =   AppLocalDB.db.userDao().getAll();

    public LiveData<List<User>> getAllUsers() {
        loadingState.setValue(LoadingState.loading);
        //read the local last update time
        Long localLastUpdate = User.getLocalLastUpdateTime();
        //ge all updates from firebase
        ModelFirebase.getAllUsers(localLastUpdate,(users)->{
            executorService.execute(()->
            {
                Long lastUpdate = new Long(0);
                //update the local DB with the new records
                for (User user: users)
                {
                    if(user.isDeleted())
                    {
                        AppLocalDB.db.userDao().delete(user);
                    }
                    else{
                        AppLocalDB.db.userDao().insertAll(user);
                    }
                    //update the local last update time
                    if(lastUpdate < user.lastUpdated)
                    {
                        lastUpdate = user.lastUpdated;
                    }
                }
                User.setLocalLastUpdateTime(lastUpdate);
                //postValue make it happen in main thread of the view and not in this thread:
                loadingState.postValue(LoadingState.loaded);
                //read all the data from the local DB -> return the data to the caller
                //automatically perform by room -> live data gets updated
            });
        });

        return allUsers;
    }

    public void saveUser(User user, String action, OnCompleteListener listener) {
        loadingState.setValue(LoadingState.loading);
        ModelFirebase.saveUser(user, action, (isSuccess) -> {
            getAllUsers();
            listener.onComplete();
        });
    }

    public void login(String userEmail, String password, isLoginSuccessCompleteListener listener) {
        ModelFirebase.login(userEmail, password, (isSuccess) -> listener.onComplete(isSuccess));
    }

    public void isLoggedIn(OnCompleteListener listener) {
        ModelFirebase.isLoggedIn(() ->{
            loadingStateDialog.setValue(LoadingState.loaded);
            listener.onComplete();
        });
    }

    public static void signOut() {
        ModelFirebase.signOut();
    }

    public void resentPassword(String email,onResentPasswordCompleteListener listener) {
        ModelFirebase.resentPassword(email,(isSuccess)->{
            listener.onComplete(isSuccess);
        });
    }

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
