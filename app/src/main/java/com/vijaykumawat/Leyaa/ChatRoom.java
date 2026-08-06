package com.vijaykumawat.Leyaa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class ChatRoom extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "roomID";
    public static final String EXTRA_ROOM_NAME = "roomName";

    Toolbar toolbar;
    FloatingActionButton back_btn;
    String roomName;
    String roomID;
    ImageButton send_msg_btn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    // Helper to create a proper Intent for this Activity
    public static Intent createIntent(Context context, String roomId, String roomName) {
        Intent intent = new Intent(context, ChatRoom.class);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_ROOM_NAME, roomName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

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

        // Read intent extras
        handleIntent(getIntent());

        // If room info is missing, don't continue
        if (roomID == null || roomName == null) {
            Toast.makeText(this, "Room information missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(roomName);
        setUpRecyclerView();

        // listen for changes to show image / auto-scroll
        if (rv.getAdapter() != null) {
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
        }

        rv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                smoothMsgScroll();
            }
        });

        send_msg_btn.setOnClickListener(view -> {
            if (message_field.getText().toString().trim().isEmpty()) {
                return;
            }

            if (roomID == null) {
                Toast.makeText(ChatRoom.this, "Room not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> chatObj = new HashMap<>();
            DocumentReference docRef = db.collection(roomID).document();

            chatObj.put("id", docRef.getId());
            chatObj.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            chatObj.put("text", message_field.getText().toString());
            chatObj.put("timestamp", new Date());

            docRef.set(chatObj).addOnCompleteListener(task -> {
                message_field.getText().clear();
                message_field.requestFocus();
            });
        });

        back_btn.setOnClickListener(view -> finish());

        wave_hand_flt_btn.setOnClickListener(view -> {
            if (roomID == null) {
                Toast.makeText(ChatRoom.this, "Room not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference docRef = db.collection("rooms").document(roomID);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    memberList = (ArrayList<String>) documentSnapshot.get("members");

                    if (memberList == null) return;

                    for (String member : memberList) {
                        if (!Objects.equals(member, FirebaseAuth.getInstance().getUid())) {
                            DocumentReference userRef = db.collection("users").document(member);

                            userRef.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot ds = task1.getResult();
                                    String userDeviceToken = (String) ds.get("deviceToken");
                                    String title = "Room: " + roomName;
                                    String msg = userName + " has posted a new message.";
                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(
                                                    userDeviceToken,
                                                    title,
                                                    msg,
                                                    getApplicationContext(),
                                                    ChatRoom.this);
                                    notificationsSender.SendNotifications();
                                }
                            });
                        }
                    }
                }
            });

            Toast.makeText(getApplicationContext(),
                    "Everyone notified of your new post.",
                    Toast.LENGTH_LONG).show();
        });

        question_mark_flt_btn.setOnClickListener(view -> {
            new AlertDialog.Builder(view.getContext(), R.style.AlertDialog)
                    .setTitle("Wall Post")
                    .setMessage(message)
                    .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    // Handle when Activity is reused (launchMode singleTop / notification tapping again)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);

        if (roomID == null || roomName == null) {
            Toast.makeText(this, "Room information missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(roomName);
        // If you ever want to support switching rooms in same activity,
        // you could re-create adapter here with new roomID.
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            roomName = extras.getString(EXTRA_ROOM_NAME);
            roomID = extras.getString(EXTRA_ROOM_ID);

            if (roomID != null) {
                msgRef = db.collection(roomID);
            }

            if (FirebaseAuth.getInstance().getUid() != null) {
                db.collection("users")
                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                userName = (String) documentSnapshot.get("fullname");
                            }
                        });
            }
        }
    }

    private void setUpRecyclerView() {
        if (msgRef == null) {
            Log.e("ChatRoom", "msgRef is null. Cannot set up RecyclerView.");
            return;
        }

        Query query = msgRef.orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Message_Data> options =
                new FirestoreRecyclerOptions.Builder<Message_Data>()
                        .setQuery(query, Message_Data.class)
                        .build();

        adapter = new MsgAdapter(options, ChatRoom.this, roomID);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        smoothMsgScroll();
    }

    private void smoothMsgScroll() {
        if (roomID == null) return;

        FirebaseFirestore.getInstance().collection(roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            if (count > 0) {
                                rv.smoothScrollToPosition(count - 1);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
