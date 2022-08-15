package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Edit_Item extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        FloatingActionButton backButton = findViewById(R.id.edit_item_back_flt_btn);
        EditText edit_name = findViewById(R.id.item_edit_name);
        EditText edit_qty = findViewById(R.id.item_edit_qty);
        EditText edit_desc = findViewById(R.id.item_edit_desc);
        Button savebtn = findViewById(R.id.edit_item_save);

        String itemID = "";
        String roomID = "";
        String qty = "";
        String name = "";
        String desc = "";


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            itemID =  extras.getString("itemID");


            qty = extras.getString("item_qty");
            name = extras.getString("item_name");
            desc = extras.getString("item_desc");


            edit_name.setText(name);
            edit_qty.setText(qty);
            edit_desc.setText(desc);

        }

        String finalItemID = itemID;
        String finalRoomID = roomID;

        String finalDesc = desc;
        String finalName = name;
        String finalQty = qty;


        savebtn.setOnClickListener(view -> {

            if (!edit_name.getText().toString().isEmpty()) {
                HashMap<String, String> itemToDel = new HashMap<>();

                itemToDel.put("id", finalItemID);
                itemToDel.put("desc", finalDesc);
                itemToDel.put("name", finalName);
                itemToDel.put("qty", finalQty);



                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("rooms").document(finalRoomID).update("newItems", FieldValue.arrayRemove(itemToDel)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Edit_Item.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    }
                });


                HashMap<String, String> itemToAdd = new HashMap<>();

                itemToAdd.put("id", finalItemID);
                itemToAdd.put("desc", edit_desc.getText().toString());
                itemToAdd.put("name", edit_name.getText().toString());
                itemToAdd.put("qty", edit_qty.getText().toString());



                db.collection("rooms").document(finalRoomID).update("newItems", FieldValue.arrayUnion(itemToAdd)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Edit_Item.this, "Item Edited", Toast.LENGTH_SHORT).show();
                    }

                });

                finish();
            } else {
                Toast.makeText(this, "Item Name is missing", Toast.LENGTH_SHORT).show();
            }

        });




        backButton.setOnClickListener(view -> {
            finish();
        });


    }
}
