package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class Add_Item extends AppCompatActivity {
    FloatingActionButton add_item_flt_btn_back;
    String roomID;
    EditText itemName;
    EditText itemQty;
    EditText itemDesc;
    Button add_btn;
    ImageView add_item_imgview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        itemName = findViewById(R.id.new_item_name);
        itemQty = findViewById(R.id.new_item_qty);
        itemDesc = findViewById(R.id.new_item_desc);
        add_item_imgview = findViewById(R.id.shopping_cart_image);
        itemName.requestFocus();

        add_item_flt_btn_back = findViewById(R.id.add_item_flt_btn_back);

        add_item_flt_btn_back.setOnClickListener(view -> {
            finish();
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID = extras.getString("roomID");
        }

        add_btn = findViewById(R.id.new_item_add_btn);

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!itemName.getText().toString().isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    String uniqueID = UUID.randomUUID().toString();

                    HashMap<String, String> newItem = new HashMap<>();
                    newItem.put("id", uniqueID);
                    newItem.put("name", itemName.getText().toString());
                    newItem.put("qty", itemQty.getText().toString());
                    newItem.put("desc", itemDesc.getText().toString());


                    db.collection("rooms").document(roomID).update("newItems", FieldValue.arrayUnion(newItem)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection("rooms").document(roomID).update("lastItemID", uniqueID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    itemName.setText("");
                                    itemQty.setText("");
                                    itemDesc.setText("");

                                    itemName.requestFocus();

                                    Toast.makeText(Add_Item.this, "Item Added", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }else {
                    Toast.makeText(Add_Item.this, "Item name is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
