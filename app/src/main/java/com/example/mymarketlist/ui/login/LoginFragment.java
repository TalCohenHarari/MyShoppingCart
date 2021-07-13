package com.example.mymarketlist.ui.login;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.User;


public class LoginFragment extends Fragment {
    View view;
    EditText emailEt;
    EditText passwordEt;
    Button enterBtn;
    TextView newAccountTv;
    LoginViewModel loginViewModel;
    Dialog dialog;
    TextView isExistTv;
    User user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize
        view = inflater.inflate(R.layout.fragment_login, container, false);
        emailEt= view.findViewById(R.id.login_email_address_et);
        passwordEt= view.findViewById(R.id.login_password_et);
        enterBtn= view.findViewById(R.id.login_enter_btn);
        newAccountTv= view.findViewById(R.id.login_new_account_tv);
        isExistTv = view.findViewById(R.id.login_validationText_tv);
        popupLoadingDialog();
        setUpProgressListener();
        isLoggedIn();

//        user=new User();
//        user.setEmail("edenhararicohen@walla.com");
//        user.setDeleted(false);
//        user.setAvatar("");
//        user.setName("Eden");
//        user.setPhone("0123456789");
//        user.setPassword("123456");
//        Model.instance.saveUser(user,"signUp",()->{});


        //ViewModel:
        //ViewModel:
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getData().observe(getViewLifecycleOwner(), (data)->{});

        //Listeners:
        newAccountTv.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.nav_signUpFragment));
        enterBtn.setOnClickListener(v->login());

        return view;
    }

    private void isLoggedIn(){
        //If the user is still logged in:
        Model.instance.isLoggedIn(()->{
            //Pop the last login page to start from main page for connected users:
            Navigation.findNavController(view).popBackStack();
            Navigation.findNavController(view).navigate(R.id.nav_itemsListFragment);

        });
    }

    private void login(){
        if(emailEt.getText().toString().isEmpty() && passwordEt.getText().toString().isEmpty())
            isExistTv.setText("הכנס כתובת אימייל וסיסמה");
        else if(emailEt.getText().toString().isEmpty() && !passwordEt.getText().toString().isEmpty())
            isExistTv.setText("הכנס כתובת אימייל");
        else if(!emailEt.getText().toString().isEmpty() && passwordEt.getText().toString().isEmpty())
            isExistTv.setText("הכנס סיסמה");
        else if(loginViewModel.isUserExist(emailEt.getText().toString(), passwordEt.getText().toString()))
        {
            dialog.show();
            isExistTv.setText("");
            Model.instance.login(emailEt.getText().toString(), passwordEt.getText().toString(), () -> {

                //Pop up login page after we connected:
                while(Navigation.findNavController(view).popBackStack());
                dialog.dismiss();
                Navigation.findNavController(view).navigate(R.id.nav_itemsListFragment);
            });
        }
        else
            isExistTv.setText("כתובת האימייל או הסיסמה אינם נכונים");
    }

    private void setUpProgressListener() {
        Model.instance.loadingStateDialog.observe(getViewLifecycleOwner(),(state)->{
            switch(state){
                case loaded:
                    dialog.dismiss();
                    break;
                case loading:
                    dialog.show();
                    break;
            }
        });
    }
    private void popupLoadingDialog() {

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.popup_dialog_loading);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;
        ProgressBar pb = dialog.findViewById(R.id.loading_progressBar_pb);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if( ((AppCompatActivity)getActivity()).getSupportActionBar()!=null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        if( ((AppCompatActivity)getActivity()).getSupportActionBar()!=null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}