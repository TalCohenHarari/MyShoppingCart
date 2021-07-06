package com.example.mymarketlist.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mymarketlist.R;

public class HomeFragment extends Fragment {

    View view;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        Button enterBtn = view.findViewById(R.id.home_enter_btn);
        enterBtn.setOnClickListener(v->{
            Navigation.findNavController(view).popBackStack();
            Navigation.findNavController(v).navigate(R.id.nav_itemsListFragment);
        });

        return view;
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