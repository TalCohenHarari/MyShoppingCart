package com.example.mymarketlist.ui.allMyShoppingCarts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mymarketlist.MyApplication;
import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;
import com.example.mymarketlist.ui.itemsList.ItemsListFragment;
import com.example.mymarketlist.ui.itemsList.ItemsListViewModel;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AllMyShoppingCartsFragment extends Fragment {
;
    View view;
    AllMyShoppingCartsViewModel allMyShoppingCartsViewModel;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView itemsList;
    MyAdapter adapter;
    LayoutAnimationController layoutAnimationController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_all_my_shopping_carts, container, false);
        swipeRefreshLayout = view.findViewById(R.id.allMyShoppingCartsList_swipeRefreshLayout);
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );

        //ViewModel
        allMyShoppingCartsViewModel  = new ViewModelProvider(this).get(AllMyShoppingCartsViewModel.class);
        allMyShoppingCartsViewModel.getData().observe(getViewLifecycleOwner(),
                (data)->{
                    allMyShoppingCartsViewModel.sortByDate(data);
                    itemsList.setLayoutAnimation(layoutAnimationController);
                    adapter.notifyDataSetChanged();
                });

        //RecyclerView:
        itemsList = view.findViewById(R.id.allMyShoppingCartsList_RecyclerView);
        itemsList.setHasFixedSize(true);
        GridLayoutManager manager = new GridLayoutManager(MyApplication.context,2,GridLayoutManager.VERTICAL,false);
//        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.context);
        itemsList.setLayoutManager(manager);
        adapter = new MyAdapter();
        itemsList.setAdapter(adapter);

         /*RecyclerView Animation:
        https://youtu.be/5PMI_bHGehg*/
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_animation_slide_right);

        //Listeners
        swipeRefreshLayout.setOnRefreshListener(()->{
            allMyShoppingCartsViewModel.refresh();
        });
        adapter.setOnClickListener(position -> {
            AllMyShoppingCartsFragmentDirections.ActionNavAllMyShoppingCartsFragmentToNavMyMarketListFragment
                    action = AllMyShoppingCartsFragmentDirections.actionNavAllMyShoppingCartsFragmentToNavMyMarketListFragment().setPosition(position);
            Navigation.findNavController(view).navigate(action);
        });
        setUpProgressListener();

        return view;
    }


    private void setUpProgressListener() {
        Model.instance.loadingState.observe(getViewLifecycleOwner(),(state)->{
            switch(state){
                case loaded:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case loading:
//                    swipeRefreshLayout.setRefreshing(true);
                    break;
            }
        });
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        OnItemClickListener listener;
        TextView dateTv;
        ImageView imageIv;
        TextView priceTv;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.allMyShoppingCartsRow_date_textView);
            imageIv = itemView.findViewById(R.id.allMyShoppingCartsRow_image_imgV);
            priceTv = itemView.findViewById(R.id.allMyShoppingCartsRow_price_tv);

            this.listener=listener;
            itemView.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION)
                        listener.onClick(position);
                }
            });
        }

        public void bind(ShoppingCart shoppingCart){
            dateTv.setText(shoppingCart.getDatePurchase());
            imageIv.setImageResource(R.drawable.background2);
            priceTv.setText(shoppingCart.getTotalPrice()+ "₪");
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);

    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        OnItemClickListener listener;

        public void setOnClickListener(OnItemClickListener listener){
            this.listener=listener;
        }
        //Create a viewHolder for the view:
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= getLayoutInflater().inflate(R.layout.all_my_shopping_carts_row,parent,false);
            MyViewHolder holder= new MyViewHolder(view,listener);
            return holder;
        }
        // make the variables bind to the created view from "onCreateViewHolder" function:
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ShoppingCart shoppingCart = allMyShoppingCartsViewModel.list.get(position);
            holder.bind(shoppingCart);
        }
        //Give me the items count:
        @Override
        public int getItemCount() {
            return allMyShoppingCartsViewModel.list.size();
        }
    }
}

