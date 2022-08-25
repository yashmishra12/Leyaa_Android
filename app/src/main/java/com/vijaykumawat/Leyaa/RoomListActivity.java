package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;


// BaseActivity extends AppCompatActivity so do not need to extend here
// you will automatically get parent properties
public class RoomListActivity extends BaseActivity {


//    private FirebaseAuth mAuth;
final Handler handler = new Handler();
    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    private RoomAdapter adapter;
    FloatingActionButton floatingButton;
    ImageView relaximage;
    int globalCounter = 10;
    Toolbar mToolbar;

    String mUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


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
        findViewById(R.id.no_room_image).setVisibility(View.GONE);
        relaximage = findViewById(R.id.no_room_image);
        relaximage.setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_bill_split_trans);
        mToolbar.setTitle("Rooms");
        setUpRecyclerView();

        // listen for changes to show image
        Objects.requireNonNull(recyclerView.getAdapter()).registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //db.collection("rooms").whereArrayContains("members", FirebaseAuth.getInstance().getCurrentUser()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                  db.collection("rooms").whereArrayContains("members",FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            showImage();

                        }
                    }
                });
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("rooms").whereArrayContains("members", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            showImage();


                        }
                    }
                });
            }
        });

    }


    private void setUpRecyclerView() {

        relaximage.setVisibility(View.GONE);
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
            }
        });

        showImage();

    }

    public void showImage() {
        int i;
        boolean flag = false;
        for(i=globalCounter; i>0; i--) {
            if(adapter.getItemCount()==0) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getItemCount()==0) {
                            relaximage.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            relaximage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            relaximage.setVisibility(View.VISIBLE);
                        }

                    }
                }, 500);

                flag = true;
                break;
            }
        }

       if (!flag) {
           relaximage.setVisibility(View.GONE);
           relaximage.getLayoutParams().width = 0;
           relaximage.getLayoutParams().height = 0;
       }

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
