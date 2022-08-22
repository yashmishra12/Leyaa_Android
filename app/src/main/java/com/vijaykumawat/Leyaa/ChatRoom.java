package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ChatRoom extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton back_btn;
    String roomName;
    String roomID;
    ImageButton send_msg_btn;
    FirebaseFirestore db;
    EditText message_field;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        back_btn = findViewById(R.id.char_room_back_flt_btn);
        send_msg_btn = findViewById(R.id.send_msg_btn);
        message_field = findViewById(R.id.message_field);


        Bundle extras = getIntent().getExtras();

        if (extras != null ) {
            roomName =  extras.getString("roomName");
            roomID = extras.getString("roomID");
        }

        toolbar.setTitle(roomName);

        send_msg_btn.setOnClickListener(view -> {
            HashMap<String, Object> chatObj = new HashMap<>();
            chatObj.put("id", UUID.randomUUID().toString());
            chatObj.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            chatObj.put("text", message_field.getText().toString());
            chatObj.put("timestamp", new Date());

             db.collection(roomID).add(chatObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                 @Override
                 public void onSuccess(DocumentReference documentReference) {
                     message_field.getText().clear();
                     message_field.requestFocus();

                 }
             });
        });


        back_btn.setOnClickListener(view -> finish());


    }
}
