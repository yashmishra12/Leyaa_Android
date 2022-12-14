package com.vijaykumawat.Leyaa;



import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;


// BaseActivity extends AppCompatActivity so do not need to extend here
// you will automatically get parent properties
public class InvitationListActivity extends BaseActivity {

    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();

    private InvitationAdapter adapter;
    Toolbar mToolbar;

    ImageView relaximage;
    int globalCounter = 10;
    TextView relaxtext;



    @Override
    int getContentViewId() {
        return R.layout.invitation_list;
    }

    // action you want to selected - eg. i want home btn to get selected
    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_invitation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.no_room_image).setVisibility(View.GONE);
        findViewById(R.id.relaxtext).setVisibility(View.GONE);
        relaximage = findViewById(R.id.no_room_image);
        relaxtext = findViewById(R.id.relaxtext);

        RecyclerView rcv = findViewById(R.id.recycler_view_invitation);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_bill_split_trans);
        mToolbar.setTitle("Invitation");
        setUpRecyclerView();


        Objects.requireNonNull(rcv.getAdapter()).registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("roomRequest").whereEqualTo("receiverEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs =   task.getResult();
                            ArrayList<DocumentSnapshot> ds = (ArrayList<DocumentSnapshot>) qs.getDocuments();
                            if (ds.size()>0) {
                                relaxtext.setVisibility(View.GONE);
                                relaximage.setVisibility(View.GONE);
                            } else {
                                relaxtext.setVisibility(View.VISIBLE);
                                relaximage.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                });
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("roomRequest").whereEqualTo("receiverEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs =   task.getResult();
                            ArrayList<DocumentSnapshot> ds = (ArrayList<DocumentSnapshot>) qs.getDocuments();
                            if (ds.size()>0) {
                                relaxtext.setVisibility(View.GONE);
                                relaximage.setVisibility(View.GONE);
                            }else {
                                relaxtext.setVisibility(View.VISIBLE);
                                relaximage.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                });
            }
        });






    }


    private void setUpRecyclerView() {


        String userEmailInfo = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

        Query query = mstore.collection("roomRequest").whereEqualTo("receiverEmail",userEmailInfo);

        FirestoreRecyclerOptions<Invitation_Data> options = new FirestoreRecyclerOptions.Builder<Invitation_Data>()
                .setQuery(query, Invitation_Data.class).build();


        adapter = new InvitationAdapter(options);

        ImageView relaximage = findViewById(R.id.no_room_image);
        TextView relaxtext = findViewById(R.id.relaxtext);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_invitation);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new InvitationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {


                adapter.acceptRequest(position);

                DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()){

                            return;
                        }

                        String token = task.getResult();
                        docRef.update("deviceToken", token);
                    }
                });

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
                            relaximage.getLayoutParams().height = 750;
                            relaximage.setVisibility(View.VISIBLE);

                            relaxtext.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            relaxtext.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            relaxtext.setVisibility(View.VISIBLE);
                        }

                    }
                }, 10);

                flag = true;
                break;
            }
        }

        if (!flag) {
            relaximage.setVisibility(View.GONE);
            relaximage.getLayoutParams().width = 0;
            relaximage.getLayoutParams().height = 0;

            relaxtext.setVisibility(View.GONE);
            relaxtext.getLayoutParams().width = 0;
            relaxtext.getLayoutParams().height = 0;
        }


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