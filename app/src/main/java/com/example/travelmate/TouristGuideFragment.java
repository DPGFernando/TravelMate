package com.example.travelmate;

import static android.content.Intent.getIntent;

import static com.google.firebase.database.DatabaseKt.getSnapshots;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class TouristGuideFragment extends Fragment {

    private FirebaseFirestore db;
    private List<TouristGuide> touristGuideList;
    private TouristGuideAdapter adapter;
    private RecyclerView recyclerView;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tourist_guide, container, false);

        db = FirebaseFirestore.getInstance();
        touristGuideList = new ArrayList<>();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("userID", userID);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        // Setup the adapter
        adapter = new TouristGuideAdapter(touristGuideList, documentId -> {
            Intent intent = new Intent(getContext(), touristGuideView.class);
            intent.putExtra("touristGuideId", documentId);
            Log.d("touristGuideId", documentId);
            intent.putExtra("touristId", userID);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Fetch bookings from Firestore for the current tourist guide
        fetchtouristGuideFromFirestore();

        return view;
    }

    private void fetchtouristGuideFromFirestore() {

        CollectionReference touristGuideRef = db.collection("TouristGuide");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        touristGuideRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    TouristGuide touristGuide = document.toObject(TouristGuide.class);
                    touristGuide.setDocumentId(document.getId());
                    touristGuideList.add(touristGuide);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }


    public static class TouristGuide {
        private String firstName;
        private String lastName;
        private String mobileNo;
        private String email;
        private String profileImageUrl;
        private String documentId;


        public TouristGuide() { }

        public TouristGuide(String firstName, String lastName, String mobileNo, String email, String profileImageUrl, String documentId) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.mobileNo = mobileNo;
            this.email = email;
            this.profileImageUrl = profileImageUrl;
            this.documentId = documentId;

        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getMobileNo() { return mobileNo; }
        public String getEmail() { return email; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }


    }

    // RecyclerView Adapter class
    public static class TouristGuideAdapter extends RecyclerView.Adapter<TouristGuideAdapter.TouristGuideViewHolder> {

        private List<TouristGuide> touristGuideList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {

            void onItemClick(String documentId);
        }

        public TouristGuideAdapter(List<TouristGuide> touristGuideList, OnItemClickListener listener) {
            this.touristGuideList = touristGuideList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TouristGuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_touristguide_card, parent, false);
            return new TouristGuideViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull TouristGuideViewHolder holder, int position) {
            TouristGuide touristGuide = touristGuideList.get(position);

            holder.nameTextView.setText(touristGuide.getFirstName() + " " + touristGuide.getLastName());
            holder.contactTextView.setText(touristGuide.getMobileNo());
            holder.emailTextView.setText(touristGuide.getEmail());
            Picasso.get().load(touristGuide.getProfileImageUrl()).transform(new CircleTransform()).into(holder.profileImageView);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(touristGuide.getDocumentId()); // Pass document ID
                }
            });

        }

        @Override
        public int getItemCount() {
            return touristGuideList.size();
        }

        // ViewHolder class for RecyclerView
        public class TouristGuideViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView,  contactTextView, emailTextView;
            ImageView profileImageView;

            public TouristGuideViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.text_name);
                contactTextView = itemView.findViewById(R.id.text_contact);
                emailTextView = itemView.findViewById(R.id.text_email);
                profileImageView = itemView.findViewById(R.id.profile_image);
            }

        }
    }

}