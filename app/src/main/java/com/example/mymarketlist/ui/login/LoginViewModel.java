package com.example.mymarketlist.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.User;

import java.util.List;


public class LoginViewModel extends ViewModel {

    private static LiveData<List<User>> usersList;

    public LoginViewModel() {
        usersList = Model.instance.getAllUsers();
    }

    public LiveData<List<User>> getData() {
        return usersList;
    }

    public static boolean isUserExist(String userEmail,String password){

        if(usersList.getValue()!=null)
            for (User user : usersList.getValue())
                if (user.getEmail().equals(userEmail) && user.getPassword().equals(password) && !(user.isDeleted()))
                    return true;

        return false;
    }

    public static boolean isUserExistResentPassword(String userEmail ){

        if(usersList.getValue()!=null)
            for (User user : usersList.getValue())
                if (user.getEmail().equals(userEmail)  && !(user.isDeleted()))
                    return true;

        return false;
    }

}