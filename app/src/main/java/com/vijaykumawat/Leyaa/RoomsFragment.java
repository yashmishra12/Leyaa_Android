package com.vijaykumawat.Leyaa;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


public class RoomsFragment extends Fragment {

    View view;
    private FirebaseAuth mAuth;
    FloatingActionButton floatingButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_rooms, container, false);
        floatingButton =  view.findViewById(R.id.floatingActionButton);
        floatingButton.setOnClickListener( v -> startActivity(new Intent(getContext(), room_create_view.class)));


        return view;

    }

}