package com.example.travelmate;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class TGSignUpActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, password, retypePassword, mobileNo, nicNo;
    private Button createAccountButton;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_LICENSE_IMAGE_REQUEST = 2;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private Uri profileImageUri, licenseImageUri;
    private ImageView uploadProfilePhoto, uploadLicensePhoto;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mstore;
    private StorageReference mStorageRef;
    private String userID;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_tourist_guide);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mstore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        retypePassword = findViewById(R.id.retypePassword);
        mobileNo = findViewById(R.id.mobileNo);
        nicNo = findViewById(R.id.nicNo);
        uploadProfilePhoto = findViewById(R.id.uploadProfilePhoto);
        uploadLicensePhoto = findViewById(R.id.uploadLicensePhoto);
        createAccountButton = findViewById(R.id.createAccountButton);
        progressBar = findViewById(R.id.progressBar);




        // Set OnClickListener for uploading profile photo
        uploadProfilePhoto.setOnClickListener(v -> requestStoragePermission(PICK_PROFILE_IMAGE_REQUEST));

        // Set OnClickListener for uploading license photo
        uploadLicensePhoto.setOnClickListener(v -> requestStoragePermission(PICK_LICENSE_IMAGE_REQUEST));

        // Set OnClickListener for create account button
        createAccountButton.setOnClickListener(v -> {
            if (validateFields()) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImagesAndCreateUser();
                startActivity(new Intent(getApplicationContext(), loggin.class));
            } else {
                Toast.makeText(TGSignUpActivity.this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // For Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted; request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // Permission already granted, proceed with file chooser
                openFileChooser(requestCode);
            }
        } else { // For Android versions below 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted; request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // Permission already granted, proceed with file chooser
                openFileChooser(requestCode);
            }
        }
    }

    private void openFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {
                profileImageUri = data.getData();
                uploadProfilePhoto.setImageURI(profileImageUri);  // Set image URI for profile picture
            } else if (requestCode == PICK_LICENSE_IMAGE_REQUEST) {
                licenseImageUri = data.getData();
                uploadLicensePhoto.setImageURI(licenseImageUri);  // Set image URI for license picture
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser(requestCode);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateFields() {
        if (firstName.getText().toString().trim().isEmpty()) {
            firstName.setError("First name is required");
            return false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastName.setError("Last name is required");
            return false;
        }
        if (email.getText().toString().trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Valid email is required");
            return false;
        }
        if (password.getText().toString().trim().isEmpty() || password.getText().toString().length() < 8) {
            password.setError("Password must be at least 8 characters");
            return false;
        }
        if (!password.getText().toString().equals(retypePassword.getText().toString().trim())) {
            retypePassword.setError("Passwords do not match");
            return false;
        }
        if (mobileNo.getText().toString().trim().isEmpty() || mobileNo.getText().toString().length() != 10) {
            mobileNo.setError("Valid mobile number is required");
            return false;
        }

        if (nicNo.getText().toString().trim().isEmpty() || !(nicNo.getText().toString().trim().length() == 10 || nicNo.getText().toString().trim().length() == 12) || (nicNo.getText().toString().trim().length() == 10 && !nicNo.getText().toString().trim().matches(".*[VXvx]$"))) {
            if (nicNo.getText().toString().trim().isEmpty()) {
                nicNo.setError("NIC number is required");
            } else if (!(nicNo.getText().toString().trim().length() == 10 || nicNo.getText().toString().trim().length() == 12)) {
                nicNo.setError("NIC number does not contain valid character count");
            } else if (nicNo.getText().toString().trim().length() == 10 && !nicNo.getText().toString().trim().matches(".*[VXvx]$")) {
                nicNo.setError("Last character should changed with 'V' or 'X'");
            } else if (nicNo.getText().toString().trim().length() == 9 ) {
                nicNo.setError("Your NIC number should end with 'V' or 'X'");
            }
            return false;
        }
        if (profileImageUri == null) {
            Toast.makeText(this, "Please upload a profile photo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (licenseImageUri == null) {
            Toast.makeText(this, "Please upload a license image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadImagesAndCreateUser() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userID = mAuth.getCurrentUser().getUid();
                        uploadProfileImage();

                        // Send verification link
                        FirebaseUser userf = mAuth.getCurrentUser();
                        userf.sendEmailVerification().addOnSuccessListener(aVoid -> {
                            Toast.makeText(TGSignUpActivity.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                        });

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(TGSignUpActivity.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TGSignUpActivity.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void uploadProfileImage() {
        final StorageReference profileImageRef = mStorageRef.child("TouristGuide/" + userID  + "/profilePicture.jpg");
        profileImageRef.putFile(profileImageUri).addOnSuccessListener(taskSnapshot -> {
            profileImageRef.getDownloadUrl().addOnSuccessListener(profileImageUrl -> {
                uploadLicenseImage(profileImageUrl.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(TGSignUpActivity.this, "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void uploadLicenseImage(String profileImageUrl) {
        final StorageReference licenseImageRef = mStorageRef.child("TouristGuide/" + userID + "/licenseImage.jpg");
        licenseImageRef.putFile(licenseImageUri).addOnSuccessListener(taskSnapshot -> {
            licenseImageRef.getDownloadUrl().addOnSuccessListener(licenseImageUrl -> {
                saveUserToFirestore(profileImageUrl, licenseImageUrl.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(TGSignUpActivity.this, "Failed to upload license image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void saveUserToFirestore(String profileImageUrl, String licenseImageUrl) {
        DocumentReference documentReference = mstore.collection("TouristGuide").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName.getText().toString());
        user.put("lastName", lastName.getText().toString());
        user.put("email", email.getText().toString());
        user.put("mobileNo", mobileNo.getText().toString());
        user.put("nicNo", nicNo.getText().toString());
        user.put("password", password.getText().toString());
        user.put("profileImageUrl", profileImageUrl);
        user.put("licenseImageUrl", licenseImageUrl);

        documentReference.set(user).addOnSuccessListener(aVoid -> {
            Toast.makeText(TGSignUpActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(TGSignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }
}
