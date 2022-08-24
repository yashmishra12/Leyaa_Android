package com.vijaykumawat.Leyaa;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Split_Add_Bill extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> memberIDs;
    ArrayList<SplitBill_Add_Bill_MemberData> userArrayList;
    Split_Add_Bill_Member_Adapter myAdapter;
    FirebaseFirestore db;
    String roomID = "";
    String roomName;
    Button save_bill;
    EditText bill_item_name;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_add_bill_mainactivity);

        Toolbar toolbar= findViewById(R.id.toolbar);
        save_bill = findViewById(R.id.save_bill);
        bill_item_name = findViewById(R.id.bill_item_name);

        FloatingActionButton backButtonRMI = findViewById(R.id.back_button_split_add_bill);

        backButtonRMI.setOnClickListener(view -> {
            finish();
        });



        recyclerView = findViewById(R.id.recyclerView_add_bill);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new CustLinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        userArrayList = new ArrayList<>();
        myAdapter = new Split_Add_Bill_Member_Adapter(Split_Add_Bill.this, userArrayList);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID = extras.getString("roomID");
            populateMemberID();
        }

        db.collection("rooms").document(roomID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    roomName = (String) documentSnapshot.get("title");
                    toolbar.setTitle(roomName);
                }
            }
        });


        save_bill.setOnClickListener(view -> {
            if (bill_item_name.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Item Name is required.", Toast.LENGTH_SHORT).show();
                bill_item_name.requestFocus();
                return;
            }

            String itemName = bill_item_name.getText().toString();

            for (int x = recyclerView.getChildCount(), i = 0; i < x; ++i) {
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                EditText itemPriceTV = holder.itemView.findViewById(R.id.bill_amount_ind);
                String itemPriceString = (itemPriceTV).getText().toString();

                if(itemPriceString.trim().isEmpty() ) {
                    return;
                }

                double itemPrice = Double.parseDouble(itemPriceString);

                if (itemPrice == 0) {
                    continue;
                }

                String contributor = ((TextView)holder.itemView.findViewById(R.id.bill_add_card_memberID)).getText().toString();
                String payer = FirebaseAuth.getInstance().getUid();

                HashMap<String, Object> billObj = new HashMap<>();

                String collectionName = roomID+"_BILLS";

                billObj.put("contributor", contributor);
                billObj.put("itemName", itemName);
                billObj.put("itemPrice", itemPrice);
                billObj.put("payer", payer);
                billObj.put("timestamp", new Date());

                db.collection(collectionName).add(billObj).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        itemPriceTV.setText("0.0");
                        bill_item_name.getText().clear();
                        bill_item_name.requestFocus();
                    }
                });

            }

            Toast.makeText(this, "Bill Added.", Toast.LENGTH_SHORT).show();

        });

        bill_item_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });




    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void populateMemberID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("rooms").document(roomID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                memberIDs = (ArrayList<String>) doc.get("members");

                recyclerView.setAdapter(myAdapter);
                EventChangeListener();

            }
        });
    }

    private void EventChangeListener() {

        for (String memberID: memberIDs) {
            if (memberID.equals(FirebaseAuth.getInstance().getUid())) {
                continue;
            }
            DocumentReference documentReference = db.collection("users").document(memberID);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.exists()) {
                            String fullname = (String) documentSnapshot.get("fullname");
                            String avatar = (String) documentSnapshot.get("avatar");
                            String uid = (String) documentSnapshot.get("uid");


                            SplitBill_Add_Bill_MemberData md = new SplitBill_Add_Bill_MemberData(avatar, fullname, uid);

                            userArrayList.add(md);
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });



        }



    }


}
