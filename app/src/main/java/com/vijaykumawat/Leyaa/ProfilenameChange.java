package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfilenameChange extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_name_change);

        mAuth = FirebaseAuth.getInstance();
        String nameVal;
        EditText newNameText = findViewById(R.id.newNameText);
        Button saveNewName = findViewById(R.id.nameChangeSave);
        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference documentReference = db.collection("users").document(userID);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nameVal =  extras.getString("nameValue");
            newNameText.setText(nameVal, TextView.BufferType.EDITABLE);
        }

        newNameText.requestFocus();

        FloatingActionButton backButton = findViewById(R.id.back_button_name_change);


        //back button pressed
        backButton.setOnClickListener(view -> finish());


        // save button pressed
        saveNewName.setOnClickListener(view -> {
            if (!newNameText.getText().toString().isEmpty()) {
                documentReference
                        .update("fullname", newNameText.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
            }
        });

    }
}