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
import android.widget.ImageView;
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
    Dialog resentEmailDialog;
    TextView isExistTv;
    TextView forgotPassword;
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
        forgotPassword = view.findViewById(R.id.login_forgot_password_tv);
        resentEmailDialog = new Dialog(getContext());
        popupLoadingDialog();
        setUpProgressListener();
        isLoggedIn();

        //ViewModel:
        //ViewModel:
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getData().observe(getViewLifecycleOwner(), (data)->{});

        //Listeners:
        newAccountTv.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.nav_signUpFragment));
        enterBtn.setOnClickListener(v->login());
        forgotPassword.setOnClickListener(v->popupResentEmailDialog());
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
        else {
            dialog.show();
            isExistTv.setText("");
            Model.instance.login(emailEt.getText().toString(), passwordEt.getText().toString(), (isSuccess) -> {
                if(isSuccess) {
                    //Pop up login page after we connected:
                    while (Navigation.findNavController(view).popBackStack()) ;
                    dialog.dismiss();
                    Navigation.findNavController(view).navigate(R.id.nav_itemsListFragment);
                }
                else{
                    dialog.dismiss();
                    isExistTv.setText("כתובת האימייל או הסיסמה אינם נכונים");
                }
            });
        }
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

    private void popupResentEmailDialog() {

        resentEmailDialog = new Dialog(getContext());
        resentEmailDialog.setContentView(R.layout.popup_dialog_resent_email);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            resentEmailDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        resentEmailDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        resentEmailDialog.setCancelable(true);
        resentEmailDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;
        TextView textTv= resentEmailDialog.findViewById(R.id.resent_password_dialog_text_tv);
        EditText dialogEmailEt= resentEmailDialog.findViewById(R.id.resent_password_dialog_email_et);
        Button button= resentEmailDialog.findViewById(R.id.resent_password_enter_btn);
        ImageView cancelImgV= resentEmailDialog.findViewById(R.id.resent_password_dialog_cancel_imgV);

        //Listeners
        cancelImgV.setOnClickListener(v->resentEmailDialog.dismiss());
        ProgressBar pb = resentEmailDialog.findViewById(R.id.resent_password_dialog_progressBar_pb);
        button.setOnClickListener(v->{
            if(loginViewModel.isUserExistResentPassword(dialogEmailEt.getText().toString()))
            {
                button.setEnabled(false);
                pb.setVisibility(View.VISIBLE);
                textTv.setText("אנא המתן...");
                Model.instance.resentPassword(dialogEmailEt.getText().toString(),(isSuccess)->
                {
                    if(isSuccess){
                        pb.setVisibility(View.INVISIBLE);
                        textTv.setText("הסיסמה נשלחה לכתובת האימייל בהצלחה!");
//                        resentEmailDialog.dismiss();
                    }
                    else{
                        button.setEnabled(true);
                        pb.setVisibility(View.INVISIBLE);
                        textTv.setText("אנא בדוק את כתובת האימייל");
                    }
                });
            }
        });
        resentEmailDialog.show();

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