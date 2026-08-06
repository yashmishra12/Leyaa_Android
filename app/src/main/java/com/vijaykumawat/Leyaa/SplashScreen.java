package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        handleNotificationSetup();
    }

    private void handleNotificationSetup() {

        // Android 13+ → ask for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // ASK FOR PERMISSION → DO NOT NAVIGATE YET
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST
                );
                return; // STOP here. Do NOT go to next screen yet.
            }
        }

        // Permission already granted OR device < Android 13 → proceed normally
        proceedToNextScreen();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // After user taps Allow / Deny → now move to next screen
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            proceedToNextScreen();
        }
    }

    private void proceedToNextScreen() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent i = new Intent(SplashScreen.this, RoomListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else {
            Intent i = new Intent(SplashScreen.this, LoginPage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
}
