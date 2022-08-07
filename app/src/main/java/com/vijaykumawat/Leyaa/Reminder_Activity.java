package com.vijaykumawat.Leyaa;

import android.os.Bundle;




public class Reminder_Activity extends BaseActivity {


    @Override
    int getContentViewId() {
        return R.layout.reminder_activity;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_reminder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
