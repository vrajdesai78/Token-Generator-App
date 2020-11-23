package com.vrajdesai.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShowPlaces extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private RecyclerView placesList;
    private FirestoreRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_places);

        firebaseFirestore = FirebaseFirestore.getInstance();
        placesList = findViewById(R.id.listPlaces);

        String msg = getIntent().getStringExtra("Category");
        Query query = firebaseFirestore.collection("Places").whereEqualTo("Category",""+msg);

        FirestoreRecyclerOptions<PlacesModel> option = new FirestoreRecyclerOptions.Builder<PlacesModel>()
                .setQuery(query, PlacesModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PlacesModel, PlacesViewHolder>(option) {
            @NonNull
            @Override
            public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_places, parent, false);
                return new PlacesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PlacesViewHolder holder, int position, @NonNull PlacesModel model) {
                holder.place_name.setText(model.getName());
                holder.place_add.setText(model.getAddress());
                holder.place_open.setText(model.getOpen()+"");
                holder.place_close.setText(model.getClose()+"");
            }
        };
        placesList.setHasFixedSize(true);
        placesList.setLayoutManager(new LinearLayoutManager(this));
        placesList.setAdapter(adapter);
    }

    private class PlacesViewHolder extends RecyclerView.ViewHolder {

        private TextView place_add;
        private TextView place_name;
        private TextView place_open;
        private TextView place_close;

        public PlacesViewHolder(@NonNull View itemView)
        {
            super(itemView);
            place_add = itemView.findViewById(R.id.place_address);
            place_name = itemView.findViewById(R.id.place_name);
            place_open = itemView.findViewById(R.id.open_time);
            place_close = itemView.findViewById(R.id.close_time);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}
