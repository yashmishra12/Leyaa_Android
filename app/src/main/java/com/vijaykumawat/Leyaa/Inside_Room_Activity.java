package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Inside_Room_Activity extends BaseActivity {
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    String roomName="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        Window window = getWindow();
        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);



        toolbar.setTitle(roomName);



        nav=(NavigationView)findViewById(R.id.navmenu);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);

        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.message_wall:
                        Toast.makeText(getApplicationContext(),"Message ",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.bill_split:
                        Toast.makeText(getApplicationContext(),"Bill Split",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.edit_room_name_ic:
                        //Toast.makeText(getApplicationContext(),"Edit Room Name",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Inside_Room_Activity.this, EditRoomName.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.doing_laundry:
                        Toast.makeText(getApplicationContext(),"Doing Laundry",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.shopping:
                        Toast.makeText(getApplicationContext(),"Going for Shopping ",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.fridge_is_full:
                        Toast.makeText(getApplicationContext(),"Fridge is Full ",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.clean_house:
                        Toast.makeText(getApplicationContext(),"Clean House",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }

                return true;
            }
        });



    }

    @Override
    int getContentViewId() {
        return R.layout.inside_room_activity;
    }

    // action you want to selected - eg. i want home btn to get selected
    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_room;
    }

}

