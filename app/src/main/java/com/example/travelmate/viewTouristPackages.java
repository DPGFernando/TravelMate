package com.example.travelmate;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class viewTouristPackages extends AppCompatActivity {

    String touristGuideId,touristId;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference firebaseStorage;
    GridView gridView;
    MyAdapter adapter;
    ArrayList<DataClass> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_tourist_packages);

        touristGuideId = getIntent().getStringExtra("touristGuideId");
        touristId = getIntent().getStringExtra("touristId");
        firebaseStorage = FirebaseStorage.getInstance().getReference();

        gridView = findViewById(R.id.gridView);
        dataList = new ArrayList<>();
        adapter = new MyAdapter(dataList, this);
        gridView.setAdapter(adapter);

        loadData();

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(this, bookTouristPackages.class);
            Log.v("tourist ID", touristId);
            intent.putExtra("packageId", dataList.get(position).getDocumentId());
            intent.putExtra("touristId", touristId);
            intent.putExtra("touristGuideId", touristGuideId);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadData() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        DocumentReference touristGuideDocRef = firestore.collection("TouristGuide").document(touristGuideId);

        CollectionReference collectionRef = touristGuideDocRef.collection("Packages");

        collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String title = document.getString("packName");
                    String imagePath = document.getString("photoimg");
                    String documentId = document.getId();

                    StorageReference imageRef = storage.getReference().child("Packages/" + touristGuideId + "/" + documentId + ".jpg");

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        DataClass data = new DataClass(uri.toString(), title, documentId);
                        dataList.add(data);
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    );
                }
            } else {
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        );

    }




}