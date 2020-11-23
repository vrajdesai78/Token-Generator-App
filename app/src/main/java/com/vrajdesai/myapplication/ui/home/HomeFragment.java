package com.vrajdesai.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vrajdesai.myapplication.R;
import com.vrajdesai.myapplication.ShowPlaces;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button health,corporate,personal;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        health = root.findViewById(R.id.health_btn);
        corporate = root.findViewById(R.id.corporate_btn);
        personal = root.findViewById(R.id.personal_btn);

        health.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowPlaces.class);
                intent.putExtra("Category","Hospitals");
                startActivity(intent);
            }
        });

        corporate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowPlaces.class);
                intent.putExtra("Category","Corporates");
                startActivity(intent);
            }
        });

        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowPlaces.class);
                intent.putExtra("Category","Personal Care");
                startActivity(intent);
            }
        });

        return root;
    }
}