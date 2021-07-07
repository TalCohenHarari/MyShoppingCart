package com.example.mymarketlist.ui.editItemn;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;
import com.example.mymarketlist.ui.itemsList.ItemsListViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class EditItemFragment extends Fragment {

    View view;
    EditText nameEt;
    TextView categoryTextTv;
    Button categoryBtn;
    ImageView galleryIcon;
    ImageView cameraIcon;
    Button saveBtn;
    Button backBtn;
    ImageView imageV;
    Bitmap imageBitmap;
    Item item;
    Dialog dialog;
    Dialog loadingDialog;
    EditItemViewModel editItemViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialise
        view = inflater.inflate(R.layout.fragment_edit_item, container, false);
        nameEt = view.findViewById(R.id.editItem_name_et);
        categoryBtn = view.findViewById(R.id.editItem_category_btn);
        categoryTextTv = view.findViewById(R.id.editItem_category_tv);
        galleryIcon = view.findViewById(R.id.editItem_galleryIcon_imgV);
        cameraIcon = view.findViewById(R.id.editItem_cameraIcon_imgV);
        saveBtn = view.findViewById(R.id.editItem_save_btn);
        backBtn = view.findViewById(R.id.editItem_back_btn);
        imageV = view.findViewById(R.id.editItem_image_imgV);
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );
        loadingDialog = new Dialog(getContext());

        //Bundle
        String itemId = EditItemFragmentArgs.fromBundle(getArguments()).getItemId();

        //ViewModel
        editItemViewModel  = new ViewModelProvider(this).get(EditItemViewModel.class);
        editItemViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<Item>>() {
            @Override
            public void onChanged(List<Item> items) {
                editItemViewModel.getItem(itemId);
                nameEt.setText(editItemViewModel.item.getName());
                categoryTextTv.setText(editItemViewModel.item.getCategory());
                if(editItemViewModel.item.getImage()!=null && !editItemViewModel.item.getImage().equals("")){
                    Picasso.get().load(editItemViewModel.item.getImage()).placeholder(R.drawable.chef).into(imageV);
                }
            }
        });

        //Listeners
        saveBtn.setOnClickListener(v -> saveItem());
        backBtn.setOnClickListener(v-> Navigation.findNavController(view).navigateUp());
        cameraIcon.setOnClickListener(v->takePicture());
        galleryIcon.setOnClickListener(v->takePictureFromGallery());
        categoryBtn.setOnClickListener(v->categoryDialog());

        return view;
    }

    private void categoryDialog() {

        dialog   = new Dialog(getContext());
        dialog.setContentView(R.layout.category_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;

        //Initialize params
        TextView freezer = dialog.findViewById(R.id.category_dialog_freezer);
        TextView vegetables = dialog.findViewById(R.id.category_dialog_vegetables);
        TextView clean = dialog.findViewById(R.id.category_dialog_clean);
        TextView cabinet = dialog.findViewById(R.id.category_dialog_cabinet);
        popupLoadingDialog();

        //Listeners
        freezer.setOnClickListener(v->{categoryTextTv.setText(freezer.getText().toString()); dialog.dismiss();});
        vegetables.setOnClickListener(v->{categoryTextTv.setText(vegetables.getText().toString()); dialog.dismiss();});
        clean.setOnClickListener(v->{categoryTextTv.setText(clean.getText().toString()); dialog.dismiss();});
        cabinet.setOnClickListener(v->{categoryTextTv.setText(cabinet.getText().toString()); dialog.dismiss();});

        dialog.show();

    }

    //----------------------------------------- Loading Dialog -----------------------------------------
    private void popupLoadingDialog() {

        loadingDialog.setContentView(R.layout.popup_dialog_loading);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            loadingDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;
        ProgressBar pb = loadingDialog.findViewById(R.id.loading_progressBar_pb);
        pb.setVisibility(View.VISIBLE);

    }

    private void saveItem() {

        if(!(categoryTextTv.getText().toString().isEmpty()) &&!(nameEt.getText().toString().isEmpty())) {
            Item item= editItemViewModel.item;
            item.setName(nameEt.getText().toString());
            item.setCategory(categoryTextTv.getText().toString());
            loadingDialog.show();
            Model.instance.saveItem(item, () -> {
                if (imageBitmap != null) {
                    Model.instance.uploadImage(imageBitmap, item.getId(), new Model.UpLoadImageListener() {
                        @Override
                        public void onComplete(String url) {
                            item.setImage(url);
                            Model.instance.saveItem(item, () -> {
                                loadingDialog.dismiss();
                                Navigation.findNavController(view).navigateUp();
                            });
                        }
                    });
                } else {
                    loadingDialog.dismiss();
                    Navigation.findNavController(view).navigateUp();
                }
            });
        }

    }

    //---------------------------------Image-------------------------------------------------
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int GALLERY_IMAGE = 0;
    final static int RESULT_SUCCESS = -1;

    void takePicture()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    void takePictureFromGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode == RESULT_SUCCESS)
        {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageV.setImageBitmap(imageBitmap);
        }
        else if(requestCode == GALLERY_IMAGE && resultCode == RESULT_SUCCESS)
        {
            Uri selectedImage = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
            } catch (IOException e) {}
            imageV.setImageBitmap(imageBitmap);
        }
    }
}