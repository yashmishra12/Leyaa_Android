package com.vijaykumawat.Leyaa;

import android.os.Bundle;



public class ProfileActivity extends BaseActivity {


    @Override
    int getContentViewId() {
        return R.layout.profile_activity;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
