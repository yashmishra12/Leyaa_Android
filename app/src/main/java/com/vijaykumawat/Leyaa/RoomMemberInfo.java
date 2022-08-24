package com.vijaykumawat.Leyaa;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;

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

public class RoomMemberInfo extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> memberIDs;
    ArrayList<MemberData> userArrayList;
    MemberDataAdapter myAdapter;
    FirebaseFirestore db;
    String roomID = "";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_member_info);

        Toolbar toolbar= findViewById(R.id.toolbar_bill_split_trans);
        FloatingActionButton backButtonRMI = findViewById(R.id.bill_split_member_back_flt_btn);

        backButtonRMI.setOnClickListener(view -> {
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();


        recyclerView = findViewById(R.id.memberRV);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        userArrayList = new ArrayList<>();
        myAdapter = new MemberDataAdapter(RoomMemberInfo.this, userArrayList);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            toolbar.setTitle(extras.getString("roomName"));
            populateMemberID();
        }


    }

    private void populateMemberID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("rooms").document(roomID);

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
                            String email = (String) documentSnapshot.get("email");
                            String avatar = (String) documentSnapshot.get("avatar");


                            MemberData md = new MemberData(avatar, fullname, email);

                            userArrayList.add(md);
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }


        }



    }


}
