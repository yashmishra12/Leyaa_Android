
package com.vijaykumawat.Leyaa;



        import android.content.Intent;

        import android.os.Build;
        import android.os.Bundle;

        import androidx.annotation.NonNull;
        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.Toolbar;
        import androidx.recyclerview.widget.GridLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.android.material.floatingactionbutton.FloatingActionButton;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.firestore.DocumentReference;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.FirebaseFirestore;

        import java.util.ArrayList;

public class SplitBill_Member_Info extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> memberIDs;
    ArrayList<SplitMemberData> userArrayList;
    SplitMemberDataAdapter myAdapter;
    FirebaseFirestore db;
    String roomID = "";

    RecyclerView.LayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_member_info);



        Toolbar toolbar= findViewById(R.id.toolbar);
        FloatingActionButton backButtonRMI = findViewById(R.id.bill_split_member_back_flt_btn);


        backButtonRMI.setOnClickListener(view -> {
            finish();
        });



        FloatingActionButton split_add_bill = findViewById(R.id.add_bill);
        split_add_bill.setOnClickListener(view -> {
            Intent intent = new Intent(SplitBill_Member_Info.this, Split_Add_Bill.class);
            intent.putExtra("roomID",roomID);
            startActivity(intent);
        });



        recyclerView = findViewById(R.id.recycler_view_bill_member);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(SplitBill_Member_Info.this, 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));


        db = FirebaseFirestore.getInstance();



        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            toolbar.setTitle(extras.getString("roomName"));
            populateMemberID();
        }

        userArrayList = new ArrayList<>();
        myAdapter = new SplitMemberDataAdapter(SplitBill_Member_Info.this, userArrayList, roomID);

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


                String selfID = FirebaseAuth.getInstance().getUid();
                memberIDs.remove(selfID);
                recyclerView.setAdapter(myAdapter);
                EventChangeListener();

            }
        });

    }

    private void EventChangeListener() {
        String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (String memberID: memberIDs) {
            DocumentReference documentReference = db.collection("users").document(memberID);


            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {


                        DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.exists() && (!memberID.equals(mUid))) {
                            String fullname = (String) documentSnapshot.get("fullname");
                            String avatar = (String) documentSnapshot.get("avatar");
                            String userID = (String) documentSnapshot.get("uid");


                            SplitMemberData smd = new SplitMemberData(avatar, fullname, userID);

                            userArrayList.add(smd);
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });



        }



    }


}
