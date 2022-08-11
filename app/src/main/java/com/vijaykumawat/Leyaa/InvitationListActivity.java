package com.vijaykumawat.Leyaa;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


// BaseActivity extends AppCompatActivity so do not need to extend here
// you will automatically get parent properties
public class InvitationListActivity extends BaseActivity {



    private FirebaseAuth mAuth;
    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();

    private InvitationAdapter adapter;


    private InvitationAdapter accept;
    FloatingActionButton floatingButton;
    String userID;
    String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    //private String users;


    @Override
    int getContentViewId() {
        return R.layout.invitation_list;
    }

    // action you want to selected - eg. i want home btn to get selected
    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_room;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpRecyclerView();

    }


    private void setUpRecyclerView() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //DocumentReference docRef = db.collection("roomRequest");
        final String[] emailID = new String[1];





        DocumentReference docRef = db.collection("users").document(mUid);



        String userEmailInfo = FirebaseAuth.getInstance().getCurrentUser().getEmail();



        Query query = mstore.collection("roomRequest").whereEqualTo("receiverEmail",userEmailInfo);

        Log.d("myTag", String.valueOf(mUid) + "-----------------------------------");
        Log.d("myTag", String.valueOf(query) + "--------------------------");
        FirestoreRecyclerOptions<Invitation_Data> options = new FirestoreRecyclerOptions.Builder<Invitation_Data>()
                .setQuery(query, Invitation_Data.class).build();


        adapter = new InvitationAdapter(options);




        RecyclerView recyclerView = findViewById(R.id.recycler_view_invitation);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));


        recyclerView.setAdapter(adapter);



        adapter.setOnItemClickListener(new InvitationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //startActivity(new Intent(InvitationListActivity.this, Inside_Room_Activity.class).putExtra("selected room",position ));
                Intent intent = new Intent(InvitationListActivity.this, InvitationListActivity.class);


                String id = documentSnapshot.getId();
                //intent.putExtra("documentID_room", id);

                adapter.acceptRequest(position);

//                Log.d("myTag", String.valueOf(finalReceiverEmail2) + "-------------------receiverEmail----------------");
//                Toast.makeText(InvitationListActivity.this,  " Accepted " , Toast.LENGTH_SHORT).show();

            }
        });





    }


    @Override
    public void onStop(){
        super.onStop();
        adapter.stopListening();


    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();


    }

}