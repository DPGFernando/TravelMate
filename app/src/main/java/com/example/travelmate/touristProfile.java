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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class touristProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvContact, username, uemail;
    private EditText etName, etEmail, etPassword, etContact;
    private ImageView image;
    private MaterialButton btnEdit, logOutbtn, btnProfileEdit;
    private Button saveChanges;


    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    StorageReference firebaseStorage;
    Uri imageUri;
    DocumentReference documentReference;
    StorageReference fileRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_profile);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        username = findViewById(R.id.username);
        uemail = findViewById(R.id.email_usern);
        tvContact = findViewById(R.id.contact_947);
        image = findViewById(R.id.image);
        saveChanges = findViewById(R.id.saveChanges);
        btnEdit = findViewById(R.id.pen);
        btnProfileEdit = findViewById(R.id.pen1);
        logOutbtn = findViewById(R.id.logOut);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        fileRef = firebaseStorage.child("Tourist/" + userID + "/profile.jpg");
        documentReference = fStore.collection("Tourist").document(userID);

        loadUserData();


        logOutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), loggin.class));
                finish();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(touristProfile.this,editTourist.class);
                startActivity(intent);
            }
        });

        btnProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1010);

                saveChanges.setEnabled(true);
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.v("TAG", "Profile Image uploaded successfully");
                        Toast.makeText(touristProfile.this, "Image upload succeessfully", Toast.LENGTH_SHORT).show();
                        saveChanges.setEnabled(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(touristProfile.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1010) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri = data.getData();
                image.setImageURI(imageUri);
            }
        }
    }

    private void loadUserData() {
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {

            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String name = value.getString("fName") + " " + value.getString("lName");
                String email = value.getString("Email");
                String contact = value.getString("mNum");


                tvName.setText(name);
                tvEmail.setText(email);
                username.setText("Username: " + name);
                uemail.setText("Email: " + email);
                tvContact.setText("Contact: " + contact);


            }

        });

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).transform(new CircleTransform()).into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                image.setImageResource(R.drawable.img_3);
            }
        });

    }
}