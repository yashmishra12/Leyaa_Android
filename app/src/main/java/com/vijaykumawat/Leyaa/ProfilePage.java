package com.vijaykumawat.Leyaa;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ProfilePage extends AppCompatActivity {


//    @Override
//    int getContentViewId() {
//        return R.layout.profile_page;
//    }

//    @Override
//    int getNavigationMenuItemId() {
//        return R.id.navigation_user;
//    }


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String[] itemName = SanitizeItemName.realAssetName;
    int[] itemImages = {R.drawable.almond, R.drawable.apple, R.drawable.avocado, R.drawable.bagel, R.drawable.banana,
            R.drawable.bandaid, R.drawable.battery, R.drawable.beer, R.drawable.bellpepper, R.drawable.biscuit, R.drawable.bodylotion,
            R.drawable.bodywash, R.drawable.boot, R.drawable.bottle, R.drawable.bread, R.drawable.broccoli, R.drawable.broom, R.drawable.bucket,
            R.drawable.bugspray, R.drawable.bulb, R.drawable.butter, R.drawable.cabbage};

    @Override
    public void onResume(){
        super.onResume();

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        TextView fullname = (TextView) findViewById(R.id.fullName);

                        String userName  = (String) doc.get("fullname");
                        fullname.setText(userName);

                    } else {
                        Log.d("TAG", "DocumentSnapshot No such document");
                    }
                }
                else {
                    Log.d("TAG", "DocumentSnapshot get failed with ", task.getException());
                }
            }
        });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        GridAdapter gridAdapter = new GridAdapter(this, itemName ,itemImages);

        TextView email = (TextView) findViewById(R.id.email);
        Button resetButton = (Button) findViewById(R.id.resetAvatar);
        Button saveButton = (Button) findViewById(R.id.saveAvatar);
        Button signoutButton = findViewById(R.id.signout_button_profile);
        Button backButton = findViewById(R.id.profileBackButton);
        Button editName = findViewById(R.id.editNameButton);

        final String[] avatar = {""};
        final String[] selectedAvatar = {""};


        TextView fullName =  findViewById(R.id.fullName);
        ImageView profileAvatar =  findViewById(R.id.profileAvatar);
        GridView gridView =  findViewById(R.id.gridView);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String avatarName = SanitizeItemName.sanitizeItemName(itemName[position]);
                selectedAvatar[0] = avatarName;
                int resID = getResources().getIdentifier(avatarName , "drawable", getPackageName());
                profileAvatar.setImageResource(resID);

            }
        });



        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);


        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + doc.get("email"));
                        String userEmail  = (String) doc.get("email");
                        email.setText(userEmail);

                        String userName  = (String) doc.get("fullname");
                        fullName.setText(userName);

                        String userAvatar = (String) doc.get("avatar");
                        selectedAvatar[0] = userAvatar;
                        avatar[0] = userAvatar;
                        int resID = getResources().getIdentifier(userAvatar , "drawable", getPackageName());

                        profileAvatar.setImageResource(resID);

                    } else {
                        Log.d("TAG", "DocumentSnapshot No such document");
                    }
                }
                else {
                    Log.d("TAG", "DocumentSnapshot get failed with ", task.getException());
                }
            }
        });


// email copied
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", email.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(ProfilePage.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });

    // Full Name Copied
        fullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", fullName.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(ProfilePage.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });

//        save button clicked
        saveButton.setOnClickListener(view -> {
            String avatarToUpload = selectedAvatar[0];

            documentReference
                    .update("avatar", avatarToUpload)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            avatar[0] = avatarToUpload;
                            selectedAvatar[0] = avatarToUpload;
                            Toast.makeText(ProfilePage.this, "Saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error updating document", e);
                        }
                    });
        });


        // On Reset Click
        resetButton.setOnClickListener(view -> {
            String avatarName = avatar[0];
            int resID = getResources().getIdentifier(avatarName , "drawable", getPackageName());
            profileAvatar.setImageResource(resID);
        });



//         Sign Out Button pressed
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfilePage.this, LoginPage.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//make sure user cant go back
                startActivity(intent);
            }
        });


        //name change transition
        editName.setOnClickListener(view -> {
            Intent intent = new Intent(ProfilePage.this, ProfilenameChange.class);
            intent.putExtra("nameValue", fullName.getText().toString());
            startActivity(intent);
        });

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfilePage.this, RoomListActivity.class);
            startActivity(intent);
        });

    }



}
