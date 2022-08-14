package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


// BaseActivity extends AppCompatActivity so do not need to extend here
// you will automatically get parent properties
public class RoomListActivity extends BaseActivity {



//    private FirebaseAuth mAuth;
    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private RoomAdapter adapter;
    FloatingActionButton floatingButton;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpRecyclerView();

    }


    private void setUpRecyclerView() {


        floatingButton =  findViewById(R.id.floatingActionButton);
        floatingButton.setOnClickListener( v -> startActivity(new Intent(this, Room_Creation.class)));


        Query query = mstore.collection("rooms").whereArrayContains("members",mUid );
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
                String document_ID = documentSnapshot.getId();
                startActivity(new Intent(RoomListActivity.this, Inside_Room_Activity.class).putExtra("document_ID",document_ID ));


                //Toast.makeText(RoomListActivity.this, "Position: " + position + " ID: " + String.valueOf(query2), Toast.LENGTH_SHORT).show();

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
