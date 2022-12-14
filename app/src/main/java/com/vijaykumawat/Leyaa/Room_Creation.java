package com.vijaykumawat.Leyaa;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Room_Creation extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    Button createButton;
    EditText roomName;
    public static final String TAG = "TAG";
    FloatingActionButton roomBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.room_create_manager);
        createButton = findViewById(R.id.createNewRoomButton);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        roomName = findViewById(R.id.newRoomName);
        roomName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        roomBackButton = findViewById(R.id.createRoomBackButton);
        roomBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imm.hideSoftInputFromWindow(roomName.getWindowToken(), 0);
                finish();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String nameRoom = roomName.getText().toString();
                if (!nameRoom.isEmpty()) {


                    imm.hideSoftInputFromWindow(roomName.getWindowToken(), 0);
                    if (!nameRoom.isEmpty()) {
                        FirebaseUser fuser = mAuth.getCurrentUser();
                        String userID = fuser.getUid();

                        DocumentReference documentReference = mStore.collection("rooms").document();
                        Map<String,Object> room = new HashMap<>();

                        room.put("lastItemID","");
                        room.put("members", Arrays.asList(userID));
                        room.put("newItems", Arrays.asList());
                        room.put("title", nameRoom);




                        documentReference.set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                        startActivity(new Intent(getApplicationContext(), RoomListActivity.class));


                        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()){

                                    return;
                                }

                                String token = task.getResult();
                                docRef.update("deviceToken", token);
                            }
                        });

                    }

                }else {
                    Toast toast= Toast.makeText(Room_Creation.this, "Room name is missing", Toast.LENGTH_SHORT);
                    View view1 = toast.getView();

                    view1.setBackgroundColor(Color.LTGRAY);
                    toast.show();
                }



            }
        });

    }
}





