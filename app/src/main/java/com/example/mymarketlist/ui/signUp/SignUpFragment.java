package com.example.mymarketlist.ui.signUp;

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
import com.example.mymarketlist.ui.login.LoginViewModel;


public class SignUpFragment extends Fragment {

    View view;
    EditText emailEt;
    EditText passwordEt;
    Button enterBtn;
    TextView newAccountTv;
    SignUpViewModel signUpViewModel;
    Dialog dialog;
    TextView isExistTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        emailEt= view.findViewById(R.id.sign_up_email_address_et);
        passwordEt= view.findViewById(R.id.sign_up_password_et);
        enterBtn= view.findViewById(R.id.sign_up_enter_btn);
        newAccountTv= view.findViewById(R.id.sign_up_have_an_account_tv);
        isExistTv = view.findViewById(R.id.signUp_validationText_tv);
        popupLoadingDialog();
        setUpProgressListener();

        //ViewModel:
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        signUpViewModel.getData().observe(getViewLifecycleOwner(), (data)->{});

        //Listeners:
        newAccountTv.setOnClickListener(v-> Navigation.findNavController(v).navigateUp());
        enterBtn.setOnClickListener(v->save());


        return view;
    }

    private void save() {

        int emailLen =  emailEt.getText().toString().length();

        if(emailEt.getText().toString().isEmpty() && passwordEt.getText().toString().isEmpty()){
            isExistTv.setText("אנא הכנס כתובת אימייל וסיסמה");
        }
        else if(emailEt.getText().toString().isEmpty()){
            isExistTv.setText("אנא הכנס כתובת אימייל");

        }
        else if(!(emailEt.getText().toString().contains("@"))
                || (emailEt.getText().toString().indexOf("@")==0)
                || emailEt.getText().toString().indexOf("@")== (emailLen-1)
                || !(emailEt.getText().toString().contains(".com")))
        {
            isExistTv.setText("אנא הכנס כתובת אימייל תקינה");
        }
        else if(passwordEt.getText().toString().isEmpty()){
            isExistTv.setText("אנא הכנס סיסמה");
        }
        else if(passwordEt.getText().toString().length() <6){
            isExistTv.setText("הסיסמה חייבת להכיל לפחות 6 תווים");
        }
        else if(!(signUpViewModel.isUserNameExist(emailEt.getText().toString()))){
            dialog.show();
            isExistTv.setText("");
            User user=new User();
            user.setEmail(emailEt.getText().toString());
            user.setDeleted(false);
            user.setAvatar("");
            user.setName("");
            user.setPhone("");
            user.setPassword(passwordEt.getText().toString());
            Model.instance.saveUser(user,"signUp",()->{
                while (Navigation.findNavController(view).popBackStack());
                Navigation.findNavController(view).navigate(R.id.nav_itemsListFragment);
                dialog.dismiss();
            });

        }
        else{
            isExistTv.setText("משתמש זה כבר קיים במערכת");
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