package com.example.mymarketlist.model;


import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

public class ModelFirebase {

    final static String itemCollection = "items";
    final static String shoppingCartCollection = "shoppingCarts";
    final static String categoryCollection = "category";



    private ModelFirebase() {}

    //--------------------------------------Item--------------------------------------------

    public interface GetAllItemsListener {
        public void onComplete(List<Item> items);
    }

    public static void getAllItems(Long since, GetAllItemsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(itemCollection)
                .whereGreaterThanOrEqualTo(Item.LAST_UPDATED,new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Item> list = new LinkedList<Item>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(Item.create(document.getData()));
                            }
                        } else {}
                        listener.onComplete(list);
                    }
                });
    }

    //Save
    public static void saveItem(Item item, Model.OnCompleteListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
             db.collection(itemCollection).document(item.getId()).set(item.toJson())
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             listener.onComplete();
                         }
                     });

    }

    //--------------------------------------ShoppingCart--------------------------------------------

    public interface GetAllShoppingCartsListener {
        public void onComplete(List<ShoppingCart> shoppingCarts);
    }

    public static void getAllShoppingCarts(Long since, GetAllShoppingCartsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(shoppingCartCollection)
                .whereGreaterThanOrEqualTo(Item.LAST_UPDATED,new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ShoppingCart> list = new LinkedList<ShoppingCart>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(ShoppingCart.create(document.getData()));
                            }
                        } else {}
                        listener.onComplete(list);
                    }
                });
    }

    //Save
    public static void saveShoppingCart(ShoppingCart shoppingCart, Model.OnCompleteListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(shoppingCartCollection).document(shoppingCart.getId()).set(shoppingCart.toJson())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onComplete();
                    }
                });

    }


    //--------------------------------SaveImages in storage----------------------------

    public static void uploadImage(Bitmap imageBmp, String name, final Model.UpLoadImageListener listener){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference imagesRef;
            imagesRef = storage.getReference().child("Pictures/").child(name);
        //Convert image:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        // Now we upload the data to firebase storage:
        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                listener.onComplete("");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Getting from fireBase the image url:
                        Uri downloadUrl = uri;
                        listener.onComplete(downloadUrl.toString());
                    }
                });
            }
        });
    }
}
