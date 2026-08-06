package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    protected Toolbar toolbar;
    protected TextView toolbarTitle;  // <-- ADD THIS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        // 1) Find toolbar & title textview
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // disable default title
        }
        toolbarTitle = toolbar.findViewById(R.id.toolbarTitle);

        // 2) Bottom navigation setup
        navigationView = findViewById(R.id.navigation);
        if (navigationView != null) {
            navigationView.setOnNavigationItemSelectedListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

//        if (item.getItemId() == getNavigationMenuItemId()) {
//            return true;
//        }

        switch (item.getItemId()) {

            case R.id.navigation_room:
                startActivity(new Intent(this, RoomListActivity.class));
                break;

            case R.id.navigation_reminder:
                startActivity(new Intent(this, Alarm_MainActivity.class));
                break;

            case R.id.navigation_invitation:
                startActivity(new Intent(this, InvitationListActivity.class));
                break;

            case R.id.navigation_profile:
                startActivity(new Intent(this, ProfilePage.class));
                break;
        }

        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
        updateToolbarTitleFromMenuItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        if (navigationView == null) return;
        MenuItem item = navigationView.getMenu().findItem(itemId);
        if (item != null) {
            item.setChecked(true);
        }
    }

    // UPDATE CENTERED TOOLBAR TITLE
    void updateToolbarTitleFromMenuItem(int itemId) {
        if (navigationView == null || toolbarTitle == null) return;

        MenuItem item = navigationView.getMenu().findItem(itemId);
        if (item != null) {
            toolbarTitle.setText(item.getTitle()); // <-- center title
        }
    }

    abstract int getContentViewId();
    abstract int getNavigationMenuItemId();
}
