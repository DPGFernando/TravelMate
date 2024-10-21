package com.example.travelmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class eventDetails extends AppCompatActivity {

    TextView eventTitle, date, venue, startsAt, endsAt, entranceFee, contact;
    ImageView eventImage;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID, documentID;
    Button editPackage;
    String ueventTitle, udate, uvenue, ustartsAt, uendsAt, uentranceFee, ucontact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        eventTitle = findViewById(R.id.eventTitle);
        date = findViewById(R.id.date);
        venue = findViewById(R.id.venue);
        startsAt = findViewById(R.id.startsAt);
        endsAt = findViewById(R.id.endsAt);
        entranceFee = findViewById(R.id.entranceFee);
        contact = findViewById(R.id.contact);
        eventImage = findViewById(R.id.eventImage);
        editPackage = findViewById(R.id.editPackage);
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentID = getIntent().getStringExtra("documentID");

        DocumentReference documentReference = fStore.collection("EventManager").document(userID);
        StorageReference fileRef = storageReference.child("Events/"+ userID +"/" + documentID + ".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(eventImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getMessage());
            }
        });

        DocumentReference documentReference1 = documentReference.collection("Events").document(documentID);
        documentReference1.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                ueventTitle = value.getString("eventName");
                udate = value.getString("date");
                uvenue = value.getString("venue");
                ustartsAt = value.getString("startsAt");
                uendsAt = value.getString("endsAt");
                uentranceFee = value.getString("entranceFee");
                ucontact = value.getString("contact");

                eventTitle.setText("Event Name : " + ueventTitle);
                date.setText("Date : " + udate);
                venue.setText("Venue : " + uvenue);
                startsAt.setText("StartsAt : " + ustartsAt);
                endsAt.setText("Ends At : " + uendsAt);
                entranceFee.setText("Entrance Fee : " + uentranceFee);
                contact.setText("Contact : " + ucontact);
            }
        });

        editPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(eventDetails.this, editeventDetails.class);
                intent.putExtra("eventID", documentID);
                intent.putExtra("eventManagerID", userID);
                intent.putExtra("eventName", ueventTitle);
                intent.putExtra("date", udate);
                intent.putExtra("venue", uvenue);
                intent.putExtra("startsAt", ustartsAt);
                intent.putExtra("endsAt", uendsAt);
                intent.putExtra("entranceFee", uentranceFee);
                intent.putExtra("contact", ucontact);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}