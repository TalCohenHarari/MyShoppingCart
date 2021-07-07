package com.example.mymarketlist.ui.addItemToExistShoppingCart;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymarketlist.MyApplication;
import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Category;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class AddItemToExistShoppingCartFragment extends Fragment {


    View view;
    AddItemToExistShoppingCartViewModel addItemToExistShoppingCartViewModel;
    int shoppingCartPosition;
    String shoppingCartId;
    MyAdapter adapter;
    CategoryAdapter categoryAdapter;
    EditText searchBoxEt;
    Button saveBtn;
    static Map<String,Item> tempList;
    int  selectedItem = 0;
    int selectedCategory = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize
        view = inflater.inflate(R.layout.fragment_add_item_to_exist_shopping_cart, container, false);
        saveBtn = view.findViewById(R.id.AddItemToExistShoppingCartList_save_btn);
        searchBoxEt = view.findViewById(R.id.AddItemToExistShoppingCartList_searchBox_Et);
        tempList = new HashMap<>();
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );


        //ViewModel
        addItemToExistShoppingCartViewModel  = new ViewModelProvider(this).get(AddItemToExistShoppingCartViewModel.class);
        addItemToExistShoppingCartViewModel.getShoppingCartData().observe(getViewLifecycleOwner(), shoppingCarts -> {shoppingCartId=shoppingCarts.get(shoppingCartPosition).getId();});
        addItemToExistShoppingCartViewModel.getData().observe(getViewLifecycleOwner(),
                (data)->{
                    addItemToExistShoppingCartViewModel.getGeneralData();
                    adapter.notifyDataSetChanged();
                });

        //Bundle
        shoppingCartPosition = AddItemToExistShoppingCartFragmentArgs.fromBundle(getArguments()).getShoppingCartPosition();


        //items RecyclerView:
        RecyclerView itemsList = view.findViewById(R.id.addItemToExistShoppingCartList_RecyclerView);
        itemsList.setHasFixedSize(true);
//        GridLayoutManager manager = new GridLayoutManager(MyApplication.context,2,GridLayoutManager.VERTICAL,false);
        LinearLayoutManager manager = new LinearLayoutManager(MyApplication.context,RecyclerView.HORIZONTAL,true);
        itemsList.setLayoutManager(manager);
        adapter = new MyAdapter();
        itemsList.setAdapter(adapter);

        //category RecyclerView:
        RecyclerView categoryList = view.findViewById(R.id.AddItemToExistShoppingCartListCategories_RecyclerView);
        categoryList.setHasFixedSize(true);
//        GridLayoutManager manager = new GridLayoutManager(MyApplication.context,2,GridLayoutManager.VERTICAL,false);
        LinearLayoutManager categoryManager = new LinearLayoutManager(MyApplication.context,RecyclerView.HORIZONTAL,true);
        categoryList.setLayoutManager(categoryManager);
        categoryAdapter = new CategoryAdapter();
        categoryList.setAdapter(categoryAdapter);

        //Listeners
        searchBox();

        adapter.setOnClickListener(position -> {
            selectedItem=position;
            adapter.notifyDataSetChanged();
        });

        categoryAdapter.setOnClickListener((position) -> {
            //visual
            selectedCategory=position;
            selectedItem = 0;
            categoryAdapter.notifyDataSetChanged();
            //filter items data
            addItemToExistShoppingCartViewModel.filterDataByCategory(position);
            adapter.notifyDataSetChanged();
        });

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
                for (Item i:addItemToExistShoppingCartViewModel.getData().getValue())
                    if(i.getName().contains(s.toString()) && i.getOwner().equals(""))
                        searchList.add(i);

                addItemToExistShoppingCartViewModel.list=searchList;
                adapter.notifyDataSetChanged();

            }

        });
    }

    //-------------------------------------------Save----------------------------------------
    private void save() {

        if(tempList.size()>0) {

            for (Map.Entry<String, Item> itemEntry : tempList.entrySet()) {

                    String key = itemEntry.getKey();
                    tempList.get(key).setOwner(shoppingCartId);
                    Item newItem = new Item(tempList.get(key));
                    Model.instance.saveItem(newItem, () -> {});
            }
            Navigation.findNavController(view).navigate(R.id.nav_myMarketListFragment);
        }
    }


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

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.itemsListrow_Name_textView);
            imageV = itemView.findViewById(R.id.itemsListrow_image_imgV);
            plusTv = itemView.findViewById(R.id.itemsListrow_plus_btn);
            minusTv = itemView.findViewById(R.id.itemsListrow_minus_btn);
            countTv = itemView.findViewById(R.id.itemsListrow_number_tv);
            cardView = itemView.findViewById(R.id.itemsListrow_cardView);
            linearLayout = itemView.findViewById(R.id.itemsListrow_linearLayout);

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
            Item item = addItemToExistShoppingCartViewModel.list.get(position);
            holder.bind(item);

            if(selectedItem==position)
            {
                holder.cardView.animate().scaleX(1.1f);
                holder.cardView.animate().scaleY(1.1f);
                holder.nameTv.setTextColor(getResources().getColor(R.color.black));
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
                holder.plusTv.setBackgroundResource(R.drawable.add_icon);
                holder.minusTv.setBackgroundResource(R.drawable.minus_icon);
                holder.countTv.setTextColor(getResources().getColor(R.color.black));
            }
            else{
                holder.cardView.animate().scaleX(1f);
                holder.cardView.animate().scaleY(1f);
                holder.nameTv.setTextColor(getResources().getColor(R.color.white));
                holder.plusTv.setBackgroundResource(R.drawable.white_add_icon);
                holder.minusTv.setBackgroundResource(R.drawable.white_minus_icon);
                holder.countTv.setTextColor(getResources().getColor(R.color.white));
                holder.linearLayout.setBackgroundColor(holder.linearLayout.getDrawingCacheBackgroundColor());
            }
        }
        //Give me the items count:
        @Override
        public int getItemCount() {
            return addItemToExistShoppingCartViewModel.list.size();
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
            Category category = addItemToExistShoppingCartViewModel.categoryList.get(position);
            holder.bind(category);

            if(selectedCategory==position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    holder.cardView.setOutlineSpotShadowColor(getResources().getColor(R.color.myBlue));
                    holder.cardView.setOutlineAmbientShadowColor(getResources().getColor(R.color.myBlue));
                }
                holder.cardView.setStrokeWidth(2);
                holder.cardView.setStrokeColor(getResources().getColor(R.color.myBlue));
                holder.nameTv.setTextColor(getResources().getColor(R.color.myBlue));
                holder.imageV.setColorFilter(ContextCompat.getColor(getContext(),R.color.myBlue), PorterDuff.Mode.SRC_IN);
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
            return addItemToExistShoppingCartViewModel.categoryList.size();
        }
    }
}