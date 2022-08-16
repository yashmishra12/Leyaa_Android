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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ProfilePage extends BaseActivity {



    @Override
    int getContentViewId() {
        return R.layout.profile_page;
    }

    // action you want to selected - eg. i want home btn to get selected
    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_profile;
    }


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String[] itemName = SanitizeItemName.realAssetName;
    int[] itemImages = {R.drawable.almond, R.drawable.apple, R.drawable.avocado, R.drawable.bagel, R.drawable.banana,
            R.drawable.bandaid, R.drawable.battery, R.drawable.beer, R.drawable.bellpepper, R.drawable.biscuit, R.drawable.bodylotion,
            R.drawable.bodywash, R.drawable.boot, R.drawable.bottle, R.drawable.bread, R.drawable.broccoli, R.drawable.broom, R.drawable.bucket,
            R.drawable.bugspray, R.drawable.bulb, R.drawable.butter, R.drawable.cabbage, R.drawable.calculator, R.drawable.candle, R.drawable.canopener,
    R.drawable.cap, R.drawable.carrot, R.drawable.cashew, R.drawable.cauliflower, R.drawable.cereal, R.drawable.cheese, R.drawable.cherry,
            R.drawable.chicken, R.drawable.chilli, R.drawable.chip, R.drawable.chocolate, R.drawable.choppingboard, R.drawable.cigarette,
            R.drawable.cleaner, R.drawable.clock, R.drawable.clove, R.drawable.coffee, R.drawable.cola, R.drawable.conditioner, R.drawable.condom,
            R.drawable.contactlen, R.drawable.converse, R.drawable.cookie, R.drawable.cookingoil, R.drawable.corn, R.drawable.cornflake, R.drawable.croc,
            R.drawable.cucumber, R.drawable.deodorant, R.drawable.detergent, R.drawable.dishwasher, R.drawable.donut, R.drawable.egg,
            R.drawable.eggplant, R.drawable.energydrink, R.drawable.facewash, R.drawable.fish, R.drawable.flour, R.drawable.flower, R.drawable.fork,
            R.drawable.formalshoe, R.drawable.freshener, R.drawable.garlic, R.drawable.ginger, R.drawable.glove, R.drawable.glue,
            R.drawable.grape, R.drawable.guava, R.drawable.handwash, R.drawable.hat, R.drawable.highheel, R.drawable.honey, R.drawable.icecream,
            R.drawable.jalapeno, R.drawable.jam, R.drawable.jelly, R.drawable.juice, R.drawable.ketchup, R.drawable.kitchenroll, R.drawable.knife,
            R.drawable.lamp, R.drawable.lemon, R.drawable.lentil, R.drawable.lobster, R.drawable.lube, R.drawable.mango, R.drawable.meat,
            R.drawable.milk, R.drawable.mitten, R.drawable.mop, R.drawable.mouthwash, R.drawable.mug, R.drawable.musclecream, R.drawable.mushroom,
            R.drawable.nailclipper, R.drawable.nailpolish, R.drawable.napkin, R.drawable.notebook, R.drawable.okra, R.drawable.onion,
            R.drawable.orange, R.drawable.pad, R.drawable.pea, R.drawable.peanutbutter, R.drawable.pen, R.drawable.pencil, R.drawable.pineapple,
            R.drawable.plate, R.drawable.pomegranate, R.drawable.popcorn, R.drawable.potato, R.drawable.pulse, R.drawable.pumpkin,
            R.drawable.radish, R.drawable.rainboot, R.drawable.ramen, R.drawable.rice, R.drawable.rum, R.drawable.runningshoe, R.drawable.salt,
            R.drawable.sanitizer, R.drawable.sausage, R.drawable.scissor, R.drawable.shampoo, R.drawable.shoe, R.drawable.slipper,
            R.drawable.smoke, R.drawable.smoothie, R.drawable.snack, R.drawable.soap, R.drawable.soda, R.drawable.spinach,
            R.drawable.spoon, R.drawable.sportsshoe, R.drawable.squash, R.drawable.stirfry, R.drawable.strawberry, R.drawable.studylamp,
            R.drawable.sugar, R.drawable.sunglasse, R.drawable.sunscreen, R.drawable.sweetpotato, R.drawable.tampon, R.drawable.tea,
            R.drawable.thermometer, R.drawable.tissuepaper, R.drawable.toiletbrush, R.drawable.toiletpaper, R.drawable.tomato,
            R.drawable.toothbrush, R.drawable.toothpaste, R.drawable.tortilla, R.drawable.trashbag, R.drawable.tuna, R.drawable.turkey,
            R.drawable.turnip, R.drawable.vacuumcleaner, R.drawable.vodka, R.drawable.watch, R.drawable.watermelon, R.drawable.wetwipe,
            R.drawable.whiskey, R.drawable.wine, R.drawable.yogurt, R.drawable.imagenotfound};

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
//        setContentView(R.layout.profile_page);

        GridAdapter gridAdapter = new GridAdapter(this, itemName ,itemImages);

        TextView email = findViewById(R.id.email);
        Button resetButton = findViewById(R.id.resetAvatar);
        Button saveButton = findViewById(R.id.saveAvatar);
        Button signoutButton = findViewById(R.id.signout_button_profile);
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

                new AlertDialog.Builder(view.getContext(),R.style.AlertDialog)
                        .setTitle("Are you sure?")
                        .setMessage("Sad to see you leave")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            DocumentReference docRef = db.collection("users").document(userID);

                            docRef.update("deviceToken", "").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(ProfilePage.this, LoginPage.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//make sure user cant go back
                                    startActivity(intent);
                                }
                            });

                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();






            }
        });


        //name change transition
        editName.setOnClickListener(view -> {
            Intent intent = new Intent(ProfilePage.this, ProfilenameChange.class);
            intent.putExtra("nameValue", fullName.getText().toString());
            startActivity(intent);
        });



    }



}
