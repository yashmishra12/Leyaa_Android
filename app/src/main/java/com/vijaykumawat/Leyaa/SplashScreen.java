package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.splash_screen);

       FirebaseAuth auth = FirebaseAuth.getInstance();
       if (auth.getCurrentUser() != null) {
           Intent i = new Intent(SplashScreen.this, RoomListActivity.class);
           i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//make sure user cant go back
           startActivity(i);
       }
       else {
           Intent i = new Intent(SplashScreen.this, LoginPage.class);
           i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//make sure user cant go back
           startActivity(i);
       }
    }

}
