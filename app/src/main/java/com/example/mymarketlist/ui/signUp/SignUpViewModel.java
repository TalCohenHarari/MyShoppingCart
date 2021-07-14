package com.example.mymarketlist.ui.signUp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.User;

import java.util.List;

public class SignUpViewModel extends ViewModel {


    private LiveData<List<User>> usersList;

    public SignUpViewModel() {
        usersList = Model.instance.getAllUsers();
    }

    public LiveData<List<User>> getData() {
        return usersList;
    }


    public boolean isUserNameExist(String userEmail) {

        if(usersList.getValue()!=null)
            for (User user : usersList.getValue())
                if (user.getEmail().equals(userEmail) && !(user.isDeleted()))
                    return true;

        return false;
    }
}