package com.vijaykumawat.Leyaa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MemberAdd extends AppCompatActivity {
    String roomID;
    String roomName;
    String  userName;

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_add);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            roomName =  extras.getString("roomName");
            userName =  extras.getString("userName");
        }

        FloatingActionButton backButton = findViewById(R.id.back_button_member_add);
        EditText share_email = findViewById(R.id.share_email);
        EditText share_message = findViewById(R.id.share_message);
        Button send_invite = findViewById(R.id.send_invite_btn);
        Button share_app = findViewById(R.id.share_app_btn);

        share_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        share_email.requestFocus();

        // Share App Pressed
        share_app.setOnClickListener(view -> {
            Intent myItent = new Intent(Intent.ACTION_SEND);
            myItent.setType("text/plain");
            myItent.putExtra(Intent.EXTRA_SUBJECT, "Leyaa - Shop Better, Live Organized.");
            myItent.putExtra(Intent.EXTRA_TEXT, secretKey.playStoreURL);
            startActivity(Intent.createChooser(myItent, "Share Via"));
        });

        // Send Invite Pressed
        send_invite.setOnClickListener(view -> {
            String email = share_email.getText().toString().toLowerCase(Locale.ROOT);
            String message = share_message.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Oops! Email is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();



            db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot ds = task.getResult();

                        if (ds.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Uh Oh! It looks like the person you're trying to add isn't registered with us. Share the app with them", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Map<String, String> request = new HashMap<>();
                            request.put("message", message);
                            request.put("roomID", roomID);
                            request.put("roomName", roomName);
                            request.put("senderName", userName);
                            request.put("receiverEmail", email);



                            db.collection("roomRequest").add(request).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getApplicationContext(), "Request sent!", Toast.LENGTH_SHORT).show();

                                    Query dbCollection =  db.collection("users").whereEqualTo("email", email).limit(1);

                                    dbCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            QuerySnapshot qs = task.getResult();


                                            ArrayList<DocumentSnapshot> ds = (ArrayList<DocumentSnapshot>) qs.getDocuments();
                                            DocumentSnapshot ds_first = ds.get(0);

                                            String userDeviceToken = (String) ds_first.get("deviceToken");

                                            String title = "Room Join Invitation";
                                            String message = userName+" invited you to join "+roomName;
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), MemberAdd.this );
                                            notificationsSender.SendNotifications();

                                            share_email.setText("");
                                            share_message.setText("");
                                            share_email.requestFocus();

                                        }
                                    });

                                }
                            });


                        }

                    }
                }
            });



        });

        // Back Button Pressed
        backButton.setOnClickListener(view -> {
            finish();
        });







    }
}
