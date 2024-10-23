package com.example.travelmate;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class editEventManager extends AppCompatActivity {

    ImageView app_logo,profilePhoto,nicFront,nicBack;
    TextView signUp,uploadImageText,uploadNic,front,back;
    EditText firstName,lastName,userEmail,userPassword,retypePassword,mobileNo,nicNo;
    Button createBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    StorageReference fileRef, fileRef1, fileRef2;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event_manager);

        app_logo = findViewById(R.id.imageView);
        signUp = findViewById(R.id.signUp);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        retypePassword = findViewById(R.id.retypePassword);
        mobileNo = findViewById(R.id.mobileNo);
        nicNo = findViewById(R.id.nicNo);
        createBtn = findViewById(R.id.createBtn);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = fAuth.getCurrentUser();
        userID = fAuth.getCurrentUser().getUid();

        loadUserData();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = firstName.getText().toString();
                String lName = lastName.getText().toString();
                String uEmail = userEmail.getText().toString().trim();
                String uPass = userPassword.getText().toString().trim();
                String uRetypePass = retypePassword.getText().toString();
                String uPhone = mobileNo.getText().toString();
                String uNicNo = nicNo.getText().toString();

                if(fName.isEmpty()){
                    firstName.setError("FirstName is required");
                    return;
                }

                if(lName.isEmpty()){
                    lastName.setError("LastName is required");
                    return;
                }

                if(uEmail.isEmpty()){
                    userEmail.setError("Email is required");
                    return;
                }

                if(uPass.isEmpty()){
                    userPassword.setError("Password is required");
                    return;
                }

                if(uPass.length()<8){
                    userPassword.setError("At least 8 characters");
                    return;
                }

                if(!uRetypePass.equals(uPass)){
                    retypePassword.setError("Password doesn't match");
                    return;
                }

                if(uPhone.isEmpty()){
                    mobileNo.setError("Mobile is required");
                    return;
                }

                if(uPhone.length() != 10){
                    mobileNo.setError("Enter Valid Telephone number");
                }

                if(uNicNo.isEmpty()) {
                    nicNo.setError("NIC number is required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                user.updateEmail(userEmail.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(editEventManager.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                                });

                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(editEventManager.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(editEventManager.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                user.updatePassword(userPassword.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(editEventManager.this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(editEventManager.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                DocumentReference documentReference = fStore.collection("EventManager").document(userID);
                documentReference.update("email", uEmail,
                        "firstName", fName,
                        "lastName", lName,
                        "password", uPass,
                        "phoneNumber", uPhone,
                        "NIC No", uNicNo);

                Toast.makeText(editEventManager.this, "Data Entered", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(getApplicationContext(), EventManagerProfile.class));

            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void loadUserData() {
        DocumentReference documentReference = fStore.collection("EventManager").document(userID);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {

            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String fname = value.getString("firstName");
                String lname = value.getString("lastName");
                String uemail = value.getString("email");
                String upassword = value.getString("password");
                String contact = value.getString("phoneNumber");
                String nic = value.getString("NIC No");

                firstName.setText(fname);
                lastName.setText(lname);
                userEmail.setText(uemail);
                userPassword.setText(upassword);
                retypePassword.setText(upassword);
                mobileNo.setText(contact);
                nicNo.setText(nic);

            }

        });

    }
}