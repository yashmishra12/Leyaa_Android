
package com.vijaykumawat.Leyaa;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.Build;
        import android.os.Bundle;
        import android.widget.Button;
        import android.widget.Toast;

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

public class SplitBill_MemberInfo extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> memberIDs;
    ArrayList<SplitMemberData> userArrayList;
    SplitMemberDataAdapter myAdapter;
    FirebaseFirestore db;
    String roomID = "";
    ProgressDialog progressDialog;
    RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_member_info);

        Toolbar toolbar= findViewById(R.id.toolbar);
        FloatingActionButton backButtonRMI = findViewById(R.id.FLOATINGbackButtonRMI);

        backButtonRMI.setOnClickListener(view -> {
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();


        recyclerView = findViewById(R.id.recycler_view_bill_member);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(SplitBill_MemberInfo.this, 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));


        db = FirebaseFirestore.getInstance();

        userArrayList = new ArrayList<>();
        myAdapter = new SplitMemberDataAdapter(SplitBill_MemberInfo.this, userArrayList);

        myAdapter.setOnItemClickListener(new SplitMemberDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SplitMemberData splitMemberData) {


                //startActivity(new Intent(SplitBill_MemberInfo.this, Inside_Room_Activity.class).putExtra("document_ID",document_ID ));
                Toast.makeText(SplitBill_MemberInfo.this, "Selected profile id    "+ memberIDs.get(0), Toast.LENGTH_SHORT ).show();
            }
        });

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


                            SplitMemberData smd = new SplitMemberData(avatar,fullname);

                            userArrayList.add(smd);
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

