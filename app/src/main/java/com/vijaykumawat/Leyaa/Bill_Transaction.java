package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

public class Bill_Transaction extends AppCompatActivity {

    String roomID;
    String memberID;
    TextView payNotification, getNotification;
    String userName;
    String roomName;

    FloatingActionButton bill_split_member_back_flt_btn;

    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private Bill_Transaction_Adapter adapter;
    Bill_Transaction_Adapter adapter2;
    RecyclerView.LayoutManager layoutManager;
    String mUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String memberIDDevToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_transactions);

        payNotification = findViewById(R.id.payNotification);
        getNotification = findViewById(R.id.getNotification);

        bill_split_member_back_flt_btn = findViewById(R.id.bill_split_member_back_flt_btn);
      
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            memberID = extras.getString("memberID");
        }


        bill_split_member_back_flt_btn.setOnClickListener(view -> {
            //Toast.makeText(this, roomID, Toast.LENGTH_LONG).show();
            finish();
        });

        db.collection("users").document(memberID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    memberIDDevToken = (String) documentSnapshot.get("deviceToken");
                }
            }
        });

        db.collection("rooms").document(roomID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                roomName = (String) documentSnapshot.get("title");
            }
        });


        db.collection("users").document(mUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    userName = (String) documentSnapshot.get("fullname");
                }
            }
        });



        setUpRecyclerViewGet();
        setUpRecyclerViewPay();

        payNotification.setOnClickListener(view -> {

            String title = "Room: " + roomName;
            String message = userName+" has cleared dues.";
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(memberIDDevToken, title, message, getApplicationContext(), Bill_Transaction.this );
            notificationsSender.SendNotifications();


            Toast.makeText(getApplicationContext(),"Notification Sent",Toast.LENGTH_LONG).show();
        });

        getNotification.setOnClickListener(view -> {

            String title = "Room: " + roomName;
            String message = userName+" requested you to clear dues.";
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(memberIDDevToken, title, message, getApplicationContext(), Bill_Transaction.this );
            notificationsSender.SendNotifications();


            Toast.makeText(getApplicationContext(),"Notification Sent",Toast.LENGTH_LONG).show();
        });
    }

    private void setUpRecyclerViewGet() {


        Query query = mstore.collection(roomID+"_BILLS")
                .whereEqualTo("contributor",memberID)
                .whereEqualTo("payer", mUid);

        FirestoreRecyclerOptions<Bill_Transaction_Data> options = new FirestoreRecyclerOptions.Builder<Bill_Transaction_Data>()
                .setQuery(query, Bill_Transaction_Data.class).build();

        adapter = new Bill_Transaction_Adapter(options, roomID+"_BILLS");
        RecyclerView recyclerView = findViewById(R.id.recycler_view_get);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(Bill_Transaction.this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }
    private void setUpRecyclerViewPay() {


        Query query2 = mstore.collection(roomID+"_BILLS")
                .whereEqualTo("contributor",mUid)
                .whereEqualTo("payer",memberID);

        FirestoreRecyclerOptions<Bill_Transaction_Data> options2 = new FirestoreRecyclerOptions.Builder<Bill_Transaction_Data>()
                .setQuery(query2, Bill_Transaction_Data.class).build();

        adapter2 = new Bill_Transaction_Adapter(options2, roomID+"_BILLS");
        RecyclerView recyclerView = findViewById(R.id.recyclerView_pay);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(Bill_Transaction.this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter2);



    }




    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        adapter2.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        adapter2.startListening();
    }
}

