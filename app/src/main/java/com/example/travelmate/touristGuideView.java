package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class touristGuideView extends AppCompatActivity {

    TextView tName, tContact, tEmail;
    ImageView tImage;
    Button viewPackages, submitReview;
    String touristGuideId,touristId;
    String ufName, ulName, uAge, uContact, uEmail;
    RatingBar ratingBar;
    EditText reviewTitle, review;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    DocumentReference documentReference;
    private List<Review> reviewList;
    private ReviewAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tourist_guide_view);

        touristGuideId = getIntent().getStringExtra("touristGuideId");
        touristId = getIntent().getStringExtra("touristId");
        tName = findViewById(R.id.tName);
        tContact = findViewById(R.id.tContact);
        tEmail = findViewById(R.id.tEmail);
        tImage = findViewById(R.id.tImage);
        viewPackages = findViewById(R.id.viewPackages);
        ratingBar = findViewById(R.id.ratingBar);
        reviewTitle = findViewById(R.id.reviewTitle);
        review = findViewById(R.id.review);
        submitReview = findViewById(R.id.submitReview);

        reviewList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.v("tourist Guide ID", touristGuideId);
        Log.v("tourist ID", touristId);


        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        adapter = new ReviewAdapter(reviewList);

        recyclerView.setAdapter(adapter);


        fetchReviewFromFirestore();

        documentReference = fStore.collection("TouristGuide").document(touristGuideId);
        StorageReference fileRef = storageReference.child("TouristGuide/"+ touristGuideId +"/profilePicture.jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(tImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getMessage());
            }
        });

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                ufName = value.getString("firstName");
                ulName = value.getString("lastName");
                uContact = value.getString("mobileNo");
                uEmail = value.getString("email");

                tName.setText("Name : " + ufName + " " + ulName);
                tContact.setText("Contact : " + uContact);
                tEmail.setText("Email : " + uEmail);
            }
        });

        viewPackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(touristGuideView.this, viewTouristPackages.class);
                intent.putExtra("touristGuideId", touristGuideId);
                intent.putExtra("touristId", touristId);
                startActivity(intent);
            }
        });

        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchReviewFromFirestore() {

        DocumentReference guideDocRef =  fStore.collection("TouristGuide").document(touristGuideId);

        CollectionReference ratingRef = guideDocRef.collection("Reviews");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        ratingRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Review review = document.toObject(Review.class);
                    reviewList.add(review);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static class Review {
        private String rating;
        private String title;
        private String review;
        private String touristId;

        public Review() {
        }

        public Review(String rating, String title, String review, String touristId) {
            this.rating = rating;
            this.title = title;
            this.review = review;
            this.touristId = touristId;
        }

        public String getRating() {
            return rating;
        }

        public String getTitle() {
            return title;
        }

        public String getReview() {
            return review;
        }

        public String getTouristId() {
            return touristId;
        }

    }

    public static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder>{

        private List<Review> reviewList;
        private OnItemClickListener listener;
        StorageReference fileRef, firebaseStorage;

        public interface OnItemClickListener {

        }

        public ReviewAdapter(List<Review> reviewList) {
            this.reviewList = reviewList;
        }

        public ReviewAdapter(List<Review> reviewList, OnItemClickListener listener) {
            this.reviewList = reviewList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_card, parent, false);
            firebaseStorage = FirebaseStorage.getInstance().getReference();
            return new ReviewAdapterViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
            Review reviews = reviewList.get(position);

            holder.ratingBar.setRating(Float.parseFloat(reviews.getRating()));
            holder.ratingBar.setIsIndicator(true);
            holder.reviewTitle.setText(reviews.getTitle());
            holder.reviewBody.setText(reviews.getReview());

            fileRef = firebaseStorage.child("Tourist/" + reviews.getTouristId() +"/profile.jpg");

            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).transform(new CircleTransform()).into(holder.profileImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.profileImageView.setImageResource(R.drawable.sample_profile);
                }
            });

        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        // ViewHolder class for RecyclerView
        public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
            TextView  reviewTitle, reviewBody;
            RatingBar ratingBar;
            ImageView profileImageView;

            public ReviewAdapterViewHolder(@NonNull View itemView) {
                super(itemView);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                reviewTitle = itemView.findViewById(R.id.reviewTitle);
                reviewBody = itemView.findViewById(R.id.review);
                profileImageView = itemView.findViewById(R.id.touristProfile);
            }

        }
    }

    private void submitData() {

        String rating = String.valueOf(ratingBar.getRating());
        String title = reviewTitle.getText().toString();
        String reviewText = review.getText().toString();

        DocumentReference ratingRef = documentReference.collection("Reviews").document();
        Map<String, Object> userreview = new HashMap<>();
        userreview.put("rating", rating);
        userreview.put("title", title);
        userreview.put("review", reviewText);
        userreview.put("touristId", touristId);

        ratingRef.set(userreview).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Review Submitted", Toast.LENGTH_SHORT).show();
                reviewTitle.setText("");
                review.setText("");
                ratingBar.setRating(0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}