package com.example.mymarketlist.ui.myMarketList;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mymarketlist.MyApplication;
import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class MyMarketListFragment extends Fragment {

    View view;
    MyMarketListViewModel myMarketListViewModel;
    SwipeRefreshLayout swipeRefreshLayout;
    MyAdapter adapter;
    int shoppingCartPosition;
    EditText priceEd;
    DateTimeFormatter dtf;
    LocalDateTime now;
    String todayDate;
    ImageView updateImgV;
    RecyclerView recyclerView;
    boolean update=false;
    TextView addItemTextTv;
    TextView priceTextTv;
    ImageView addItemImgV;
    ImageButton shareIconBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initialise Params
        view = inflater.inflate(R.layout.fragment_my_market_list, container, false);
        swipeRefreshLayout = view.findViewById(R.id.myMarketList_swipeRefreshLayout);
        priceEd = view.findViewById(R.id.myMarketList_price_et);
        updateImgV = view.findViewById(R.id.myMarketList_update_icon_imgV);
        addItemTextTv= view.findViewById(R.id.myMarketList_add_item__text_tv);
        priceTextTv = view.findViewById(R.id.myMarketList_price_headLine_tv);
        shareIconBtn = view.findViewById(R.id.myMarketList_share_icon_imgB);
        addItemImgV = view.findViewById(R.id.myMarketList_addItem_imgV);
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            now = LocalDateTime.now();
            todayDate =dtf.format(now);
        }

        //Bundle args
        shoppingCartPosition = MyMarketListFragmentArgs.fromBundle(getArguments()).getPosition();
        if(shoppingCartPosition==-1)
            update=false;


        //ViewModel
        myMarketListViewModel  = new ViewModelProvider(this).get(MyMarketListViewModel.class);
        myMarketListViewModel.getData().observe(getViewLifecycleOwner(), items -> initData(items));
        myMarketListViewModel.getShoppingCartData().observe(getViewLifecycleOwner(), new Observer<List<ShoppingCart>>() {@Override public void onChanged(List<ShoppingCart> shoppingCarts){ }});


        //RecyclerView:
        recyclerView = view.findViewById(R.id.myMarketList_RecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.context);
        recyclerView.setLayoutManager(manager);
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        //Listeners
        swipeRefreshLayout.setOnRefreshListener(()->myMarketListViewModel.refresh());
        updateImgV.setOnClickListener(v->updatePrice());
        adapter.setOnClickListener(position -> {});
        setUpProgressListener();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        shareIconBtn.setOnClickListener(v->shareMyShoppingCart());

        return view;
    }
    //-------------------------------------------------------Share My Shopping Cart----------------------------------------------------------------------

    private void shareMyShoppingCart() {
        StringBuilder text = new StringBuilder();
        text.append("רשימת סופר שבועית:");
        text.append("\n\n");
        for (Item item: myMarketListViewModel.list) {
            text.append(item.getName() + " - " + item.getCount() + "\n");
        }
        String sendText = text.toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sendText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    //--------------------------------------------------------Init the data to theRecyclerView and play with the view----------------------------------------------------------------------
    private void initData(List<Item> items){

        if(myMarketListViewModel.getShoppingCartData().getValue()!=null){
            List<ShoppingCart> shoppingCarts= myMarketListViewModel.getShoppingCartData().getValue();
            if(shoppingCarts.size()==0 || (!(todayDate.equals(shoppingCarts.get(shoppingCarts.size()-1).getDatePurchase())) && shoppingCartPosition==-1) ){
                priceEd.setVisibility(View.INVISIBLE);
                priceTextTv.setVisibility(View.INVISIBLE);
                updateImgV.setVisibility(View.INVISIBLE);
                addItemTextTv.setVisibility(View.INVISIBLE);
                addItemImgV.setVisibility(View.INVISIBLE);
            }
            else{
                priceEd.setVisibility(View.VISIBLE);
                updateImgV.setVisibility(View.VISIBLE);
                addItemTextTv.setVisibility(View.VISIBLE);
                priceTextTv.setVisibility(View.VISIBLE);
                addItemImgV.setVisibility(View.VISIBLE);
            }

            if (!update && shoppingCarts.size()>0) {
                int size = shoppingCarts.size() - 1;
                //Get in to this page or from drawer or from save new shopping-cart
                if (shoppingCartPosition == -1 && todayDate.equals(shoppingCarts.get(size).getDatePurchase())) {
                    shoppingCartPosition = size;
                    priceEd.setHint(shoppingCarts.get(size).getTotalPrice() + "₪");
                    myMarketListViewModel.getGeneralData(size, items);
                    adapter.notifyDataSetChanged();
                }
                //Get in to this page from all shopping-carts list
                else if (shoppingCartPosition != -1) {
                    if(todayDate.equals(shoppingCarts.get(shoppingCartPosition).getDatePurchase())) {
                        addItemTextTv.setVisibility(View.VISIBLE);
                        addItemImgV.setVisibility(View.VISIBLE);
                    }
                    else {
                        addItemTextTv.setVisibility(View.INVISIBLE);
                        addItemImgV.setVisibility(View.INVISIBLE);
                    }
                    myMarketListViewModel.getGeneralData(shoppingCartPosition, items);
                    priceEd.setHint(shoppingCarts.get(shoppingCartPosition).getTotalPrice() + "₪");
                    adapter.notifyDataSetChanged();
                }
            }else if(update && shoppingCarts.size()>0) {
                myMarketListViewModel.getGeneralData(shoppingCartPosition, items);
                adapter.notifyDataSetChanged();
            }}
    }
    //--------------------------------------------------------RecyclerView delete by swiped left---------------------------------------------------------------------------
    //source:   https://youtu.be/rcSNkSJ624U

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    myMarketListViewModel.list.remove(position);
                    adapter.notifyItemRemoved(position);
                    break;
//                case ItemTouchHelper.RIGHT:
//                    break;

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(),R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.delete_icon)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    //------------------------------------------------------------------Update shopping cart price------------------------------------------------------------------------------------------
    private void updatePrice() {
        if(priceEd.getText().toString()!=null && !(priceEd.getText().toString().equals(""))) {
            ShoppingCart shoppingCart = myMarketListViewModel.getShoppingCartData().getValue().get(shoppingCartPosition);
            shoppingCart.setTotalPrice(priceEd.getText().toString());
            Model.instance.saveShoppingCart(shoppingCart,()->{
                Toast.makeText(getContext(), "המחיר עודכן בהצלחה!", Toast.LENGTH_LONG).show();
                update=true;
                priceEd.setText("");
                priceEd.setHint(myMarketListViewModel.getShoppingCartData().getValue().get(shoppingCartPosition).getTotalPrice()+"₪");
            });
        }
    }

    //------------------------------------------------------------------SwipeRefreshLayout------------------------------------------------------------------------------------------

    private void setUpProgressListener() {
        Model.instance.loadingState.observe(getViewLifecycleOwner(),(state)->{
            switch(state){
                case loaded:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case loading:
                    swipeRefreshLayout.setRefreshing(true);
                    break;
            }
        });
    }

    //------------------------------------------------------------------RecyclerView------------------------------------------------------------------------------------------

    static class MyViewHolder extends RecyclerView.ViewHolder{
        OnItemClickListener listener;
        TextView nameTv;
        ImageView imageV;
        TextView countTv;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.my_market_list_row_Name_textView);
            imageV = itemView.findViewById(R.id.my_market_list_row_image_imgV);
            countTv = itemView.findViewById(R.id.my_market_list_row_number_tv);

            this.listener=listener;
            itemView.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onClick(position);
                    }
                }
            });
        }

        public void bind(Item item){
            countTv.setText(item.getCount());
            nameTv.setText(item.getName());
            imageV.setImageResource(R.drawable.background2);
            if(item.getImage()!=null && !item.getImage().equals("")){
                Picasso.get().load(item.getImage()).placeholder(R.drawable.background2).into(imageV);
            }
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
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= getLayoutInflater().inflate(R.layout.my_market_list_row,parent,false);
            MyViewHolder holder= new MyViewHolder(view,listener);
            return holder;
        }
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Item item = myMarketListViewModel.list.get(position);
            holder.bind(item);
        }
        @Override
        public int getItemCount() {
            return myMarketListViewModel.list.size();
        }
    }
}