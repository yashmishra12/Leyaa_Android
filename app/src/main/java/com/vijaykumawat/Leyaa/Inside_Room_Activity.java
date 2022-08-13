package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class Inside_Room_Activity extends BaseActivity {
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    String roomName="";
    String roomID = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar= findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        FloatingActionButton roomBackButton = findViewById(R.id.roomBackButton);

        roomBackButton.setOnClickListener(view -> {
            finish();
        });

        if (extras != null) {
            roomID =  extras.getString("document_ID");

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




        Window window = getWindow();
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

                String tokenKK = "fO1yfYWjSeGTdQSKeXfhzW:APA91bFb5S95iYkjAS3t3qcDdHQ8SaX2A-wC6orNjwZZ83VdtOfLRfNQzxxphi75PfibE0Y_TN6moM8aZZseSSHVU8zadV9XSnyyFr4geDdnpP4_ye-rH3MuqjNytIDzPA8eJN37FQJc";
                String tokenYM = "";
                String title = "Leyaa";
                String message = "L Block - Do Laundry";

                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(tokenYM, title, message, getApplicationContext(), Inside_Room_Activity.this );
                notificationsSender.SendNotifications();

                Toast.makeText(getApplicationContext(),"Doing Laundry",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.leave_room_ic){
              leaveRoom();
              startActivity(new Intent(Inside_Room_Activity.this,RoomListActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.shopping){
                Toast.makeText(getApplicationContext(),"Going for Shopping ",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.fridge_is_full){
                Toast.makeText(getApplicationContext(),"Fridge is Full ",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if (menuItem.getItemId()==R.id.clean_house){
                Toast.makeText(getApplicationContext(),"Clean House",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            else if(menuItem.getItemId() == R.id.memberInfo) {
                Intent intent = new Intent(Inside_Room_Activity.this, RoomMemberInfo.class);
                intent.putExtra("roomID",roomID);
                intent.putExtra("roomName", roomName);
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

        String documentid = "";


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            documentid =  extras.getString("document_ID");

        }
        DocumentReference docRef = db.collection("rooms").document(documentid);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String finalDocumentid = documentid;

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {

                        db.collection("rooms").document(finalDocumentid).update("members", FieldValue.arrayRemove(userId));

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

