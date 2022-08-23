package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ChatRoom extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton back_btn;
    String roomName;
    String roomID;
    ImageButton send_msg_btn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();;
    private CollectionReference msgRef;
    EditText message_field;
    FloatingActionButton wave_hand_flt_btn;
    FloatingActionButton question_mark_flt_btn;
    RecyclerView rv;
    ArrayList<String> memberList;
    String message = "1. Long press on your avatar to delete message.\n\n" +
            "2. Hit the 'Wave' button if you want to send notifications to everyone. 'Wall Messages' is meant for important posts and not conversation.";

    private MsgAdapter adapter;

    String userName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);



        toolbar = findViewById(R.id.toolbar);
        back_btn = findViewById(R.id.char_room_back_flt_btn);
        send_msg_btn = findViewById(R.id.send_msg_btn);
        message_field = findViewById(R.id.message_field);
        rv = findViewById(R.id.chat_rv);
        wave_hand_flt_btn = findViewById(R.id.wave_hand_flt_btn);
        question_mark_flt_btn = findViewById(R.id.question_mark_flt_btn);


        Bundle extras = getIntent().getExtras();

        if (extras != null ) {
            roomName =  extras.getString("roomName");
            roomID = extras.getString("roomID");
            msgRef = db.collection(roomID);

            db.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                       userName =  (String) documentSnapshot.get("fullname");
                    }
                }
            });
        }

        toolbar.setTitle(roomName);
        setUpRecyclerView();

        send_msg_btn.setOnClickListener(view -> {
            HashMap<String, Object> chatObj = new HashMap<>();
            DocumentReference docRef = db.collection(roomID).document();

            chatObj.put("id", docRef.getId());
            chatObj.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            chatObj.put("text", message_field.getText().toString());
            chatObj.put("timestamp", new Date());


            docRef.set(chatObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    message_field.getText().clear();
                    message_field.requestFocus();
                }
            });

        });


        back_btn.setOnClickListener(view -> finish());

        wave_hand_flt_btn.setOnClickListener(view -> {


            DocumentReference docRef =  db.collection("rooms").document(roomID);


            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        memberList = (ArrayList<String>) documentSnapshot.get("members");

                        assert memberList != null;
                        for (String member: memberList) {
                            if (!Objects.equals(member, FirebaseAuth.getInstance().getUid())) {
                                DocumentReference userRef = db.collection("users").document(member);

                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot ds = task.getResult();
                                            String userDeviceToken = (String) ds.get("deviceToken");
                                            String title = "Room: " + roomName;
                                            String message = userName+" has posted a new message.";
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), ChatRoom.this );
                                            notificationsSender.SendNotifications();
                                        }
                                    }
                                });
                            }
                        }

                    }
                }
            });






            Toast.makeText(getApplicationContext(),"Everyone notified of your new post.",Toast.LENGTH_LONG).show();

        });


        question_mark_flt_btn.setOnClickListener(view -> {
            //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(view.getContext(),R.style.AlertDialog)
                    .setTitle("Wall Post")
                    .setMessage(message)
                    .setPositiveButton("Okay", (dialog, which) -> {

                        dialog.dismiss();
                    })
                    .show();
        });


        // listen for changes to show image
        Objects.requireNonNull(rv.getAdapter()).registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                smoothMsgScroll();

            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                 smoothMsgScroll();
            }
        });


        rv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override

            public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
            {

                smoothMsgScroll();

            }
        });
    }

    private void setUpRecyclerView(){
        Query query = msgRef.orderBy("timestamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Message_Data> options = new FirestoreRecyclerOptions.Builder<Message_Data>()
                                                        .setQuery(query, Message_Data.class)
                                                        .build();

        adapter = new MsgAdapter(options, ChatRoom.this, roomID);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        smoothMsgScroll();




    }

    private void smoothMsgScroll(){
        int[] countFinal = new int[1];

        FirebaseFirestore.getInstance().collection(roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            countFinal[0]  = task.getResult().size();
                            if ( countFinal[0]  > 0 ) {
                                rv.smoothScrollToPosition( countFinal[0]-1);
                            }

                        }
                    }
                });
    }

    protected void onStart(){
        super.onStart();
        adapter.startListening();
    }

    protected void onStop(){
        super.onStop();
        adapter.stopListening();
    }
}
