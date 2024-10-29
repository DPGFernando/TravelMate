package com.example.travelmate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Guides> guideList;
    private StorageReference storageReference,fileRef;
    private List<EventManager> eventsList;
    private GuidesAdapter adapter1;
    private ImageView tProfile;
    private ManagerAdapter adapter2;
    private GridView guideGridView, eventGridView;
    private String userID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        guideList = new ArrayList<>();
        eventsList = new ArrayList<>();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("userID", userID);
        fetchtouristGuideFromFirestore();
        fetcheventManagerFromFireStore();

        guideGridView = view.findViewById(R.id.guideGridView);
        eventGridView = view.findViewById(R.id.eventGridView);
        tProfile = view.findViewById(R.id.tProfile);

        storageReference = FirebaseStorage.getInstance().getReference();

        fileRef = storageReference.child("Tourist/" + userID + "/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity())
                        .load(uri)
                        .circleCrop()
                        .into(tProfile);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tProfile.setImageResource(R.drawable.img_3);
            }
        });

        tProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), touristProfile.class);
                intent.putExtra("touristId", userID);
                startActivity(intent);
            }
        });

        adapter1 = new GuidesAdapter(getContext(), guideList);
        adapter2 = new ManagerAdapter(getContext(), eventsList);

        guideGridView.setAdapter(adapter1);
        eventGridView.setAdapter(adapter2);


        return view;
    }

    private void fetchtouristGuideFromFirestore() {

        CollectionReference guideRef = db.collection("TouristGuide");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        guideRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document1 : task.getResult()) {
                    Guides touristGuide = document1.toObject(Guides.class);
                    touristGuide.setDocumentId(document1.getId());
                    guideList.add(touristGuide);
                }
                adapter1.notifyDataSetChanged();

            }
        });

    }

    private void fetcheventManagerFromFireStore() {

        CollectionReference eventRef = db.collection("EventManager");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    EventManager eventManager = document.toObject(EventManager.class);
                    eventManager.setDocumentId(document.getId());
                    Log.d("eventManagerId", eventManager.getDocumentId());
                    eventsList.add(eventManager);
                }
                adapter2.notifyDataSetChanged();

            }
        });
    }


    public static class Guides {
        private String firstName;
        private String lastName;
        private String profileImageUrl;
        private String documentId;


        public Guides() { }

        public Guides(String firstName, String lastName, String profileImageUrl, String documentId) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.profileImageUrl = profileImageUrl;
            this.documentId = documentId;

        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

    }

    public static class EventManager {
        private String firstName;
        private String profileImageUrl;
        private String documentId;


        public EventManager() { }

        public EventManager(String firstName,  String profileImageUrl, String documentId) {
            this.firstName = firstName;
            this.profileImageUrl = profileImageUrl;
            this.documentId = documentId;

        }

        public String getFirstName() { return firstName; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

    }

    public static class GuidesAdapter extends BaseAdapter {
        private Context context;
        private List<Guides> guides; // Replace Guide with your data model class

        public GuidesAdapter(Context context, List<Guides> guides) {
            this.context = context;
            this.guides = guides;
        }

        @Override
        public int getCount() {
            return guides.size();
        }

        @Override
        public Object getItem(int position) {
            return guides.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Guides guide = guides.get(position); // Replace Guide with your data model class
            holder.nameTextView.setText(guide.getFirstName());

            Glide.with(context)
                    .load(guide.getProfileImageUrl()) // Replace with your image URL or resource
                    .circleCrop() // Applies circular cropping
                    .into(holder.profileImageView);

            return convertView;
        }

        private static class ViewHolder {
            TextView nameTextView;
            ImageView profileImageView;

            ViewHolder(View view) {
                nameTextView = view.findViewById(R.id.gridCaption);
                profileImageView = view.findViewById(R.id.gridImage);
            }
        }


    }

    public static class ManagerAdapter extends BaseAdapter {
        private Context context;
        private List<EventManager> events; // Replace Guide with your data model class

        public ManagerAdapter(Context context, List<EventManager> events) {
            this.context = context;
            this.events = events;
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public Object getItem(int position) {
            return events.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            EventManager eventManager = events.get(position); // Replace Guide with your data model class
            holder.nameTextView.setText(eventManager.getFirstName());


            Picasso.get().load(eventManager.getProfileImageUrl()).transform(new CircleTransform()).into(holder.profileImageView);

            return convertView;
        }

        private static class ViewHolder {
            TextView nameTextView;
            ImageView profileImageView;

            ViewHolder(View view) {
                nameTextView = view.findViewById(R.id.gridCaption);
                profileImageView = view.findViewById(R.id.gridImage);
            }
        }
    }



}

