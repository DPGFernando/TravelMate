package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class editeventDetails extends AppCompatActivity {

    EditText eventTitle, date, venue, startsAt, endsAt, entranceFee, contact;
    ImageView eventImage;
    StorageReference storageReference;
    Button saveChanges;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID,documentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editevent_details);

        eventTitle = findViewById(R.id.eventTitle);
        date = findViewById(R.id.date);
        venue = findViewById(R.id.venue);
        startsAt = findViewById(R.id.startsAt);
        endsAt = findViewById(R.id.endsAt);
        entranceFee = findViewById(R.id.entranceFee);
        contact = findViewById(R.id.contact);
        eventImage = findViewById(R.id.eventImage);
        saveChanges = findViewById(R.id.saveChanges);
        userID = getIntent().getStringExtra("eventManagerID");
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();
        documentID = getIntent().getStringExtra("eventID");

        eventTitle.setText(getIntent().getStringExtra("eventName"));
        date.setText(getIntent().getStringExtra("date"));
        venue.setText(getIntent().getStringExtra("venue"));
        startsAt.setText(getIntent().getStringExtra("startsAt"));
        endsAt.setText(getIntent().getStringExtra("endsAt"));
        entranceFee.setText(getIntent().getStringExtra("entranceFee"));
        contact.setText(getIntent().getStringExtra("contact"));

        DocumentReference documentReference = fStore.collection("EventManager").document(userID);
        DocumentReference documentReference1 = documentReference.collection("Events").document(documentID);
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



        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventTitle.getText().toString().isEmpty() || date.getText().toString().isEmpty() || venue.getText().toString().isEmpty() || startsAt.getText().toString().isEmpty() || endsAt.getText().toString().isEmpty() || entranceFee.getText().toString().isEmpty() ||contact.getText().toString().isEmpty()){
                    Toast.makeText(editeventDetails.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    documentReference1.update("eventName", eventTitle.getText().toString(),
                            "date", date.getText().toString(),
                            "venue", venue.getText().toString(),
                            "startsAt", startsAt.getText().toString(),
                            "endsAt", endsAt.getText().toString(),
                            "entranceFee", entranceFee.getText().toString(),
                            "contact", contact.getText().toString());

                    Toast.makeText(editeventDetails.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(editeventDetails.this, eventDetails.class);
                    intent.putExtra("documentID", documentID);
                    startActivity(intent);
                    finish();
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}