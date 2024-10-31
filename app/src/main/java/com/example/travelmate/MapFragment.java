package com.example.travelmate;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private RecyclerView recyclerView;
    private MapsAdapter adapter;
    private List<MapsAdapter.MapData> mapDataList;
    FirebaseFirestore db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view = inflater.inflate(R.layout.fragment_map, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_view);
        mapDataList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MapsAdapter(mapDataList, getContext());

        recyclerView.setAdapter(adapter);

        fetchMapsFromFirestore();

        Button openMapButton = view.findViewById(R.id.googleMap); // Assuming the button ID is button4
        openMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMaps();
            }
        });

        return view;
    }

    private void fetchMapsFromFirestore() {
        db.collection("Maps").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MapsAdapter.MapData mapData = document.toObject(MapsAdapter.MapData.class);
                    mapDataList.add(mapData);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void openGoogleMaps() {
        // Create a Uri to specify location (optional) or open the map directly
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="); // Opens Google Maps without a specific location
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Ensures it opens in Google Maps

        // Check if Google Maps is available on the device
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Handle the case where Google Maps is not installed
            // Optionally, you could guide the user to install Google Maps from the Play Store
        }
    }



}