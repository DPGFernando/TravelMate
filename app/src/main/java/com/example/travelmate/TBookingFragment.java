package com.example.travelmate;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TBookingFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Booking> bookingList;
    private BookingAdapter adapter;
    private String userID;

   
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_t_booking, container, false);

        // Initialize Firestore and the booking list
        db = FirebaseFirestore.getInstance();

        bookingList = new ArrayList<>();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        // Setup the adapter
        adapter = new BookingAdapter(bookingList, position -> {
            Booking bookingToDelete = bookingList.get(position);
            deleteBookingFromFirestore(bookingToDelete);
            bookingList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(getContext(), "Booking deleted", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        // Fetch bookings from Firestore for the current tourist guide
        fetchBookingsFromFirestore();

        return view;
    }

    // Fetch data from Firestore and populate the booking list based on the tourist guide ID
    private void fetchBookingsFromFirestore() {
        DocumentReference guideDocRef =  db.collection("TouristGuide").document(userID);

        CollectionReference bookingsRef = guideDocRef.collection("Bookings");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        bookingsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Booking booking = document.toObject(Booking.class);
                    bookingList.add(booking);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Delete a booking from Firestore based on the booking document ID
    private void deleteBookingFromFirestore(Booking booking) {
        DocumentReference guideDocRef = db.collection("TouristGuide").document(userID);
        CollectionReference bookingsRef = guideDocRef.collection("Bookings");

        bookingsRef.whereEqualTo("touristName", booking.getTouristName()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                bookingsRef.document(document.getId()).delete();
            }
        });
    }

    // Booking model class with added touristGuideId field
    public static class Booking {
        private String touristName;
        //private String age;
        private String packageName;
        private String touristContact;
        private String touristEmail;
        private String touristId;


        public Booking() { }

        public Booking(String touristName, String packageName, String touristContact, String touristEmail, String touristId) {
            this.touristName = touristName;
            //this.age = age;
            this.packageName = packageName;
            this.touristContact = touristContact;
            this.touristEmail = touristEmail;
            this.touristId = touristId;

        }

        public String getTouristName() { return touristName; }
        //public String getAge() { return age; }
        public String getPackageName() { return packageName; }
        public String getTouristContact() { return touristContact; }
        public String getTouristEmail() { return touristEmail; }
        public String getTouristId() { return touristId; }

    }

    // RecyclerView Adapter class
    public static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        private List<Booking> bookingList;
        private OnItemClickListener listener;
        StorageReference fileRef, firebaseStorage;

        public interface OnItemClickListener {
            void onDeleteClick(int position);
        }

        public BookingAdapter(List<Booking> bookingList, OnItemClickListener listener) {
            this.bookingList = bookingList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tbooking_card, parent, false);
            firebaseStorage = FirebaseStorage.getInstance().getReference();
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            Booking booking = bookingList.get(position);
            holder.nameTextView.setText(booking.getTouristName());
            //holder.ageTextView.setText(String.valueOf(booking.getAge() + " years"));
            holder.packNameTextView.setText(booking.getPackageName());
            holder.contactTextView.setText(booking.getTouristContact());
            holder.emailTextView.setText(booking.getTouristEmail());
            fileRef = firebaseStorage.child("Tourist/" + booking.getTouristId() +"/profile.jpg");


            // Handle delete button click with confirmation dialog
            holder.deleteButton.setOnClickListener(v -> {
                // Create an AlertDialog for confirmation
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Booking")
                        .setMessage("Are you sure you want to delete this booking?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Only delete if the user confirms
                            if (listener != null) {
                                listener.onDeleteClick(position);
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Do nothing, just dismiss the dialog
                            dialog.dismiss();
                        })
                        .show();
            });

            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).transform(new CircleTransform()).into(holder.profileImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.profileImage.setImageResource(R.drawable.sample_profile);
                }
            });
        }

        @Override
        public int getItemCount() {
            return bookingList.size();
        }

        // ViewHolder class for RecyclerView
        public class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, ageTextView, packNameTextView, contactTextView, emailTextView;
            ImageView deleteButton, profileImage;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.text_name);
                ageTextView = itemView.findViewById(R.id.text_age);
                packNameTextView = itemView.findViewById(R.id.text_location);
                contactTextView = itemView.findViewById(R.id.text_contact);
                emailTextView = itemView.findViewById(R.id.text_email);
                deleteButton = itemView.findViewById(R.id.delete_button);
                profileImage = itemView.findViewById(R.id.profile_image);
            }

        }
    }
}
