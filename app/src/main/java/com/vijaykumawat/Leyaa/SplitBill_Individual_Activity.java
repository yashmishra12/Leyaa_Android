package com.vijaykumawat.Leyaa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplitBill_Individual_Activity extends AppCompatActivity {

    String roomID;
    String memberID;
    TextView userID_tv;
    TextView roomID_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splitbill_individual);

        userID_tv = findViewById(R.id.userID_SBI);
        roomID_tv = findViewById(R.id.roomID_SBI);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomID =  extras.getString("roomID");
            memberID = extras.getString("memberID");

            roomID_tv.setText(roomID);
            userID_tv.setText(memberID);

//            toolbar.setTitle(extras.getString("roomName"));
        }


    }
}
