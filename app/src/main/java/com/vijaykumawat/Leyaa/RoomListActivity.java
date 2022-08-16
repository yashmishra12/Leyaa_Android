package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        relaximage = findViewById(R.id.relaximage);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        setUpRecyclerView();



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
                            QuerySnapshot qs =   task.getResult();
                            ArrayList<DocumentSnapshot> ds = (ArrayList<DocumentSnapshot>) qs.getDocuments();


                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (ds.size()>0) {

                                        relaximage.setVisibility(View.GONE);
                                    } else {

                                        relaximage.setVisibility(View.VISIBLE);
                                    }
                                }
                            }, 10);




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
                            QuerySnapshot qs =   task.getResult();
                            ArrayList<DocumentSnapshot> ds = (ArrayList<DocumentSnapshot>) qs.getDocuments();

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (ds.size()>0) {

                                        relaximage.setVisibility(View.GONE);
                                    } else {

                                        relaximage.setVisibility(View.VISIBLE);
                                    }
                                }
                            }, 10);

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

        if(adapter.getItemCount()==0){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    relaximage.setVisibility(View.VISIBLE);
                }
            }, 10);

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
