package com.vijaykumawat.Leyaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vijaykumawat.Leyaa.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

   BottomNavigationView bottomNavigationView;

   RoomsFragment roomsFragment = new RoomsFragment();
   ReminderFragment reminderFragment = new ReminderFragment();
   InvitationFragment invitationFragment = new InvitationFragment();
   ProfileFragment profileFragment = new ProfileFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, roomsFragment).commit();

//        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.invitation_nav);
//        badgeDrawable.setVisible(true);
//        badgeDrawable.setNumber(8);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){

                    case R.id.rooms_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, roomsFragment).commit();
                        return true;

                    case R.id.reminder_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, reminderFragment).commit();
                        return true;

                    case R.id.invitation_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, invitationFragment).commit();
                        return true;

                    case R.id.profile_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, profileFragment).commit();
                        return true;
                }

                return false;
            }
        });

    }

    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}