package com.vijaykumawat.Leyaa;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatRoom extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton back_btn;
    String roomName;
    String roomID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        toolbar = findViewById(R.id.toolbar);
        back_btn = findViewById(R.id.char_room_back_flt_btn);

        Bundle extras = getIntent().getExtras();

        if (extras != null ) {
            roomName =  extras.getString("roomName");
            roomID = extras.getString("roomID");
        }

        toolbar.setTitle(roomName);


        back_btn.setOnClickListener(view -> finish());


    }
}
