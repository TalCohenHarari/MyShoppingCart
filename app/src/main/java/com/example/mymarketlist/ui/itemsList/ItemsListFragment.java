package com.example.mymarketlist.ui.itemsList;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mymarketlist.MyApplication;
import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Category;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.model.ShoppingCart;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ItemsListFragment extends Fragment {

    View view;
    private ItemsListViewModel itemsListViewModel;
    MyAdapter adapter;
    CategoryAdapter categoryAdapter;
    ImageView newItem;
    EditText searchBoxEt;
    Button saveBtn;
    static Map<String,Item> tempList;
    int  selectedItem = 0;
    int selectedCategory = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initialise Params
        view = inflater.inflate(R.layout.fragment_items_list, container, false);
        newItem = view.findViewById(R.id.itemsList_add_imgBtn);
        saveBtn = view.findViewById(R.id.itemsList_save_btn);
        searchBoxEt = view.findViewById(R.id.itemsList_searchBox_Et);
        tempList = new HashMap<>();
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );

        //ViewModel
        itemsListViewModel  = new ViewModelProvider(this).get(ItemsListViewModel.class);
        itemsListViewModel.getData().observe(getViewLifecycleOwner(),
                (data)->{

                        itemsListViewModel.getGeneralData();
                        adapter.notifyDataSetChanged();
                });

        //items RecyclerView:
        RecyclerView itemsList = view.findViewById(R.id.itemsList_RecyclerView);
        itemsList.setHasFixedSize(true);
//        GridLayoutManager manager = new GridLayoutManager(MyApplication.context,2,GridLayoutManager.VERTICAL,false);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.context,RecyclerView.HORIZONTAL,true);
        itemsList.setLayoutManager(manager);
        adapter = new MyAdapter();
        itemsList.setAdapter(adapter);

        //category RecyclerView:
        RecyclerView categoryList = view.findViewById(R.id.itemsListCategories_RecyclerView);
        categoryList.setHasFixedSize(true);
//        GridLayoutManager manager = new GridLayoutManager(MyApplication.context,2,GridLayoutManager.VERTICAL,false);
        LinearLayoutManager categoryManager = new LinearLayoutManager(MyApplication.context,RecyclerView.HORIZONTAL,true);
        categoryList.setLayoutManager(categoryManager);
        categoryAdapter = new CategoryAdapter();
        categoryList.setAdapter(categoryAdapter);

        //Listeners
        newItem.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.nav_newItemFragment));
        searchBox();

        adapter.setOnClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                selectedItem=position;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onEditIconClick(int position) {
                String itemId = itemsListViewModel.list.get(position).getId();
                ItemsListFragmentDirections.ActionNavItemsListFragmentToEditItemFragment
                        action = ItemsListFragmentDirections.actionNavItemsListFragmentToEditItemFragment(itemId);
                Navigation.findNavController(view).navigate(action);
            }
        });
        categoryAdapter.setOnClickListener((position) -> {
            //visual
            selectedCategory=position;
            selectedItem = 0;
            categoryAdapter.notifyDataSetChanged();
            //filter items data
            itemsListViewModel.filterDataByCategory(position);
            adapter.notifyDataSetChanged();
        });
//        setUpProgressListener();
        saveBtn.setOnClickListener(v->save());

        return view;
    }

    //------------------------------------------Search Box----------------------------------------

    private void searchBox() {

        searchBoxEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Item> searchList = new LinkedList<>();
                for (Item i:itemsListViewModel.getData().getValue())
                    if(i.getName().contains(s.toString()) && i.getOwner().equals(""))
                        searchList.add(i);

                itemsListViewModel.list=searchList;
                adapter.notifyDataSetChanged();

            }

        });
    }

    //-------------------------------------------Save----------------------------------------
    static int i;
    private void save() {

        i=1;
        if(tempList.size()>0) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setId(UUID.randomUUID().toString());
            shoppingCart.setDatePurchase("");
            shoppingCart.setDeleted(false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime now = LocalDateTime.now();
                shoppingCart.setDatePurchase(dtf.format(now));
            }
            Model.instance.saveShoppingCart(shoppingCart, () -> {
                for (Map.Entry<String, Item> itemEntry : tempList.entrySet()) {
                    String key = itemEntry.getKey();
                    tempList.get(key).setOwner(shoppingCart.getId());
                    Item newItem = new Item(tempList.get(key));
                    Model.instance.saveItem(newItem, () -> {
                        if(i==tempList.size())
                            Navigation.findNavController(view).navigate(R.id.nav_myMarketListFragment);
                        else
                            i++;
                    });
                }
            });
        }
    }

//    private void setUpProgressListener() {
//        Model.instance.loadingState.observe(getViewLifecycleOwner(),(state)->{
//            switch(state){
//                case loaded:
//                    break;
//                case loading:
//                    break;
//            }
//        });
//    }

    //-------------------------------------------Items LIST----------------------------------------
    static class MyViewHolder extends RecyclerView.ViewHolder{
        OnItemClickListener listener;
        TextView nameTv;
        ImageView imageV;
        TextView plusTv;
        TextView minusTv;
        TextView countTv;
        CardView cardView;
        LinearLayout linearLayout;
        Item item;
        ImageView editIconImgV;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.itemsListrow_Name_textView);
            imageV = itemView.findViewById(R.id.itemsListrow_image_imgV);
            plusTv = itemView.findViewById(R.id.itemsListrow_plus_btn);
            minusTv = itemView.findViewById(R.id.itemsListrow_minus_btn);
            countTv = itemView.findViewById(R.id.itemsListrow_number_tv);
            cardView = itemView.findViewById(R.id.itemsListrow_cardView);
            linearLayout = itemView.findViewById(R.id.itemsListrow_linearLayout);
            editIconImgV= itemView.findViewById(R.id.itemsListrow_edit_icon_tv);

            plusTv.setOnClickListener(v->{
                Integer temp = Integer.parseInt(countTv.getText().toString()) +1;
                countTv.setText(temp.toString());
                item.setCount(countTv.getText().toString());
                tempList.put(item.getName(),item);

            });
            minusTv.setOnClickListener(v->{
                Integer temp = Integer.parseInt(countTv.getText().toString()) -1;
                if(temp>=0)
                    countTv.setText(temp.toString());
                else
                    countTv.setText("0");
                item.setCount(countTv.getText().toString());

                if(temp>0)
                    tempList.put(item.getName(),item);
                else
                    tempList.remove(item.getName());
            });

            this.listener=listener;
            itemView.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onClick(position);
                    }
                }
            });
            editIconImgV.setOnClickListener(v -> {
                if(listener!=null){
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onEditIconClick(position);
                    }
                }
            });
        }

        public void bind(Item item){
            countTv.setText(item.getCount());
            nameTv.setText(item.getName());
            imageV.setImageResource(R.drawable.chef);
            if(item.getImage()!=null && !item.getImage().equals("")){
                Picasso.get().load(item.getImage()).placeholder(R.drawable.chef).into(imageV);
            }
            this.item=item;
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
        void onEditIconClick(int position);


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
            View view= getLayoutInflater().inflate(R.layout.items_list_row,parent,false);
            MyViewHolder holder= new MyViewHolder(view,listener);
            return holder;
        }
        // make the variables bind to the created view from "onCreateViewHolder" function:
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Item item = itemsListViewModel.list.get(position);
            holder.bind(item);

            if(selectedItem==position)
            {
                holder.cardView.animate().scaleX(1.1f);
                holder.cardView.animate().scaleY(1.1f);
//                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
                holder.nameTv.setTextColor(getResources().getColor(R.color.black));
                holder.plusTv.setBackgroundResource(R.drawable.add_icon);
                holder.minusTv.setBackgroundResource(R.drawable.minus_icon);
                holder.countTv.setTextColor(getResources().getColor(R.color.black));
                holder.editIconImgV.setImageResource(R.drawable.edit_icon_black);
            }
            else{
                holder.cardView.animate().scaleX(1f);
                holder.cardView.animate().scaleY(1f);
//                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
                holder.nameTv.setTextColor(getResources().getColor(R.color.white));
                holder.plusTv.setBackgroundResource(R.drawable.white_add_icon);
                holder.minusTv.setBackgroundResource(R.drawable.white_minus_icon);
                holder.countTv.setTextColor(getResources().getColor(R.color.white));
//                holder.linearLayout.setBackgroundColor(holder.linearLayout.getDrawingCacheBackgroundColor());
                holder.editIconImgV.setImageResource(R.drawable.edit_icon);
            }
        }
        //Give me the items count:
        @Override
        public int getItemCount() {
            return itemsListViewModel.list.size();
        }
    }

    //-------------------------------------------Categories LIST----------------------------------------

    static class CategoryViewHolder extends RecyclerView.ViewHolder{
        OnCategoryClickListener listener;
        TextView nameTv;
        ImageView imageV;
        MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView, OnCategoryClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.categoryRow_name_tv);
            imageV = itemView.findViewById(R.id.categoryRow_image_imgV);

            cardView = itemView.findViewById(R.id.category_MaterialcardView);

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

        public void bind(Category category){
            nameTv.setText(category.getName());
            imageV.setImageResource(category.getImage());
        }
    }

    public interface OnCategoryClickListener{
        void onClick(int position);

    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
        OnCategoryClickListener listener;
        public void setOnClickListener(OnCategoryClickListener listener){
            this.listener=listener;
        }
        //Create a viewHolder for the view:
        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= getLayoutInflater().inflate(R.layout.category_list_row,parent,false);
            CategoryViewHolder holder= new CategoryViewHolder(view,listener);
            return holder;
        }
        // make the variables bind to the created view from "onCreateViewHolder" function:
        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = itemsListViewModel.categoryList.get(position);
            holder.bind(category);

            if(selectedCategory==position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    holder.cardView.setOutlineSpotShadowColor(getResources().getColor(R.color.my_brown));
                    holder.cardView.setOutlineAmbientShadowColor(getResources().getColor(R.color.my_brown));
                }
                holder.cardView.setStrokeWidth(2);
                holder.cardView.setStrokeColor(getResources().getColor(R.color.my_brown));
                holder.nameTv.setTextColor(getResources().getColor(R.color.my_brown));
                holder.imageV.setColorFilter(ContextCompat.getColor(getContext(),R.color.my_brown), PorterDuff.Mode.SRC_IN);
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    holder.cardView.setOutlineSpotShadowColor(getResources().getColor(R.color.black));
                    holder.cardView.setOutlineAmbientShadowColor(getResources().getColor(R.color.black));
                }
                holder.cardView.setStrokeWidth(0);
                holder.cardView.setStrokeColor(getResources().getColor(R.color.black));
                holder.nameTv.setTextColor(getResources().getColor(R.color.black));
                holder.imageV.setColorFilter(ContextCompat.getColor(getContext(),R.color.black), PorterDuff.Mode.SRC_IN);
            }
        }
        //Give me the items count:
        @Override
        public int getItemCount() {
            return itemsListViewModel.categoryList.size();
        }
    }
}