package com.example.travelmate;


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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class signUp_tourist extends AppCompatActivity {
    EditText First_Name, Last_Name, Email, Password, Re_Password, Mobile_Num, Passport_ID;
    Button SingUpB;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    ImageView profileImage;
    ProgressBar progressBar;
    Uri imageUri;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_tourist);

        First_Name = findViewById(R.id.First_Name);
        Last_Name = findViewById(R.id.Last_Name);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Re_Password = findViewById(R.id.Re_Password);
        Mobile_Num = findViewById(R.id.Mobile_Num);
        Passport_ID = findViewById(R.id.Passport_ID);
        SingUpB = findViewById(R.id.signUpBtn);
        profileImage = findViewById(R.id.profile);
        progressBar = findViewById(R.id.progressBar1);
        storageReference = FirebaseStorage.getInstance().getReference();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        /*if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Interface.class));
            finish();
        }*/

        SingUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = First_Name.getText().toString();
                String lName = Last_Name.getText().toString();
                String uEmail = Email.getText().toString().trim();
                String uPass = Password.getText().toString().trim();
                String uRetypePass = Re_Password.getText().toString();
                String uPhone = Mobile_Num.getText().toString();
                String passID = Passport_ID.getText().toString();

                if(fName.isEmpty()){
                    First_Name.setError("FirstName is required");
                    return;
                }

                if(lName.isEmpty()){
                    Last_Name.setError("LastName is required");
                    return;
                }

                if(uEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()){
                    Email.setError("Email is required");
                    return;
                }

                if(uPass.isEmpty()){
                    Password.setError("Password is required");
                    return;
                }

                if(uPass.length()<8){
                    Password.setError("At least 8 characters");
                    return;
                }

                if(!uRetypePass.equals(uPass)){
                    Re_Password.setError("Password doesn't match");
                    return;
                }

                if(uPhone.isEmpty()){
                    Mobile_Num.setError("Mobile is required");
                    return;
                }

                if(uPhone.length() != 10){
                    Mobile_Num.setError("Enter Valid Telephone number");
                }

                if(passID.isEmpty()) {
                    Passport_ID.setError("NIC number is required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                Task<AuthResult> authResultTask = fAuth.createUserWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser fUser = fAuth.getCurrentUser();

                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(signUp_tourist.this, "Verification Email was Sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.v("TAG", "Some error is occurred"+ e.toString());
                                }
                            });

                            userID = fAuth.getCurrentUser().getUid();
                            uploadImages(imageUri, userID);

                            DocumentReference documentReference = fStore.collection("Tourist").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName", fName);
                            user.put("lName", lName);
                            user.put("Email", uEmail);
                            user.put("mNum", uPhone);
                            user.put("Passport", passID);
                            user.put("password", uPass);

                            Toast.makeText(signUp_tourist.this, "Data Entered", Toast.LENGTH_SHORT).show();

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.v("TAG", "onSuccess: User profile is created for" + userID);
                                    Intent intent = new Intent(signUp_tourist.this, loggin.class);
                                    startActivity(intent);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.v("TAG", "Failed" + e.toString());
                                }
                            });


                        }else{
                            Toast.makeText(signUp_tourist.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1010);

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1010) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
            }
        }
    }

    private void uploadImages(Uri profileimg, String userID) {

        StorageReference profileRef = storageReference.child("Tourist/" + userID + "/profile.jpg");
        profileRef.putFile(profileimg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("TAG", "Profile Image uploaded successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(signUp_tourist.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}