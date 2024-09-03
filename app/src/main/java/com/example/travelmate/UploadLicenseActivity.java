package com.example.travelmate;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UploadLicenseActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;

    private Button uploadLicensePhoto, createAccountButton;
    private Uri imageUri;
    private Uri licenseImageUri, profileImageUri;
    private ImageView licenseImageView;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private ProgressBar progressBar;
    private FirebaseFirestore mstore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_license);

        // Initialize UI elements
        uploadLicensePhoto = findViewById(R.id.uploadLicensePhoto);
        createAccountButton = findViewById(R.id.createAccountButton);
        licenseImageView = findViewById(R.id.licenseImageView);
        progressBar = findViewById(R.id.progressBar);

        // Retrieve user details from Intent
        Intent intent = getIntent();
        String firstNameText = intent.getStringExtra("firstName");
        String lastNameText = intent.getStringExtra("lastName");
        String emailText = intent.getStringExtra("email");
        String passwordText = intent.getStringExtra("password");
        String mobileNoText = intent.getStringExtra("mobileNo");
        String nicNoText = intent.getStringExtra("nicNo");
        profileImageUri = Uri.parse(intent.getStringExtra("profileImageUri"));

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mstore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        uploadLicensePhoto.setOnClickListener(v -> requestStoragePermission());

        createAccountButton.setOnClickListener(v -> {
            if (validateFields()) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImagesAndCreateUser(emailText, passwordText, firstNameText, lastNameText, mobileNoText, nicNoText);
            } else {
                Toast.makeText(UploadLicenseActivity.this, "Please upload a valid license", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImagesAndCreateUser(String email, String password, String firstName, String lastName, String mobileNo, String nicNo) {
        // Upload profile picture
        userID = mAuth.getCurrentUser().getUid();
        final StorageReference profileImageRef = mStorageRef.child(userID).child("profile_pictures/" + System.currentTimeMillis() + ".jpg");
        profileImageRef.putFile(profileImageUri).addOnSuccessListener(taskSnapshot -> {
            profileImageRef.getDownloadUrl().addOnSuccessListener(profileImageUrl -> {
                // Upload license image
                final StorageReference licenseImageRef = mStorageRef.child(userID).child("license_images/" + System.currentTimeMillis() + ".jpg");
                licenseImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot1 -> {
                    licenseImageRef.getDownloadUrl().addOnSuccessListener(licenseImageUrl -> {
                        // Both images are uploaded, now create the user in Firestore
                        createUserAccount(email, password, firstName, lastName, mobileNo, nicNo, profileImageUrl.toString(), licenseImageUrl.toString());
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(UploadLicenseActivity.this, "Failed to upload license image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UploadLicenseActivity.this, "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void createUserAccount(String email, String password, String firstName, String lastName, String mobileNo, String nicNo, String profileImageUrl, String licenseImageUrl) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // sent verification link

                        FirebaseUser userf = mAuth.getCurrentUser();
                        userf.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(UploadLicenseActivity.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                            }
                        });

                        Toast.makeText(UploadLicenseActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                        //userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = mstore.collection("TouristGuide").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("firstName", firstName);
                        user.put("lastName", lastName);
                        user.put("email", email);
                        user.put("mobileNo", mobileNo);
                        user.put("nicNo", nicNo);
                        user.put("profileImageUrl", profileImageUrl);
                        user.put("licenseImageUrl", licenseImageUrl);
                        documentReference.set(user).addOnSuccessListener((OnSuccessListener) (aVoid) -> {
                            Log.d(TAG, "User profile is created for " + userID);
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(UploadLicenseActivity.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UploadLicenseActivity.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openFileChooser();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            licenseImageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateFields() {
        if (imageUri == null) {
            Toast.makeText(this, "Please upload a license image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
