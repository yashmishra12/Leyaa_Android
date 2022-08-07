package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


// BaseActivity extends AppCompatActivity so do not need to extend here
// you will automatically get parent properties
public class RoomListActivity extends BaseActivity {



    private FirebaseAuth mAuth;
    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private CollectionReference room_col = mstore.collection("room");
    private RoomAdapter adapter;
    FloatingActionButton floatingButton;
    String userID;
    String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    int getContentViewId() {
        return R.layout.room_list;
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


        floatingButton =  findViewById(R.id.floatingActionButton);
        floatingButton.setOnClickListener( v -> startActivity(new Intent(this, Room_Creation.class)));


        Query query = mstore.collection("rooms").whereArrayContains("members",mUid );
        Log.d("myTag", String.valueOf(mUid) + "-----------------------------------");
        Log.d("myTag", String.valueOf(query) + "--------------------------");
        FirestoreRecyclerOptions<Room_Title> options = new FirestoreRecyclerOptions.Builder<Room_Title>()
                .setQuery(query, Room_Title.class).build();

        adapter = new RoomAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new RoomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                startActivity(new Intent(RoomListActivity.this, Inside_Room_Activity.class).putExtra("selected room",position ));

                //Toast.makeText(getActivity(), "Position: " + position + " ID: " + id, Toast.LENGTH_SHORT).show();

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