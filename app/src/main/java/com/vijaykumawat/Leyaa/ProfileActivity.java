package com.vijaykumawat.Leyaa;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


@RequiresApi(api = Build.VERSION_CODES.N)
public class ProfileActivity extends BaseActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View view;
    String currentAvatar = "";

    String[] itemName = SanitizeItemName.realAssetName;
    int[] itemImages = {R.drawable.almond, R.drawable.apple, R.drawable.avacado, R.drawable.bagel, R.drawable.banana,
            R.drawable.bandaid, R.drawable.battery, R.drawable.beer, R.drawable.bellpepper, R.drawable.biscuit, R.drawable.bodylotion,
            R.drawable.bodywash, R.drawable.boot, R.drawable.bottle, R.drawable.bread, R.drawable.broccoli, R.drawable.broom, R.drawable.bucket,
            R.drawable.bugspray, R.drawable.bulb, R.drawable.butter, R.drawable.cabbage};


    @Override
    int getContentViewId() {
        return R.layout.profile_activity;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        GridAdapter gridAdapter = new GridAdapter(this, itemName ,itemImages);

        TextView email = (TextView) findViewById(R.id.email);
        Button resetButton = (Button) findViewById(R.id.resetAvatar);
        Button saveButton = (Button) findViewById(R.id.saveAvatar);

        TextView fullname = (TextView) findViewById(R.id.fullName);
        ImageView profileAvatar = (ImageView) findViewById(R.id.profileAvatar);
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = SanitizeItemName.sanitizeItemName(itemName[position]);
                String userAvatar = (String) name;
                int resID = getResources().getIdentifier(userAvatar , "drawable", getPackageName());
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
                        fullname.setText(userName);

                        String userAvatar = (String) doc.get("avatar");
                        currentAvatar = userAvatar;
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



        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", email.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(ProfileActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });


        fullname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Edit Text", fullname.getText().toString());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(ProfileActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });

//        saveButton.setOnClickListener(view -> {
////            documentReference.update()
//            documentReference.update("avatar").addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot doc = task.getResult();
//                        if (doc.exists()) {
//                            Log.d("TAG", "DocumentSnapshot data: " + doc.get("email"));
//                            String userEmail  = (String) doc.get("email");
//                            email.setText(userEmail);
//
//                            String userName  = (String) doc.get("fullname");
//                            fullname.setText(userName);
//
//                            String userAvatar = (String) doc.get("avatar");
//                            currentAvatar = userAvatar;
//                            int resID = getResources().getIdentifier(userAvatar , "drawable", getPackageName());
//
//                            profileAvatar.setImageResource(resID);
//
//                        } else {
//                            Log.d("TAG", "DocumentSnapshot No such document");
//                        }
//                    }
//                    else {
//                        Log.d("TAG", "DocumentSnapshot get failed with ", task.getException());
//                    }
//                }
//            });
//
//
//        });

    }



}
