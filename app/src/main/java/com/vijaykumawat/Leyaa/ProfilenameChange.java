package com.vijaykumawat.Leyaa;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilenameChange extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.N)
    String name;

    @RequiresApi(api = Build.VERSION_CODES.N)


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_name_change);

        mAuth = FirebaseAuth.getInstance();
        String nameVal = "";
        EditText newNameText = findViewById(R.id.newNameText);
        Button saveNewName = findViewById(R.id.nameChangeSave);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nameVal =  extras.getString("nameValue");
            newNameText.setText(nameVal, TextView.BufferType.EDITABLE);
        }

        newNameText.requestFocus();

        FloatingActionButton backButton = findViewById(R.id.back_button_name_change);

        //back button pressed
        backButton.setOnClickListener(view -> {
           finish();
        });

        // save button pressed

        saveNewName.setOnClickListener(view -> {
            if (!newNameText.getText().toString().isEmpty()) {
                documentReference
                        .update("fullname", newNameText.getText().toString())
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