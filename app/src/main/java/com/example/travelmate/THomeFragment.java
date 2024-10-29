package com.example.travelmate;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class THomeFragment extends Fragment {

    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference firebaseStorage;
    StorageReference fileRef;
    String userID;
    GridView gridView;
    MyAdapter adapter;
    ArrayList<DataClass> dataList;
    ImageView tProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_t_home, container, false);

        fAuth = FirebaseAuth.getInstance();
        userID = getArguments().getString("userID");

        firebaseStorage = FirebaseStorage.getInstance().getReference();

        fileRef = firebaseStorage.child("TouristGuide/" + userID +"/profilePicture.jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity())
                        .load(uri)
                        .circleCrop()
                        .into(tProfile);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tProfile.setImageResource(R.drawable.img_3);
            }
        });

        gridView = view.findViewById(R.id.gridView);
        tProfile = view.findViewById(R.id.touristProfile);
        dataList = new ArrayList<>();
        adapter = new MyAdapter(dataList, getContext());
        gridView.setAdapter(adapter);

        loadData();

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getContext(), packageDetails.class);
            Log.d("Document ID", dataList.get(position).getDocumentId());
            intent.putExtra("documentID", dataList.get(position).getDocumentId());
            startActivity(intent);
        });

        tProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TouristGuideProfile.class);
                startActivity(intent);
            }
        });


        return view;

    }

    private void loadData() {

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        DocumentReference touristGuideDocRef = firestore.collection("TouristGuide").document(userID);

        CollectionReference collectionRef = touristGuideDocRef.collection("Packages");

        collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String title = document.getString("packName");
                    String imagePath = document.getString("photoimg");
                    String documentId = document.getId();

                    StorageReference imageRef = storage.getReference().child("Packages/" + userID + "/" + documentId + ".jpg");

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        DataClass data = new DataClass(uri.toString(), title, documentId);
                        dataList.add(data);
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                    );
                }
            } else {
                Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
        );
    }


}