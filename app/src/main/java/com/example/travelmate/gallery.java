package com.example.travelmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class gallery extends AppCompatActivity {

    String documentId;
    private TextView locationTextView;
    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private List<String> galleryList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);

        documentId = getIntent().getStringExtra("documentId");
        locationTextView = findViewById(R.id.location);
        locationTextView.setText(documentId);

        recyclerView = findViewById(R.id.recycler_view); // Replace with your RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        loadGalleryData();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadGalleryData() {

        DocumentReference docRef = db.collection("Maps").document(documentId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    GalleryAdapter.Gallery gallery = document.toObject(GalleryAdapter.Gallery.class);
                    if (gallery != null && gallery.getImageUrl() != null) {
                        galleryList = gallery.getImageUrl(); // Get all image URLs
                        galleryAdapter = new GalleryAdapter(this, galleryList); // Pass all image URLs
                        recyclerView.setAdapter(galleryAdapter);
                    }
                }
            } else {
                // Handle the error
            }
        });
    }
}