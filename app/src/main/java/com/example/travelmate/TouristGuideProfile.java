package com.example.travelmate;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class TouristGuideProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvContact, username, uemail ;
    private EditText etName, etEmail, etPassword, etContact;
    private ImageView image, backButton;
    private MaterialButton btnEdit, logOutbtn,btnProfileEdit;
    private Button saveChanges;
    private List<Reviews> reviewList;
    private ReviewsAdapter adapter;
    private RecyclerView recyclerView;


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
        setContentView(R.layout.activity_tourist_guide_profile);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        username = findViewById(R.id.username);
        uemail = findViewById(R.id.email_usern);
        tvContact = findViewById(R.id.contact_947);
        backButton = findViewById(R.id.backButton);
        image = findViewById(R.id.image);

        btnEdit = findViewById(R.id.pen);
        logOutbtn = findViewById(R.id.logOut);
        saveChanges = findViewById(R.id.saveChanges);
        btnProfileEdit = findViewById(R.id.pen1);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        fileRef = firebaseStorage.child("TouristGuide/" + userID +"/profilePicture.jpg");
        documentReference = fStore.collection("TouristGuide").document(userID);

        reviewList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewsAdapter(reviewList);
        recyclerView.setAdapter(adapter);

        loadUserData();
        fetchReviewFromFirestore();


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
                Intent intent = new Intent(TouristGuideProfile.this, editTouristGuide.class);
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
                        Toast.makeText(TouristGuideProfile.this, "Image upload succeessfully", Toast.LENGTH_SHORT).show();
                        saveChanges.setEnabled(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TouristGuideProfile.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TouristGuideProfile.this, touristGuideMain.class);
            startActivity(intent);
            finish();

        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button
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
                Picasso.get().load(uri).transform(new CircleTransform()).into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                image.setImageResource(R.drawable.img_3);
            }
        });

    }

    private void fetchReviewFromFirestore() {

        DocumentReference guideDocRef =  fStore.collection("TouristGuide").document(userID);

        CollectionReference ratingRef = guideDocRef.collection("Reviews");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        ratingRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Reviews review = document.toObject(Reviews.class);
                    reviewList.add(review);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static class Reviews {
        private String rating;
        private String title;
        private String review;
        private String touristId;

        public Reviews() {
        }

        public Reviews(String rating, String title, String review, String touristId) {
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

    public static class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

        private List<Reviews> reviewList;
        private OnItemClickListener listener;
        StorageReference fileRef, firebaseStorage;

        public interface OnItemClickListener {

        }

        public ReviewsAdapter(List<Reviews> reviewList) {
            this.reviewList = reviewList;
        }

        public ReviewsAdapter(List<Reviews> reviewList, OnItemClickListener listener) {
            this.reviewList = reviewList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_card, parent, false);
            firebaseStorage = FirebaseStorage.getInstance().getReference();
            return new ReviewsAdapterViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
            Reviews reviews = reviewList.get(position);

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
        public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
            TextView reviewTitle, reviewBody;
            RatingBar ratingBar;
            ImageView profileImageView;

            public ReviewsAdapterViewHolder(@NonNull View itemView) {
                super(itemView);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                reviewTitle = itemView.findViewById(R.id.reviewTitle);
                reviewBody = itemView.findViewById(R.id.review);
                profileImageView = itemView.findViewById(R.id.touristProfile);
            }

        }
    }

}