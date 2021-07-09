package com.example.mymarketlist.ui.newItem;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.SystemClock;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NewItemFragment extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialise
        view = inflater.inflate(R.layout.fragment_new_item, container, false);
        nameEt = view.findViewById(R.id.newItem_name_et);
        categoryBtn = view.findViewById(R.id.newItem_category_btn);
        categoryTextTv = view.findViewById(R.id.newItem_category_tv);
        galleryIcon = view.findViewById(R.id.newItem_galleryIcon_imgV);
        cameraIcon = view.findViewById(R.id.newItem_cameraIcon_imgV);
        saveBtn = view.findViewById(R.id.newItem_save_btn);
        backBtn = view.findViewById(R.id.newItem_back_btn);
        imageV = view.findViewById(R.id.newItem_image_imgV);
        view.setLayoutDirection(view.LAYOUT_DIRECTION_LTR );
        popupLoadingDialog();
        nameEt.setText("");
        categoryTextTv.setText("");

        //Listeners
        saveBtn.setOnClickListener(v->saveItem());
        backBtn.setOnClickListener(v->Navigation.findNavController(view).navigateUp());
        cameraIcon.setOnClickListener(v->takePicture());
        galleryIcon.setOnClickListener(v->takePictureFromGallery());
        categoryBtn.setOnClickListener(v->categoryDialog());
        setUpProgressListener();

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

        //Listeners
        freezer.setOnClickListener(v->{categoryTextTv.setText(freezer.getText().toString()); dialog.dismiss();});
        vegetables.setOnClickListener(v->{categoryTextTv.setText(vegetables.getText().toString()); dialog.dismiss();});
        clean.setOnClickListener(v->{categoryTextTv.setText(clean.getText().toString()); dialog.dismiss();});
        cabinet.setOnClickListener(v->{categoryTextTv.setText(cabinet.getText().toString()); dialog.dismiss();});

        dialog.show();

    }

    //----------------------------------------- Loading Dialog -----------------------------------------
    private void popupLoadingDialog() {

        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.popup_dialog_loading);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            loadingDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_dialog_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.popup_dialog_animation;
        ProgressBar pb = loadingDialog.findViewById(R.id.loading_progressBar_pb);
        pb.setVisibility(View.VISIBLE);

    }

    private void setUpProgressListener() {
        Model.instance.loadingState.observe(getViewLifecycleOwner(),(state)->{
            switch(state){
                case loaded:
                    break;
                case loading:
                    break;
            }
        });
    }

    private void saveItem() {

        if(!(categoryTextTv.getText().toString().isEmpty()) &&!(nameEt.getText().toString().isEmpty())) {
            String imageUrl = "";
            String datePurchase = "";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime now = LocalDateTime.now();
                datePurchase = dtf.format(now);
            }
            item = new Item(nameEt.getText().toString(), categoryTextTv.getText().toString(), "0",
                    datePurchase, imageUrl, false);

            loadingDialog.show();
            Model.instance.saveGeneralItem(item, () -> {
                if (imageBitmap != null) {
                    Model.instance.uploadImage(imageBitmap, item.getId(), new Model.UpLoadImageListener() {
                        @Override
                        public void onComplete(String url) {
                            item.setImage(url);
                            Model.instance.saveGeneralItem(item, () -> {
                                loadingDialog.dismiss();
                                Navigation.findNavController(view).navigateUp();});
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