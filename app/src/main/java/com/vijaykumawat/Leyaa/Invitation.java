package com.vijaykumawat.Leyaa;

import android.os.Bundle;


public class Invitation extends  BaseActivity {
    @Override
    int getContentViewId() {
        return R.layout.invitation_activity;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_invitation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do not use setContentView(R.id.activity_home) only pass it in getContentViewId();

    }
}
