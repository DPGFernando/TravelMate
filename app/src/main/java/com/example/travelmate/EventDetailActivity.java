package com.example.travelmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

public class EventDetailActivity extends AppCompatActivity {

    private TextView eventName, eventDate, eventVenue, eventStartsAt, eventEndsAt, eventEntranceFee, eventContact;
    private ImageView eventImage;
    private FirebaseFirestore db;
    private String documentID,userId;
    String managerId;
    private Button eventBookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventlist_details);

        // Initialize UI components
        eventName = findViewById(R.id.eventName);
        eventDate = findViewById(R.id.eventDate);
        eventVenue = findViewById(R.id.eventVenue);
        eventStartsAt = findViewById(R.id.eventStartsAt);
        eventEndsAt = findViewById(R.id.eventEndsAt);
        eventEntranceFee = findViewById(R.id.eventPrice);
        eventContact = findViewById(R.id.eventContact);
        eventImage = findViewById(R.id.eventImage);
        eventBookButton = findViewById(R.id.eventBookButton);

        userId = getIntent().getStringExtra("touristId");

        db = FirebaseFirestore.getInstance();

        // Retrieve the document ID passed from the previous activity
        documentID = getIntent().getStringExtra("documentID");

        if (documentID != null) {
            fetchEventData(documentID);
        } else {
            Log.e("EventDetailActivity", "Document ID is null");
            Toast.makeText(this, "Event data not available.", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if no document ID is provided
        }

        eventBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetailActivity.this, eventPaymentGateway.class);
                intent.putExtra("managerId", managerId);
                intent.putExtra("touristId", userId);
                intent.putExtra("eventId", documentID);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void fetchEventData(String documentID) {
        db.collectionGroup("Events")
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.isEmpty()) {
                        boolean found = false;
                        for (QueryDocumentSnapshot document : task) {
                            if (document.getId().equals(documentID)) {
                                found = true;
                                // Retrieve event details
                                String name = document.getString("eventName");
                                String date = document.getString("date");
                                String startsAt = document.getString("startsAt");
                                String endsAt = document.getString("endsAt");
                                String entranceFee = document.getString("entranceFee");
                                String contact = document.getString("contact");
                                String venue = document.getString("venue");
                                String photoimg = document.getString("photoimg");
                                managerId = document.getString("managerId");

                                // Set event details to UI components
                                eventName.setText(name);
                                eventDate.setText("Date: " + date);
                                eventVenue.setText("Venue: " + venue);
                                eventStartsAt.setText("Starts at: " + startsAt);
                                eventEndsAt.setText("Ends at: " + endsAt);
                                eventEntranceFee.setText("Entrance Fee: $ " + entranceFee);
                                eventContact.setText("Contact: " + contact);

                                // Load image with Picasso
                                if (photoimg != null && !photoimg.isEmpty()) {
                                    Picasso.get().load(photoimg).into(eventImage);
                                } else {
                                    Log.e("EventDetailActivity", "Image URL is null or empty");
                                }
                                break;
                            }
                        }
                        if (!found) {
                            Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EventDetailActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailActivity", "Error loading data: " + e.getMessage());
                    Toast.makeText(EventDetailActivity.this, "Failed to load event data", Toast.LENGTH_SHORT).show();
                });
    }


}
