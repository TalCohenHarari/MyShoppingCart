package com.example.mymarketlist.model;


import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

    final static String userCollection = "users";
    final static String itemCollection = "items";
    final static String shoppingCartCollection = "shoppingCarts";
    final static String generalItemCollection = "generalItems";


    private ModelFirebase() {}

    //--------------------------------------User--------------------------------------------

    public interface GetAllUsersListener {
        public void onComplete(List<User> users);
    }

    public static void getAllUsers(Long since, GetAllUsersListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(userCollection)
                .whereGreaterThanOrEqualTo(User.LAST_UPDATED,new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<User> list = new LinkedList<User>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(User.create(document.getData()));
                            }
                        } else {}
                        listener.onComplete(list);
                    }
                });
    }

    //Save and signUp:
    public static void saveUser(User user, String action, Model.isLoginSuccessCompleteListener listener) {

        if (action.equals("signUp"))
        {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    user.setId(firebaseUser.getUid());
                    save(user,action,(isSuccess)->listener.onComplete(isSuccess));
                }
            });
        }
        else if(action.equals("updateEmail") || action.equals("updatePassword") || action.equals("updateEmailAndPassword") )//If it's an update username based on firebase authentication:
        {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if(action.equals("updateEmail"))
                currentUser.updateEmail(user.getEmail()).addOnCompleteListener(task -> { if (task.isSuccessful()) { save(user,action,(isSuccess)->listener.onComplete(isSuccess)); } });
            else if(action.equals("updatePassword"))
                currentUser.updatePassword(user.getPassword()).addOnCompleteListener(task -> { if (task.isSuccessful()) { save(user,action,(isSuccess)->listener.onComplete(isSuccess)); } });
            else{
                currentUser.updateEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        currentUser.updatePassword(user.getPassword()).addOnCompleteListener(task1 -> { if (task1.isSuccessful()) save(user,action,(isSuccess)->listener.onComplete(isSuccess)); });
                    }
                });
            }
        }
        else if(action.equals("delete")) //delete user 'Auth'
        {
            save(user,action,(isSuccess)->{
                FirebaseUser deletedUser = FirebaseAuth.getInstance().getCurrentUser();
                deletedUser.delete().addOnCompleteListener(task -> { if (task.isSuccessful()) { listener.onComplete(true); } });});
        }
        else //If it's an update details:
            save(user,action,listener);
    }

    public static void save(User user,String action ,Model.isLoginSuccessCompleteListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(userCollection).document(user.getId()).set(user.toJson()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getCurrentUser(listener);
                    }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onComplete(false);
            }
        });
    }

    public static void login(String userEmail, String password, Model.isLoginSuccessCompleteListener listener) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getCurrentUser((isSuccess)->listener.onComplete(isSuccess));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onComplete(false);
            }
        });
    }

    public static void isLoggedIn(Model.OnCompleteListener listener) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Model.instance.loadingStateDialog.setValue(Model.LoadingState.loading);
            getCurrentUser((isSuccess)->listener.onComplete());
        }
    }

    public static void getCurrentUser(Model.isLoginSuccessCompleteListener listener)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        db.collection(userCollection).document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Model.instance.setUser(User.create(documentSnapshot.getData()),(isSuccess)->listener.onComplete(isSuccess));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onComplete(false);
            }
        });
    }

    public static void signOut(){
        FirebaseAuth.getInstance().signOut();
    }


    public static void resentPassword(String email, Model.onResentPasswordCompleteListener listener) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            listener.onComplete(true);
                        } else {
                            listener.onComplete(false);
                        }
                    }
                });
    }
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

    public static void updateInLiveItem(Item item,Model.OnCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(itemCollection).document(item.getId()).set(item.toJson())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onComplete();
                    }
                });
    }

    //--------------------------------------General Item--------------------------------------------

    public interface GetAllGeneralItemsListener {
        public void onComplete(List<GeneralItem> generalItems);
    }

    public static void getAllGeneralItems(Long since, GetAllGeneralItemsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(generalItemCollection)
                .whereGreaterThanOrEqualTo(GeneralItem.LAST_UPDATED,new Timestamp(since,0))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<GeneralItem> list = new LinkedList<GeneralItem>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(GeneralItem.create(document.getData()));
                            }
                        } else {}
                        listener.onComplete(list);
                    }
                });
    }

    //Save
    public static void saveGeneralItem(GeneralItem item, Model.OnCompleteListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(generalItemCollection).document(item.getName()).set(item.toJson())
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

    public static void updateInLiveShoppingCart(ShoppingCart shoppingCart,Model.OnCompleteListener listener) {
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
