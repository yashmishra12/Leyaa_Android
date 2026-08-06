package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class SplitBill_Individual_Activity extends AppCompatActivity {

    String roomID;
    String memberID;
    TextView userID_tv;
    TextView roomID_tv;
    FloatingActionButton bill_split_member_back_flt_btn;
    private final FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private Bill_Transaction_Adapter adapter;
    String mUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_individual);


        userID_tv = findViewById(R.id.userID_SBI);
        roomID_tv = findViewById(R.id.roomID_SBI);
        bill_split_member_back_flt_btn = findViewById(R.id.bill_split_member_back_flt_btn);


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            memberID = extras.getString("memberID");

            roomID_tv.setText(roomID);
            userID_tv.setText(memberID);
        }


        bill_split_member_back_flt_btn.setOnClickListener(view -> {
            //Toast.makeText(this, roomID, Toast.LENGTH_LONG).show();
            finish();
        });

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {


        Query query = mstore.collection(roomID+"_BILLS")
                .whereEqualTo("contributor",memberID)
                .whereEqualTo("payer",mUid);

        FirestoreRecyclerOptions<Bill_Transaction_Data> options = new FirestoreRecyclerOptions.Builder<Bill_Transaction_Data>()
                .setQuery(query, Bill_Transaction_Data.class).build();

        adapter = new Bill_Transaction_Adapter(options);
//        RecyclerView recyclerView = findViewById(R.id.recycler_view_indi);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);



    }




    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
}

