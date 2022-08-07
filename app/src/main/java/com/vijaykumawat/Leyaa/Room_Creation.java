package com.vijaykumawat.Leyaa;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Room_Creation extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    Button createButton;
    EditText roomName;
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_create_manager);
        createButton = findViewById(R.id.createNewRoomButton);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        roomName = findViewById(R.id.newRoomName);


        Button backButton = findViewById(R.id.roomCreateBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String nameRoom = roomName.getText().toString();
                if (nameRoom.isEmpty() == false) {
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
                                    Log.d(TAG, "onSuccess: Room ID "+ userID);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });

                    startActivity(new Intent(getApplicationContext(), RoomListActivity.class));

                }


            }
        });

    }
}

