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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class signup_eventManager extends AppCompatActivity {

    ImageView app_logo,profilePhoto,nicFront,nicBack;
    TextView signUp,uploadImageText,uploadNic,front,back;
    EditText firstName,lastName,userEmail,userPassword,retypePassword,mobileNo,nicNo;
    Button createBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    String userID;
    Uri profileimg, nicBackImg, nicFrontImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_event_manager);

        app_logo = findViewById(R.id.imageView);
        profilePhoto = findViewById(R.id.profilePhoto);
        nicFront = findViewById(R.id.nicFront);
        nicBack = findViewById(R.id.nicBack);
        signUp = findViewById(R.id.signUp);
        uploadImageText = findViewById(R.id.uploadImageText);
        uploadNic = findViewById(R.id.uploadNic);
        front = findViewById(R.id.front);
        back = findViewById(R.id.back);
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

                if(uEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()){
                    userEmail.setError("Valid Email is required");
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
                    return;
                }

                if (uNicNo.isEmpty() || !(uNicNo.length() == 10 || uNicNo.length() == 12) || (uNicNo.length() == 10 && !uNicNo.matches(".*[VXvx]$"))) {
                    if (uNicNo.isEmpty()) {
                        nicNo.setError("NIC number is required");
                    } else if (!(uNicNo.length() == 10 || uNicNo.length() == 12)) {
                        nicNo.setError("NIC number does not contain valid character count");
                    } else if (uNicNo.length() == 10 && !uNicNo.matches(".*[VXvx]$")) {
                        nicNo.setError("Last character should changed with 'V' or 'X'");
                    } else if (uNicNo.length() == 9 ) {
                        nicNo.setError("Your NIC number should end with 'V' or 'X'");
                    }
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                Task<AuthResult> authResultTask = fAuth.createUserWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser fuser = fAuth.getCurrentUser();

                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(signup_eventManager.this, "Verification Email was Sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.v("TAG", "Some error is occurred"+ e.toString());
                                }
                            });

                            userID = fAuth.getCurrentUser().getUid();
                            uploadProfileImage(userID);

                            Toast.makeText(signup_eventManager.this, "Data Entered", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), loggin.class));

                        }else{
                            Toast.makeText(signup_eventManager.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1000);
            }
        });

        nicFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1001);
            }
        });

        nicBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery, 1002);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                profileimg = data.getData();
                profilePhoto.setImageURI(profileimg);
            }
        } else if (requestCode == 1001) {
            if(resultCode == Activity.RESULT_OK){
                nicFrontImg = data.getData();
                nicFront.setImageURI(nicFrontImg);
            }
        }else if (requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {
                nicBackImg = data.getData();
                nicBack.setImageURI(nicBackImg);
            }
        }
    }



    private void uploadProfileImage(String userID) {
        final StorageReference profileImageRef = storageReference.child("EventManager/" + userID  + "/profilePicture.jpg");
        profileImageRef.putFile(profileimg).addOnSuccessListener(taskSnapshot -> {
            profileImageRef.getDownloadUrl().addOnSuccessListener(profileImageUrl -> {
                uploadNicBackImage(userID, profileImageUrl.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(signup_eventManager.this, "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void uploadNicBackImage(String userID, String profileImageUrl) {
        final StorageReference nicBackRef = storageReference.child("EventManager/" + userID + "/nicBack.jpg");
        nicBackRef.putFile(nicBackImg).addOnSuccessListener(taskSnapshot -> {
            nicBackRef.getDownloadUrl().addOnSuccessListener(nicBackImageUrl -> {
                uploadNicFrontImage(userID, profileImageUrl, nicBackImageUrl.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(signup_eventManager.this, "Failed to upload license image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void uploadNicFrontImage(String userID,String profileImageUrl, String nicBackImageUrl) {
        final StorageReference nicFrontRef = storageReference.child("EventManager/" + userID + "/nicFront.jpg");
        nicFrontRef.putFile(nicBackImg).addOnSuccessListener(taskSnapshot -> {
            nicFrontRef.getDownloadUrl().addOnSuccessListener(nicFrontImageUrl -> {
                uploadProfileData(userID, profileImageUrl, nicBackImageUrl ,nicFrontImageUrl.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(signup_eventManager.this, "Failed to upload license image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }


    private void uploadProfileData(String userID, String profileImageUrl, String nicBackUrl, String nicFrontUrl){

        String fName = firstName.getText().toString();
        String lName = lastName.getText().toString();
        String uEmail = userEmail.getText().toString().trim();
        String uPass = userPassword.getText().toString().trim();
        String uRetypePass = retypePassword.getText().toString();
        String uPhone = mobileNo.getText().toString();
        String uNicNo = nicNo.getText().toString();

        DocumentReference documentReference = fStore.collection("EventManager").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", fName);
        user.put("lastName", lName);
        user.put("email", uEmail);
        user.put("phoneNumber", uPhone);
        user.put("NIC No", uNicNo);
        user.put("password", uPass);
        user.put("profileImageUrl", profileImageUrl);
        user.put("nicBackUrl", nicBackUrl);
        user.put("nicFrontUrl", nicFrontUrl);

        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.v("TAG", "onSuccess: User profile is created for" + userID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("TAG", "Failed" + e.toString());
            }
        });
    }
}