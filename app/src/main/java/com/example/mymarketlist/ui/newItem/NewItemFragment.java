package com.example.mymarketlist.ui.newItem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.example.mymarketlist.R;
import com.example.mymarketlist.model.Item;
import com.example.mymarketlist.model.Model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NewItemFragment extends Fragment {

    View view;
    EditText nameEt;
    EditText categoryEt;
    ImageView galleryIcon;
    ImageView cameraIcon;
    Button saveBtn;
    Button backBtn;
    ImageView imageV;
    Bitmap imageBitmap;
    Item item;
    ProgressBar pb;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Initialise
        view = inflater.inflate(R.layout.fragment_new_item, container, false);
        nameEt = view.findViewById(R.id.newItem_name_et);
        categoryEt = view.findViewById(R.id.newItem_category_et);
        galleryIcon = view.findViewById(R.id.newItem_galleryIcon_imgV);
        cameraIcon = view.findViewById(R.id.newItem_cameraIcon_imgV);
        saveBtn = view.findViewById(R.id.newItem_save_btn);
        backBtn = view.findViewById(R.id.newItem_back_btn);
        imageV = view.findViewById(R.id.newItem_image_imgV);
        pb = view.findViewById(R.id.newItem_progressBar);
        nameEt.setText("");

        //Listeners
        saveBtn.setOnClickListener(v->saveItem());
        backBtn.setOnClickListener(v->Navigation.findNavController(view).navigateUp());
        cameraIcon.setOnClickListener(v->takePicture());
        galleryIcon.setOnClickListener(v->takePictureFromGallery());
        setUpProgressListener();

        return view;
    }

    private void setUpProgressListener() {
        Model.instance.loadingState.observe(getViewLifecycleOwner(),(state)->{
            switch(state){
                case loaded:
                    pb.setVisibility(View.GONE);
                    break;
                case loading:
                    pb.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void saveItem() {

        String imageUrl="";
        String datePurchase="";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime now = LocalDateTime.now();
            datePurchase = dtf.format(now);
        }
        item = new Item(nameEt.getText().toString(),categoryEt.getText().toString(),"0",
                datePurchase,imageUrl,false);


        Model.instance.saveItem(item,()->{
            if(imageBitmap!=null) {
                Model.instance.uploadImage(imageBitmap, item.getId(), new Model.UpLoadImageListener() {
                    @Override
                    public void onComplete(String url) {
                        item.setImage(url);
                        Model.instance.saveItem(item, () -> Navigation.findNavController(view).navigateUp());
                    }
                });
            }
            else {
                Navigation.findNavController(view).navigateUp();
            }
        });


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