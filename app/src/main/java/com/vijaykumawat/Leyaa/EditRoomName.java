package com.vijaykumawat.Leyaa;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;



public class EditRoomName extends AppCompatActivity {
    FirebaseAuth mAuth;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.N)
    String name;

    @RequiresApi(api = Build.VERSION_CODES.N)


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_room_name);

        mAuth = FirebaseAuth.getInstance();
        String nameVal = "";
        EditText newNameText = (EditText) findViewById(R.id.newroomname);
        Button saveNewName = findViewById(R.id.btn_update_room_name);
        String roomID = "";



        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nameVal =  extras.getString("roomName");
            roomID = extras.getString("roomID");
            newNameText.setText(nameVal, TextView.BufferType.EDITABLE);

        }

        DocumentReference documentReference = db.collection("rooms").document(roomID);


//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

//        Bundle extras = getIntent().getExtras();
//
//        if (extras != null) {
//            nameVal =  extras.getString("nameValue");
//            newNameText.setText(nameVal, TextView.BufferType.EDITABLE);
//        }



        Button backButton = findViewById(R.id.btn_edit_room_cancel);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imm.hideSoftInputFromWindow(editroomName.getWindowToken(), 0);
                finish();
            }
        });



      saveNewName.setOnClickListener(view -> {
              if (!newNameText.getText().toString().isEmpty()) {
              documentReference
              .update("title", newNameText.getText().toString())
              .addOnSuccessListener(new OnSuccessListener<Void>() {
@Override
public void onSuccess(Void aVoid) {
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
        finish();

        }
        })
        .addOnFailureListener(new OnFailureListener() {
@Override
public void onFailure(@NonNull Exception e) {
        Log.w("TAG", "Error updating document", e);
        }
        });
        }
        });



    }
}

