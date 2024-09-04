package com.example.travelmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TGSignUpActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, password, retypePassword, mobileNo, nicNo;
    private Button uploadProfilePhoto, createAccountButton, nextButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private Uri imageUri;
    private ImageView profileImageView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tg_sign_up);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();


        // Initialize UI elements
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        retypePassword = findViewById(R.id.retypePassword);
        mobileNo = findViewById(R.id.mobileNo);
        nicNo = findViewById(R.id.nicNo);
        uploadProfilePhoto = findViewById(R.id.uploadProfilePhoto);
        profileImageView = findViewById(R.id.profileImageView);
        progressBar = findViewById(R.id.progressBar);
        nextButton = findViewById(R.id.nextButton);

        // Set OnClickListener for uploading profile photo
        uploadProfilePhoto.setOnClickListener(v -> requestStoragePermission());

        // Set OnClickListener for next button
        nextButton.setOnClickListener(v -> {
            if (validateFields()) {
                Intent intent = new Intent(TGSignUpActivity.this, UploadLicenseActivity.class);
                intent.putExtra("firstName", firstName.getText().toString().trim());
                intent.putExtra("lastName", lastName.getText().toString().trim());
                intent.putExtra("email", email.getText().toString().trim());
                intent.putExtra("password", password.getText().toString().trim());
                intent.putExtra("mobileNo", mobileNo.getText().toString().trim());
                intent.putExtra("nicNo", nicNo.getText().toString().trim());
                intent.putExtra("profileImageUri", imageUri.toString()); // Pass profile image URI
                startActivity(intent);
            } else {
                Toast.makeText(TGSignUpActivity.this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show();
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
            profileImageView.setImageURI(imageUri);
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

        if (password.getText().toString().trim().isEmpty() || password.getText().toString().length() < 6) {
            password.setError("Password must be at least 6 characters");
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

        return true;
    }
}