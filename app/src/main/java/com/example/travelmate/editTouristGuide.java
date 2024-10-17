package com.example.travelmate;

import static android.content.ContentValues.TAG;

import android.Manifest;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class editTouristGuide extends AppCompatActivity {

    private EditText firstName, lastName, email, password, retypePassword, mobileNo, nicNo;
    private Button saveButton;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_LICENSE_IMAGE_REQUEST = 2;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private Uri profileImageUri, licenseImageUri;
    private ImageView uploadProfilePhoto, uploadLicensePhoto;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore mstore;
    private StorageReference mStorageRef;
    private StorageReference fileRef, fileRef1;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_tourist_guide);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        userID = user.getUid();

        // Initialize UI elements
        firstName = findViewById(R.id.efirstName);
        lastName = findViewById(R.id.elastName);
        email = findViewById(R.id.eemail);
        password = findViewById(R.id.epassword);
        retypePassword = findViewById(R.id.eretypePassword);
        mobileNo = findViewById(R.id.emobileNo);
        nicNo = findViewById(R.id.enicNo);
        uploadProfilePhoto = findViewById(R.id.euploadProfilePhoto);
        uploadLicensePhoto = findViewById(R.id.euploadLicensePhoto);
        saveButton = findViewById(R.id.esaveButton);
        progressBar = findViewById(R.id.eprogressBar);

        uploadProfilePhoto.setOnClickListener(v -> requestStoragePermission(PICK_PROFILE_IMAGE_REQUEST));

        // Set OnClickListener for uploading license photo
        uploadLicensePhoto.setOnClickListener(v -> requestStoragePermission(PICK_LICENSE_IMAGE_REQUEST));

        loadUserData();

        // Set OnClickListener for create account button
        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImagesAndUpdateUser();
                startActivity(new Intent(getApplicationContext(), TouristGuideProfile.class));
            } else {
                Toast.makeText(editTouristGuide.this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        DocumentReference documentReference = mstore.collection("TouristGuide").document(userID);
        fileRef = mStorageRef.child("TouristGuide/" + userID +"/profilePicture.jpg");
        fileRef1 = mStorageRef.child("TouristGuide/" + userID +"/licenseImage.jpg");

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {

            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String fname = value.getString("firstName");
                String lname = value.getString("lastName");
                String uemail = value.getString("email");
                String upassword = value.getString("password");
                String contact = value.getString("mobileNo");
                String nic = value.getString("nicNo");

                firstName.setText(fname);
                lastName.setText(lname);
                email.setText(uemail);
                password.setText(upassword);
                retypePassword.setText(upassword);
                mobileNo.setText(contact);
                nicNo.setText(nic);

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(uploadProfilePhoto);
                    }
                });

                fileRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(uploadLicensePhoto);
                    }
                });

            }

        });

    }

    private void requestStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // For Android 13+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted; request it
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // Permission already granted, proceed with file chooser
                openFileChooser(requestCode);
            }
        } else { // For Android versions below 13
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        if (nicNo.getText().toString().trim().isEmpty()) {
            nicNo.setError("NIC number is required");
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

    private void uploadImagesAndUpdateUser() {
        user.updateEmail(email.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        uploadProfileImage();

                        user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                            Toast.makeText(editTouristGuide.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                        });

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(editTouristGuide.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(editTouristGuide.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
        user.updatePassword(password.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(editTouristGuide.this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(editTouristGuide.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(editTouristGuide.this, "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(editTouristGuide.this, "Failed to upload license image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void saveUserToFirestore(String profileImageUrl, String licenseImageUrl) {
        DocumentReference documentReference = mstore.collection("TouristGuide").document(userID);
        documentReference.update("email", email.getText().toString(),
                "password", password.getText().toString(),
                "firstName", firstName.getText().toString(),
                "lastName", lastName.getText().toString(),
                "mobileNo", mobileNo.getText().toString(),
                "nicNo", nicNo.getText().toString(),
                "profileImageUrl", profileImageUrl,
                "licenseImageUrl", licenseImageUrl);

        Toast.makeText(editTouristGuide.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(editTouristGuide.this, TouristGuideProfile.class);
        startActivity(intent);
        finish();
    }
}