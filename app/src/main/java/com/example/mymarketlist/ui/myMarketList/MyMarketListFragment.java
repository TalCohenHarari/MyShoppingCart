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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
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
import java.util.LinkedList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class MyMarketListFragment extends Fragment implements PopupMenu.OnMenuItemClickListener{

    View view;
    MyMarketListViewModel myMarketListViewModel;
    SwipeRefreshLayout swipeRefreshLayout;
    MyAdapter adapter;
    int shoppingCartPosition;
    EditText priceEd;
    ImageView updateImgV;
    RecyclerView recyclerView;
    boolean update=false;
    TextView priceTextTv;
    ImageView addItemImgV;
    ImageView shareIconBtn;
    Integer noteRowPosition;
    Dialog noteDialog;
    Dialog noteReadOnlyDialog;
    Dialog shoppingCartNoteDialog;
    LayoutAnimationController layoutAnimationController;
    ImageView listImageImgV;
    ImageView addNewListImgV;
    TextView textTv;
    Dialog npDialog;
    ImageView noteForColorVisual;
    ImageView editShoppingCartImgV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialise Params
        view = inflater.inflate(R.layout.fragment_my_market_list, container, false);
        swipeRefreshLayout = view.findViewById(R.id.myMarketList_swipeRefreshLayout);
        priceEd = view.findViewById(R.id.myMarketList_price_et);
        updateImgV = view.findViewById(R.id.myMarketList_update_icon_imgV);
        priceTextTv = view.findViewById(R.id.myMarketList_price_headLine_tv);
        shareIconBtn = view.findViewById(R.id.myMarketList_share_icon_imgB);
        addItemImgV = view.findViewById(R.id.myMarketList_addItem_imgV);
        listImageImgV = view.findViewById(R.id.myMarketList_list_image_imgV);
        addNewListImgV = view.findViewById(R.id.myMarketList_add_shopping_cart_butten_imgV);
        textTv = view.findViewById(R.id.myMarketList_text_tv);
        editShoppingCartImgV = view.findViewById(R.id.myMarketList_edit_shopping_cart_imgV);

        noteDialog = new Dialog(getContext());
        npDialog = new Dialog(getContext());
        shoppingCartNoteDialog = new Dialog(getContext());
        noteReadOnlyDialog = new Dialog(getContext());
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );


        //Bundle args
        shoppingCartPosition = MyMarketListFragmentArgs.fromBundle(getArguments()).getPosition();
        if(shoppingCartPosition==-1)
            update=false;


        //ViewModel
        myMarketListViewModel  = new ViewModelProvider(this).get(MyMarketListViewModel.class);
        myMarketListViewModel.getData().observe(getViewLifecycleOwner(), items -> {
            initData(items);
            recyclerView.setLayoutAnimation(layoutAnimationController);
//            adapter.notifyDataSetChanged();
        });
        myMarketListViewModel.getShoppingCartData().observe(getViewLifecycleOwner(), new Observer<List<ShoppingCart>>() {
            @Override
            public void onChanged(List<ShoppingCart> shoppingCarts)
            {

            }
        });


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
        setUpProgressListener();
        updateImgV.setOnClickListener(v->updatePrice());
        shareIconBtn.setOnClickListener(v->shareMyShoppingCart());
        editShoppingCartImgV.setOnClickListener(v->shoppingCartShowPopUp(editShoppingCartImgV));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        addNewListImgV.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.nav_itemsListFragment));
        addItemImgV.setOnClickListener(v->{
            int positionSend = shoppingCartPosition;
            if(positionSend==-1)
                positionSend=todayUserShoppingCartPosition;

            MyMarketListFragmentDirections.ActionNavMyMarketListFragmentToNavAddItemToExistShoppingCartFragment
                    action = MyMarketListFragmentDirections.actionNavMyMarketListFragmentToNavAddItemToExistShoppingCartFragment().setShoppingCartPosition(positionSend);
            Navigation.findNavController(view).navigate(action);
        });
        swipeRefreshLayout.setOnRefreshListener(()->{
            myMarketListViewModel.refresh();
            myMarketListViewModel.firstTimeOrInRefresh=true;
        });
        adapter.setOnClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position,ImageView checked) {
                Item item = myMarketListViewModel.tempList.get(position);
                if (!(item.isChecked())){
                    item.setChecked(true);
                    checked.setVisibility(View.VISIBLE);
                }
                else {
                    item.setChecked(false);
                    checked.setVisibility(View.INVISIBLE);
                }
                Model.instance.updateInLiveItem(item,()->{});
            }

            @Override
            public void onNoteClick(int position,ImageView note) {
                noteRowPosition=position;
                noteForColorVisual=note;
                showPopUp(note);
            }

            @Override
            public void onNoteLongClick(int position, ImageView note) {
                noteRowPosition=position;
                popupNoteReadDialog(noteRowPosition);
            }

            @Override
            public void onCountClick(int position, TextView count) {
                numberPickerDialog(position , count);
            }
        });

        //View Display Validation
        if(myMarketListViewModel.tempList!=null && myMarketListViewModel.tempList.size()!=0)
        {
            listImageImgV.setVisibility(View.INVISIBLE);
            addNewListImgV.setVisibility(View.INVISIBLE);
            textTv.setVisibility(View.INVISIBLE);
        }
        else{
            editShoppingCartImgV.setVisibility(View.INVISIBLE);
        }
        return view;
    }
    //--------------------------------------------------------Init the data to theRecyclerView and play with the view----------------------------------------------------------------------
    Integer todayUserShoppingCartPosition=null;
    private void initData(List<Item> items){

        if(myMarketListViewModel.getShoppingCartData().getValue()!=null) {

            List<ShoppingCart> shoppingCarts = myMarketListViewModel.getShoppingCartData().getValue();
            ShoppingCart todayUserShoppingCart = null;
            for (int i=0; i<shoppingCarts.size() ; i++) {
                if (shoppingCarts.get(i).getDatePurchase().equals(myMarketListViewModel.todayDate) && shoppingCarts.get(i).getUserOwner().equals(Model.instance.getUser().getId())) {
                    todayUserShoppingCartPosition=new Integer(i);
                    todayUserShoppingCart = shoppingCarts.get(i);
                    break;
                }
            }

            // If get in from new/edit/drawer shopping cart fragment
            if (shoppingCartPosition==-1)
            {
                //If user has today's shopping cart
                if(todayUserShoppingCart!=null) {
                    //Visibility
                    priceEd.setVisibility(View.VISIBLE);
                    updateImgV.setVisibility(View.VISIBLE);
                    priceTextTv.setVisibility(View.VISIBLE);
                    addItemImgV.setVisibility(View.VISIBLE);
                    shareIconBtn.setVisibility(View.VISIBLE);
                    editShoppingCartImgV.setVisibility(View.VISIBLE);
                    listImageImgV.setVisibility(View.INVISIBLE);
                    addNewListImgV.setVisibility(View.INVISIBLE);
                    textTv.setVisibility(View.INVISIBLE);

                    //Init the data
                    int size = shoppingCarts.size() - 1;
                    priceEd.setHint(todayUserShoppingCart.getTotalPrice() + "₪");
                    myMarketListViewModel.getGeneralData(todayUserShoppingCartPosition, items);
                    //For unwanted refresh when phone go to side position
                    if (myMarketListViewModel.firstTimeOrInRefresh || myMarketListViewModel.oldSize < myMarketListViewModel.list.size()) {
                        myMarketListViewModel.oldSize = myMarketListViewModel.list.size();
                        myMarketListViewModel.tempList = myMarketListViewModel.list;
                        myMarketListViewModel.firstTimeOrInRefresh = false;
                    }
                    adapter.notifyDataSetChanged();
                }
                //If user don't has today's shopping cart
                else {
                    //Visibility
                    priceEd.setVisibility(View.INVISIBLE);
                    priceTextTv.setVisibility(View.INVISIBLE);
                    updateImgV.setVisibility(View.INVISIBLE);
                    addItemImgV.setVisibility(View.INVISIBLE);
                    shareIconBtn.setVisibility(View.INVISIBLE);
                    editShoppingCartImgV.setVisibility(View.INVISIBLE);
                    listImageImgV.setVisibility(View.VISIBLE);
                    addNewListImgV.setVisibility(View.VISIBLE);
                    textTv.setVisibility(View.VISIBLE);

                    //Delete data
                    //???
                }

            }
            // If we get in from all shopping carts fragment
            else if(shoppingCartPosition!=-1) {
                //Visibility
                priceEd.setVisibility(View.VISIBLE);
                updateImgV.setVisibility(View.VISIBLE);
                priceTextTv.setVisibility(View.VISIBLE);
                addItemImgV.setVisibility(View.INVISIBLE);
                shareIconBtn.setVisibility(View.VISIBLE);
                editShoppingCartImgV.setVisibility(View.VISIBLE);
                listImageImgV.setVisibility(View.INVISIBLE);
                addNewListImgV.setVisibility(View.INVISIBLE);
                textTv.setVisibility(View.INVISIBLE);

                //Init data
                if (myMarketListViewModel.todayDate.equals(shoppingCarts.get(shoppingCartPosition).getDatePurchase())) {
                    addItemImgV.setVisibility(View.VISIBLE);
                } else {
                    addItemImgV.setVisibility(View.INVISIBLE);
                }
                myMarketListViewModel.getGeneralData(shoppingCartPosition, items);
                //For unwanted refresh when phone go to side position
                if (myMarketListViewModel.firstTimeOrInRefresh || myMarketListViewModel.oldSize < myMarketListViewModel.list.size()) {
                    myMarketListViewModel.oldSize = myMarketListViewModel.list.size();
                    myMarketListViewModel.tempList = myMarketListViewModel.list;
                    myMarketListViewModel.firstTimeOrInRefresh = false;
                }
                priceEd.setHint(shoppingCarts.get(shoppingCartPosition).getTotalPrice() + "₪");
                adapter.notifyDataSetChanged();
            }
        }
    }
    //-------------------------------------------------------Pop up a little menu for adding some item note or delete exist note---------------------------------------

    //https://www.youtube.com/watch?v=s1fW7CpiB9c&ab_channel=CodinginFlow
    public void showPopUp(View v){
        PopupMenu popup = new PopupMenu(getContext(),v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_popup);
        popup.show();
    }
    public void shoppingCartShowPopUp(View v){
        PopupMenu popup = new PopupMenu(getContext(),v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.shopping_cart_edit_icon_menu_popup);
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
                noteForColorVisual.setImageResource(R.drawable.note_gray_icon);
                foodItem.setNote("");
                Model.instance.updateInLiveItem(foodItem,()->{});
                break;
            case R.id.popupMenu_delete_for_ever:
                Item foodItemToDelete = myMarketListViewModel.tempList.get(noteRowPosition);
                foodItemToDelete.setDeleted(true);
                Model.instance.saveItem(foodItemToDelete,()->{
                    myMarketListViewModel.tempList.remove(noteRowPosition);
//                    adapter.notifyItemRemoved(noteRowPosition);
                    myMarketListViewModel.refresh();
                    myMarketListViewModel.firstTimeOrInRefresh=true;
                    adapter.notifyDataSetChanged();
                });
                break;
            case R.id.shopping_cart_popupMenu_create_edit:
                shoppingCartPopupNoteDialog();
                break;
            case R.id.shopping_cart_popupMenu_delete:
                int deleteNotePosition=shoppingCartPosition;
                if(shoppingCartPosition==-1)
                    deleteNotePosition=todayUserShoppingCartPosition;
                ShoppingCart shoppingCart = myMarketListViewModel.getShoppingCartData().getValue().get(deleteNotePosition);
                shoppingCart.setNote("");
                Model.instance.updateInLiveShoppingCart(shoppingCart,()->{});
                break;
            case R.id.shopping_cart_popupMenu_delete_shopping_cart:
                int deletePosition=shoppingCartPosition;
                if(shoppingCartPosition==-1)
                    deletePosition=todayUserShoppingCartPosition;
                ShoppingCart deletedShoppingCart = myMarketListViewModel.getShoppingCartData().getValue().get(deletePosition);
                deletedShoppingCart.setDeleted(true);
                Model.instance.saveShoppingCart(deletedShoppingCart,()->{
                    while (Navigation.findNavController(view).popBackStack());
                    Navigation.findNavController(view).navigate(R.id.nav_allMyShoppingCartsFragment);
                });
                break;
        }
        return false;
    }

    private void shoppingCartPopupNoteDialog() {
        shoppingCartNoteDialog.setContentView(R.layout.popup_dialog_item_note);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            noteDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        shoppingCartNoteDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        shoppingCartNoteDialog.setCancelable(true);
        shoppingCartNoteDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;

        //Params
        EditText note = shoppingCartNoteDialog.findViewById(R.id.note_text_et);
        ImageView checked = shoppingCartNoteDialog.findViewById(R.id.note_checked);
        ImageView cancel = shoppingCartNoteDialog.findViewById(R.id.note_cencel);
        int pos = shoppingCartPosition;
        if(pos==-1)
            pos=todayUserShoppingCartPosition;
        ShoppingCart shoppingCart = myMarketListViewModel.getShoppingCartData().getValue().get(pos);
        note.setText(shoppingCart.getNote());

        //Listeners
        checked.setOnClickListener(v->{
            shoppingCart.setNote(note.getText().toString());
            Model.instance.updateInLiveShoppingCart(shoppingCart,()->{});
            shoppingCartNoteDialog.dismiss();
        });
        cancel.setOnClickListener(v->shoppingCartNoteDialog.dismiss());

        shoppingCartNoteDialog.show();
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
            if(note.getText().toString()!=null &&!(note.getText().toString().equals(""))) {
                noteForColorVisual.setImageResource(R.drawable.note_red_icon);
            }
            else {
                noteForColorVisual.setImageResource(R.drawable.note_gray_icon);
            }

            item.setNote(note.getText().toString());
            Model.instance.updateInLiveItem(item,()->{});
            noteDialog.dismiss();
        });
        cancel.setOnClickListener(v->noteDialog.dismiss());

        noteDialog.show();

    }

    //-------------------------------------------------------Number Picker Dialog--------------------------------------------------------------------------
    private void numberPickerDialog(int position ,TextView count) {

        //get the current item on list:
        Item item = myMarketListViewModel.tempList.get(position);
        item.setCount(count.getText().toString());

        npDialog.setContentView(R.layout.number_picker_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            npDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        npDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        npDialog.setCancelable(true);
        noteDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;

        //Params
        NumberPicker np = npDialog.findViewById(R.id.number_picker_np);
        Button okButton = npDialog.findViewById(R.id.number_okButton_btn);
        ImageView cancel = npDialog.findViewById(R.id.number_cancel_imgV);
        cancel.setOnClickListener(v->npDialog.dismiss());
        np.setMinValue(0);
        np.setMaxValue(Integer.MAX_VALUE);
        np.setValue(Integer.parseInt(count.getText().toString()));

        //Listeners
        np.setOnValueChangedListener((picker, oldVal, newVal) -> {
            okButton.setOnClickListener(v->{
                count.setText(newVal+"");
                item.setCount(newVal+"");
                Model.instance.updateInLiveItem(item,()->{});
                npDialog.dismiss();
            });
        });



        npDialog.show();

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
        int share;
        if(shoppingCartPosition==-1)
            share=todayUserShoppingCartPosition;
        else
            share=shoppingCartPosition;
        text.append("רשימת סופר שבועית"+" ("+ myMarketListViewModel.getShoppingCartData().getValue().get(share).getDatePurchase() +")"+":");
        text.append("\n\n");
        String note="";
        for (Item item: myMarketListViewModel.list) {
            text.append(item.getName() + " - " + item.getCount());
            if(!(item.getNote().isEmpty())) {
                note = " (" + item.getNote() + ")";
                text.append(note + "\n");
            }
            else
                text.append("\n");
        }
        String sendText = text.toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sendText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
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
//                    myMarketListViewModel.list.remove(position);
                    myMarketListViewModel.tempList.remove(position);
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
    ShoppingCart sc = null;
    private void updatePrice() {
        //https://youtu.be/fqU4zc_XeX0
        Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.mixed_anim);
        if(priceEd.getText().toString()!=null && !(priceEd.getText().toString().equals(""))) {
            updateImgV.startAnimation(animation);
            if(shoppingCartPosition!=-1)
                 sc = myMarketListViewModel.getShoppingCartData().getValue().get(shoppingCartPosition);
            else
            {
                List<ShoppingCart> shoppingCarts = myMarketListViewModel.getShoppingCartData().getValue();

                for (int i=0; i<shoppingCarts.size() ; i++) {
                    if (shoppingCarts.get(i).getDatePurchase().equals(myMarketListViewModel.todayDate) && shoppingCarts.get(i).getUserOwner().equals(Model.instance.getUser().getId())) {
                        sc = shoppingCarts.get(i);
                        break;
                    }
                }
            }


            sc.setTotalPrice(priceEd.getText().toString());
            Model.instance.saveShoppingCart(sc,()->{
                Toast.makeText(getContext(), "המחיר עודכן בהצלחה!", Toast.LENGTH_LONG).show();
                updateImgV.clearAnimation();
                update=true;
                priceEd.setText("");
                priceEd.setHint(sc.getTotalPrice()+"₪");
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
        ImageView checkedImgV;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.my_market_list_row_Name_textView);
            imageV = itemView.findViewById(R.id.my_market_list_row_image_imgV);
            countTv = itemView.findViewById(R.id.my_market_list_row_number_tv);
            noteImgV = itemView.findViewById(R.id.my_market_list_row_note_imgV);
            checkedImgV = itemView.findViewById(R.id.my_market_list_row_checked_imgV);

            this.listener=listener;

            itemView.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onClick(position, checkedImgV);
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
            countTv.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onCountClick(position,countTv);
                    }
                }
            });

        }

        public void bind(Item item){
            countTv.setText(item.getCount());
            nameTv.setText(item.getName());
            imageV.setImageResource(R.drawable.chef);

//            if(!(item.isChecked()))
//                checkedImgV.setVisibility(View.INVISIBLE);
//            else
//                checkedImgV.setVisibility(View.VISIBLE);

            if(item.getImage()!=null && !item.getImage().equals("")){
                Picasso.get().load(item.getImage()).placeholder(R.drawable.chef).into(imageV);
            }
        }
    }



    public interface OnItemClickListener{
        void onClick(int position, ImageView checked);
        void onNoteClick(int position,ImageView note);
        void onNoteLongClick(int position,ImageView note);
        void onCountClick(int position,TextView count);

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
//            Item item = myMarketListViewModel.list.get(position);
            Item item = myMarketListViewModel.tempList.get(position);
            holder.bind(item);
            if(item.getNote()==null || item.getNote().equals(""))
                holder.noteImgV.setImageResource(R.drawable.note_gray_icon);
            else
                holder.noteImgV.setImageResource(R.drawable.note_red_icon);

            if(item.isChecked())
                holder.checkedImgV.setVisibility(View.VISIBLE);
            else
                holder.checkedImgV.setVisibility(View.INVISIBLE);

        }
        @Override
        public int getItemCount() {

//            return myMarketListViewModel.list.size();
            return myMarketListViewModel.tempList.size();
        }
    }
}