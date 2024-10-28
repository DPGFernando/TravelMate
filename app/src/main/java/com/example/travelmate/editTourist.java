package com.example.travelmate;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class editTourist extends AppCompatActivity {

    private EditText firstName, lastName, email, password, retypePassword, mobileNo, nicNo;
    private Button saveButton;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_LICENSE_IMAGE_REQUEST = 2;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private Uri profileImageUri, licenseImageUri;
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
        setContentView(R.layout.activity_edit_tourist);

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
        saveButton = findViewById(R.id.esaveButton);
        progressBar = findViewById(R.id.eprogressBar);

        loadUserData();

        // Set OnClickListener for create account button
        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImagesAndUpdateUser();
                startActivity(new Intent(getApplicationContext(), touristProfile.class));
            } else {
                Toast.makeText(editTourist.this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        DocumentReference documentReference = mstore.collection("tourist").document(userID);

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


            }

        });

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

        return true;
    }

    private void uploadImagesAndUpdateUser() {
        user.updateEmail(email.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore();

                        user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                            Toast.makeText(editTourist.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                        });

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(editTourist.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(editTourist.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
        user.updatePassword(password.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(editTourist.this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(editTourist.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void saveUserToFirestore() {
        DocumentReference documentReference = mstore.collection("tourist").document(userID);
        documentReference.update("email", email.getText().toString(),
                "password", password.getText().toString(),
                "firstName", firstName.getText().toString(),
                "lastName", lastName.getText().toString(),
                "mobileNo", mobileNo.getText().toString(),
                "nicNo", nicNo.getText().toString());

        Toast.makeText(editTourist.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(editTourist.this, touristProfile.class);
        startActivity(intent);
        finish();
    }
}