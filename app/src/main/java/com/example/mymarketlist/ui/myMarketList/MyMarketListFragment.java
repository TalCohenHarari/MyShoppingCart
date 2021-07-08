package com.example.mymarketlist.ui.myMarketList;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
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
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class MyMarketListFragment extends Fragment implements PopupMenu.OnMenuItemClickListener{

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
    ImageView shareIconBtn;
    Integer noteRowPosition;
    Dialog noteDialog;
    Dialog noteReadOnlyDialog;
    LayoutAnimationController layoutAnimationController;

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
        noteDialog = new Dialog(getContext());
        noteReadOnlyDialog = new Dialog(getContext());
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
        myMarketListViewModel.getData().observe(getViewLifecycleOwner(), items -> {
            initData(items);
            recyclerView.setLayoutAnimation(layoutAnimationController);
            adapter.notifyDataSetChanged();
        });
        myMarketListViewModel.getShoppingCartData().observe(getViewLifecycleOwner(), new Observer<List<ShoppingCart>>() {@Override public void onChanged(List<ShoppingCart> shoppingCarts){ }});


        //RecyclerView:
        recyclerView = view.findViewById(R.id.myMarketList_RecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.context);
        recyclerView.setLayoutManager(manager);
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        /*RecyclerView Animation:
        https://youtu.be/5PMI_bHGehg*/
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_animation_slide_right);


        //Listeners
        swipeRefreshLayout.setOnRefreshListener(()->{
            myMarketListViewModel.refresh();
//            recyclerView.setLayoutAnimation(layoutAnimationController);
//            adapter.notifyDataSetChanged();
        });
        updateImgV.setOnClickListener(v->updatePrice());
        adapter.setOnClickListener(new OnItemClickListener() {@Override public void onClick(int position) {}
            @Override
            public void onNoteClick(int position,ImageView note) {
                noteRowPosition=position;
                showPopUp(note);
            }

            @Override
            public void onNoteLongClick(int position, ImageView note) {
                noteRowPosition=position;
                popupNoteReadDialog(noteRowPosition);
            }
        });
        setUpProgressListener();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        shareIconBtn.setOnClickListener(v->shareMyShoppingCart());
        addItemImgV.setOnClickListener(v->{
            MyMarketListFragmentDirections.ActionNavMyMarketListFragmentToNavAddItemToExistShoppingCartFragment
                    action = MyMarketListFragmentDirections.actionNavMyMarketListFragmentToNavAddItemToExistShoppingCartFragment().setShoppingCartPosition(shoppingCartPosition);
            Navigation.findNavController(view).navigate(action);
        });

        return view;
    }
    //-------------------------------------------------------Pop up a little menu for adding some item note or delete exist note---------------------------------------

    //https://www.youtube.com/watch?v=s1fW7CpiB9c&ab_channel=CodinginFlow
    public void showPopUp(View v){
        PopupMenu popup = new PopupMenu(getContext(),v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_popup);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.popupMenu_create_edit:
                popupNoteDialog(noteRowPosition);
                break;
            case R.id.popupMenu_delete:
                Item foodItem = myMarketListViewModel.list.get(noteRowPosition);
                foodItem.setNote("");
                Model.instance.saveItem(foodItem,()->{});
                break;
        }
        return false;
    }

    private void popupNoteDialog(int noteRowPosition) {

        noteDialog.setContentView(R.layout.popup_dialog_item_note);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            noteDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        noteDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        noteDialog.setCancelable(true);
        noteDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;

        //Params
        EditText note = noteDialog.findViewById(R.id.note_text_et);
        ImageView checked = noteDialog.findViewById(R.id.note_checked);
        ImageView cancel = noteDialog.findViewById(R.id.note_cencel);
        Item item = myMarketListViewModel.list.get(noteRowPosition);
        note.setText(item.getNote());

        //Listeners
        checked.setOnClickListener(v->{
            item.setNote(note.getText().toString());
            Model.instance.saveItem(item,()->{noteDialog.dismiss();});
        });
        cancel.setOnClickListener(v->noteDialog.dismiss());

        noteDialog.show();

    }
    //-------------------------------------------------------Read note----------------------------------------------------------------------

    private void popupNoteReadDialog(int noteRowPosition) {

         noteReadOnlyDialog.setContentView(R.layout.popup_dialog_item_note_read_only);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
             noteReadOnlyDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        noteReadOnlyDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteReadOnlyDialog.setCancelable(true);
        noteReadOnlyDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;

         //Params
         ImageView cancel = noteReadOnlyDialog.findViewById(R.id.note_cencel);
        TextView note = noteReadOnlyDialog.findViewById(R.id.note_read_only_text_et);
        Item item = myMarketListViewModel.list.get(noteRowPosition);
        note.setText(item.getNote());

         //Listeners
         cancel.setOnClickListener(v -> noteReadOnlyDialog.dismiss());
        noteReadOnlyDialog.show();
     }

    //-------------------------------------------------------Share My Shopping Cart----------------------------------------------------------------------
    private void shareMyShoppingCart() {
        StringBuilder text = new StringBuilder();
        text.append(" ("+ myMarketListViewModel.getShoppingCartData().getValue().get(shoppingCartPosition).getDatePurchase() +")");
        text.append("רשימת סופר שבועית:");
        text.append("\n\n");
        String note="";
        for (Item item: myMarketListViewModel.list) {
            if(!(item.getNote().isEmpty()))
                note = " (" + item.getNote() + ")";
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
                shareIconBtn.setVisibility(View.INVISIBLE);
            }
            else{
                priceEd.setVisibility(View.VISIBLE);
                updateImgV.setVisibility(View.VISIBLE);
                addItemTextTv.setVisibility(View.VISIBLE);
                priceTextTv.setVisibility(View.VISIBLE);
                addItemImgV.setVisibility(View.VISIBLE);
                shareIconBtn.setVisibility(View.VISIBLE);
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
        //https://youtu.be/fqU4zc_XeX0
        Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.mixed_anim);
        if(priceEd.getText().toString()!=null && !(priceEd.getText().toString().equals(""))) {
            updateImgV.startAnimation(animation);
            ShoppingCart shoppingCart = myMarketListViewModel.getShoppingCartData().getValue().get(shoppingCartPosition);
            shoppingCart.setTotalPrice(priceEd.getText().toString());
            Model.instance.saveShoppingCart(shoppingCart,()->{
                Toast.makeText(getContext(), "המחיר עודכן בהצלחה!", Toast.LENGTH_LONG).show();
                updateImgV.clearAnimation();
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
//                    swipeRefreshLayout.setRefreshing(true);
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
        ImageView noteImgV;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.my_market_list_row_Name_textView);
            imageV = itemView.findViewById(R.id.my_market_list_row_image_imgV);
            countTv = itemView.findViewById(R.id.my_market_list_row_number_tv);
            noteImgV = itemView.findViewById(R.id.my_market_list_row_note_imgV);
            this.listener=listener;

            itemView.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onClick(position);
                    }
                }
            });
            noteImgV.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onNoteClick(position,noteImgV);
                    }
                }
            });
            noteImgV.setOnLongClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onNoteLongClick(position,noteImgV);
                    }
                }
                return false;
            });

        }

        public void bind(Item item){
            countTv.setText(item.getCount());
            nameTv.setText(item.getName());
            imageV.setImageResource(R.drawable.chef);
            if(item.getImage()!=null && !item.getImage().equals("")){
                Picasso.get().load(item.getImage()).placeholder(R.drawable.chef).into(imageV);
            }
        }
    }



    public interface OnItemClickListener{
        void onClick(int position);
        void onNoteClick(int position,ImageView note);
        void onNoteLongClick(int position,ImageView note);

    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        OnItemClickListener listener;

        public void setOnClickListener(OnItemClickListener listener){
            this.listener=listener;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view= getLayoutInflater().inflate(R.layout.my_market_list_row,parent,false);
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_market_list_row,parent,false);
            MyViewHolder holder= new MyViewHolder(view,listener);
            return holder;
        }
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Item item = myMarketListViewModel.list.get(position);
            holder.bind(item);
            if(item.getNote()==null || item.getNote().equals(""))
                holder.noteImgV.setImageResource(R.drawable.note_gray_icon);
            else
                holder.noteImgV.setImageResource(R.drawable.note_red_icon);
        }
        @Override
        public int getItemCount() {
            return myMarketListViewModel.list.size();
        }
    }
}