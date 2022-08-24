package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class Bill_Transaction extends AppCompatActivity {

    String roomID;
    String memberID;

    FloatingActionButton bill_split_member_back_flt_btn;

    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private Bill_Transaction_Adapter adapter;
    Bill_Transaction_Adapter adapter2;
    RecyclerView.LayoutManager layoutManager;
    String mUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_transactions);


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



        setUpRecyclerViewGet();
        setUpRecyclerViewPay();
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

