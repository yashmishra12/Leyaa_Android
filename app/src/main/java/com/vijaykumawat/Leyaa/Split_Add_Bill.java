package com.vijaykumawat.Leyaa;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Split_Add_Bill extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> memberIDs;
    ArrayList<SplitBill_Add_Bill_MemberData> userArrayList;
    Split_Add_Bill_Member_Adapter myAdapter;
    FirebaseFirestore db;
    String roomID = "";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_add_bill_mainactivity);

        Toolbar toolbar= findViewById(R.id.toolbar);
        FloatingActionButton backButtonRMI = findViewById(R.id.back_button_split_add_bill);

        backButtonRMI.setOnClickListener(view -> {
            finish();
        });



        recyclerView = findViewById(R.id.recyclerView_add_bill);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        userArrayList = new ArrayList<>();
        myAdapter = new Split_Add_Bill_Member_Adapter(Split_Add_Bill.this, userArrayList);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            //roomID = "tbHm3PkJa9ZLzFEoGNRi";
            Log.d("RoomID",roomID+"--------------------------------------");
            //toolbar.setTitle(extras.getString("roomName"));
            populateMemberID();
        }


    }

    private void populateMemberID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("rooms").document("tbHm3PkJa9ZLzFEoGNRi");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                memberIDs = (ArrayList<String>) doc.get("members");
//                Log.d("TAG", "MEMEBRS: "+ memberIDs.stream()
//                        .collect(Collectors.joining(" --> ")));
                recyclerView.setAdapter(myAdapter);
                EventChangeListener();

            }
        });
    }

    private void EventChangeListener() {

        for (String memberID: memberIDs) {
            DocumentReference documentReference = db.collection("users").document(memberID);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.exists()) {
                            String fullname = (String) documentSnapshot.get("fullname");
                            //String email = (String) documentSnapshot.get("email");
                            String avatar = (String) documentSnapshot.get("avatar");


                            SplitBill_Add_Bill_MemberData md = new SplitBill_Add_Bill_MemberData(avatar, fullname);

                            userArrayList.add(md);
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });



        }



    }


}
