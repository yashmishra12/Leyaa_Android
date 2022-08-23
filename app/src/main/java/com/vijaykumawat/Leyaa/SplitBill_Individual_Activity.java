package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SplitBill_Individual_Activity extends AppCompatActivity {

    String roomID;
    String memberID;
    TextView userID_tv;
    TextView roomID_tv;
    FloatingActionButton bill_split_member_back_flt_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_split_individual);


        userID_tv = findViewById(R.id.userID_SBI);
        roomID_tv = findViewById(R.id.roomID_SBI);
        bill_split_member_back_flt_btn = findViewById(R.id.bill_split_member_back_flt_btn);


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            memberID = extras.getString("memberID");

            roomID_tv.setText(roomID);
            userID_tv.setText(memberID);
        }


        bill_split_member_back_flt_btn.setOnClickListener(view -> {
            finish();
        });


    }
}
