package com.vijaykumawat.Leyaa;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Inside_Room_Activity extends BaseActivity {
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    String roomName = "";
    String roomID = "";
    String userName = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference itemRef;
    RecyclerView.LayoutManager layoutManager;
    private ItemAdapter itemAdapter;

    ArrayList<String> memberList;

    private void setUpRecyclerView(){
        itemRef = db.collection("rooms");

        itemRef.document(roomID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent (@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                ArrayList<HashMap<String, String>> item_data_models = (ArrayList<HashMap<String, String>>) value.get("newItems");
                RecyclerView rcv = findViewById(R.id.item_rv);

                layoutManager = new GridLayoutManager(Inside_Room_Activity.this, 2);
                rcv.setHasFixedSize(true);
                rcv.setLayoutManager(layoutManager);
                itemAdapter = new ItemAdapter(item_data_models, roomID);

                rcv.setAdapter(itemAdapter);


                if (itemAdapter.getItemCount()  > 0 ) {
                    rcv.smoothScrollToPosition(itemAdapter.getItemCount()-1);
                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar= findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        FloatingActionButton roomBackButton = findViewById(R.id.roomBackButton);

        roomBackButton.setOnClickListener(view -> finish());

        if (extras != null) {
            roomID =  extras.getString("document_ID");
            setUpRecyclerView();

            DocumentReference docRef = db.collection("rooms").document(roomID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {

                            roomName = (String) documentSnapshot.get("title");
                            toolbar.setTitle(roomName);
                        }
                    }
                }
            });

            DocumentReference userRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot ds = task.getResult();
                        userName = (String) ds.get("fullname");
                    }
                }
            });

        }

        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        nav= findViewById(R.id.navmenu);
        drawerLayout= findViewById(R.id.drawer);

        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {@Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            if (menuItem.getItemId()==R.id.message_wall) {
                Toast.makeText(getApplicationContext(),"Message ",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.bill_split) {
                Toast.makeText(getApplicationContext(),"Bill Split",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.edit_room_name_ic) {
                Intent intent = new Intent(Inside_Room_Activity.this, EditRoomName.class);
                intent.putExtra("roomName", roomName);
                intent.putExtra("roomID",roomID);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.doing_laundry){


                DocumentReference docRef =  db.collection("rooms").document(roomID);


                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                           memberList = (ArrayList<String>) documentSnapshot.get("members");

                            assert memberList != null;
                            for (String member: memberList) {
                               DocumentReference userRef = db.collection("users").document(member);

                               userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                   @Override
                                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                       if (task.isSuccessful()) {
                                           DocumentSnapshot ds = task.getResult();
                                           String userDeviceToken = (String) ds.get("deviceToken");
                                           String title = "Room: "+roomName;
                                           String message = userName+" is going to do laundry";
                                           FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), Inside_Room_Activity.this );
                                           notificationsSender.SendNotifications();
                                       }
                                   }
                               });
                           }

                        }
                    }
                });






                Toast.makeText(getApplicationContext(),"Everyone Notified",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.leave_room_ic){
              leaveRoom();
              startActivity(new Intent(Inside_Room_Activity.this,RoomListActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.shopping){
                DocumentReference docRef =  db.collection("rooms").document(roomID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            memberList  = (ArrayList<String>) documentSnapshot.get("members");

                            assert memberList != null;
                            for (String member: memberList) {
                                DocumentReference userRef = db.collection("users").document(member);

                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot ds = task.getResult();
                                            String userDeviceToken = (String) ds.get("deviceToken");
                                            String title = "Room: "+roomName;
                                            String message = userName+" is going for shopping.";
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), Inside_Room_Activity.this );
                                            notificationsSender.SendNotifications();
                                        }
                                    }
                                });
                            }

                        }
                    }
                });

                Toast.makeText(getApplicationContext(),"Everyone Notified",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.fridge_is_full){
                DocumentReference docRef =  db.collection("rooms").document(roomID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            memberList  = (ArrayList<String>) documentSnapshot.get("members");

                            assert memberList != null;
                            for (String member: memberList) {
                                DocumentReference userRef = db.collection("users").document(member);

                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot ds = task.getResult();
                                            String userDeviceToken = (String) ds.get("deviceToken");
                                            String title = "Room: "+roomName;
                                            String message = "Fridge is full. Please look into it.";
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), Inside_Room_Activity.this );
                                            notificationsSender.SendNotifications();
                                        }
                                    }
                                });
                            }

                        }
                    }
                });

                Toast.makeText(getApplicationContext(),"Everyone Notified",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.clean_house){
                DocumentReference docRef =  db.collection("rooms").document(roomID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            memberList  = (ArrayList<String>) documentSnapshot.get("members");

                            assert memberList != null;
                            for (String member: memberList) {
                                DocumentReference userRef = db.collection("users").document(member);

                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot ds = task.getResult();
                                            String userDeviceToken = (String) ds.get("deviceToken");
                                            String title = "Room: "+roomName;
                                            String message = userName+" feels it's time to clean the house.";
                                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userDeviceToken, title, message, getApplicationContext(), Inside_Room_Activity.this );
                                            notificationsSender.SendNotifications();
                                        }
                                    }
                                });
                            }

                        }
                    }
                });
                Toast.makeText(getApplicationContext(),"Everyone Notified",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }


            else if(menuItem.getItemId() == R.id.memberInfo) {
                Intent intent = new Intent(Inside_Room_Activity.this, RoomMemberInfo.class);
                intent.putExtra("roomID",roomID);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId() == R.id.add_member) {
                Intent intent = new Intent(Inside_Room_Activity.this, MemberAdd.class);
                intent.putExtra("roomID",roomID);
                intent.putExtra("roomName", roomName);
                intent.putExtra("userName",userName);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            return true;
        }

        });





    }



    @Override
    protected void onResume(){
        super.onResume();

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);

        DocumentReference docRef = db.collection("rooms").document(roomID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {

                        roomName = (String) documentSnapshot.get("title");



                        toolbar.setTitle(roomName);
                    }

                }

            }

        });
    }


    private void leaveRoom() {

        String roomID = "";

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("document_ID");
        }

        DocumentReference docRef = db.collection("rooms").document(roomID);
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        String finalRoomID = roomID;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        db.collection("rooms").document(finalRoomID).update("members", FieldValue.arrayRemove(userId));

                        ArrayList<String> memberCount = (ArrayList<String>) documentSnapshot.get("members");


                        if (memberCount != null && memberCount.size()==1) {
                            db.collection("rooms").document(finalRoomID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("TAG", "onSuccess: ");
                                }
                            });


                        }


                    }
                }
            }
        });





    }

    @Override
    int getContentViewId() {
        return R.layout.inside_room_activity;
    }

    // action you want to selected - eg. i want home btn to get selected
    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_room;
    }

}

