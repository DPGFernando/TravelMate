package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    private GridView eventsGridView;
    private FirebaseFirestore db;
    private List<Event> eventList;
    private EventAdapter eventAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        db = FirebaseFirestore.getInstance();

        eventsGridView = view.findViewById(R.id.eventsGridView);
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList);
        eventsGridView.setAdapter(eventAdapter);

        fetchEventsFromFirestore();

        eventsGridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event selectedEvent = eventList.get(position);
            String documentId = selectedEvent.getDocumentId();

            // Log to check if documentId is being set
            Log.d("EventFragment", "Selected Event Document ID: " + documentId);

            if (documentId != null) {
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.putExtra("documentID", documentId);
                startActivity(intent);
            } else {
                Log.e("EventFragment", "Document ID is null for the selected event.");
                Toast.makeText(getContext(), "Event ID not found.", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void fetchEventsFromFirestore() {
        db.collectionGroup("Events")
                .get().addOnSuccessListener(task -> {
            if (!task.isEmpty()) {
                for (QueryDocumentSnapshot document : task) {
                    String documentId = document.getId();
                    String eventName = document.getString("eventName");
                    String date = document.getString("date");
                    String startsAt = document.getString("startsAt");
                    String endsAt = document.getString("endsAt");
                    String entranceFee = document.getString("entranceFee");
                    String photoimg = document.getString("photoimg");
                    String venue = document.getString("venue");
                    String contact = document.getString("contact");

                    Event event = new Event(documentId, photoimg, eventName, date, startsAt, endsAt, entranceFee, venue, contact);
                    eventList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
        });
    }
}
