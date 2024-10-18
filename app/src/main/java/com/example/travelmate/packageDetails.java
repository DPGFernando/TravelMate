package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class packageDetails extends AppCompatActivity {

    TextView packageTitle, packageDes, packagePrice, packageDays;
    ImageView packageImage;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID, documentID;
    Button editPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_package_details);

        packageTitle = findViewById(R.id.packageTitle);
        packageDes = findViewById(R.id.packageDes);
        packagePrice = findViewById(R.id.packagePrice);
        packageDays = findViewById(R.id.packageDays);
        packageImage = findViewById(R.id.packageImage);
        editPackage = findViewById(R.id.editPackage);
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentID = getIntent().getStringExtra("documentID");

        DocumentReference documentReference = fStore.collection("TouristGuide").document(userID);
        StorageReference fileRef = storageReference.child("Packages/"+ userID +"/" + documentID + ".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(packageImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getMessage());
            }
        });

        DocumentReference documentReference1 = documentReference.collection("Packages").document(documentID);
        documentReference1.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                packageTitle.setText(value.getString("packName"));
                packageDes.setText(value.getString("description"));
                packagePrice.setText(value.getString("price"));
                packageDays.setText(value.getString("nodays") + " days");
            }
        });

        editPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(packageDetails.this, editpackageDetails.class);
                intent.putExtra("packageID", documentID);
                intent.putExtra("touristGuideID", userID);
                intent.putExtra("packageTitle", packageTitle.getText().toString());
                intent.putExtra("packageDes", packageDes.getText().toString());
                intent.putExtra("packagePrice", packagePrice.getText().toString());
                intent.putExtra("packageDays", packageDays.getText().toString());
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}