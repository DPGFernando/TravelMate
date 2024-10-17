package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class editpackageDetails extends AppCompatActivity {

    EditText packageTitle, packageDes, packagePrice, packageDays;
    ImageView packageImage;
    StorageReference storageReference;
    Button saveChanges;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editpackage_details);

        packageTitle = findViewById(R.id.packageTitle);
        packageDes = findViewById(R.id.packageDes);
        packagePrice = findViewById(R.id.packagePrice);
        packageDays = findViewById(R.id.packageDays);
        packageImage = findViewById(R.id.packageImage);
        saveChanges = findViewById(R.id.saveChanges);
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        packageTitle.setText(getIntent().getStringExtra("packageTitle"));
        packageDes.setText(getIntent().getStringExtra("packageDes"));
        packagePrice.setText(getIntent().getStringExtra("packagePrice"));
        packageDays.setText(getIntent().getStringExtra("packageDays"));

        DocumentReference documentReference = fStore.collection("TouristGuide").document(getIntent().getStringExtra("touristGuideID"));
        DocumentReference documentReference1 = documentReference.collection("packages").document(getIntent().getStringExtra("packageID"));
        StorageReference fileRef = storageReference.child("Packages/C1N5cGNvLiWSkCqkJ9qyVfVcoOi2/Kabaragala.jpg");

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



        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (packageTitle.getText().toString().isEmpty() || packageDes.getText().toString().isEmpty() || packagePrice.getText().toString().isEmpty() || packageDays.getText().toString().isEmpty()){
                    Toast.makeText(editpackageDetails.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    documentReference1.update("packageName", packageTitle.getText().toString(),
                            "description", packageDes.getText().toString(),
                            "price", packagePrice.getText().toString(),
                            "noOfDays", packageDays.getText().toString());

                    Toast.makeText(editpackageDetails.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(editpackageDetails.this, packageDetails.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}