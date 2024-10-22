package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

public class EventManagerProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvContact, username, uemail;
    private EditText etName, etEmail, etPassword, etContact;
    private ImageView image;
    private MaterialButton btnEdit, logOutbtn;


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
        setContentView(R.layout.activity_event_manager_profile);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        username = findViewById(R.id.username);
        uemail = findViewById(R.id.email_usern);
        tvContact = findViewById(R.id.contact_947);
        image = findViewById(R.id.image);

        btnEdit = findViewById(R.id.pen);
        logOutbtn = findViewById(R.id.logOut);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        fileRef = firebaseStorage.child("EventManager/" + userID + "/profilePicture.jpg");
        documentReference = fStore.collection("EventManager").document(userID);

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
                Intent intent = new Intent(EventManagerProfile.this,editTouristGuide.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {

            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String name = value.getString("firstName") + " " + value.getString("lastName");
                String email = value.getString("email");
                String contact = value.getString("mobileNo");


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
                Picasso.get().load(uri).into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                image.setImageResource(R.drawable.img_3);
            }
        });

    }
}