package com.example.travelmate;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EBookingFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Booking> bookingList;
    private BookingAdapter adapter;
    private String userID;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_e_booking, container, false);

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

        // Fetch bookings from Firestore for the current event manager
        fetchBookingsFromFirestore();

        return view;
    }

    // Fetch data from Firestore and populate the booking list based on the event manager ID
    private void fetchBookingsFromFirestore() {
        DocumentReference eventDocRef =  db.collection("EventManger").document(userID);

        CollectionReference bookingsRef = eventDocRef.collection("Bookings");

        // Query to get bookings where the EventManagerId matches the current manager's ID
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
        DocumentReference eventDocRef = db.collection("EventManager").document(userID);
        CollectionReference bookingsRef = eventDocRef.collection("Bookings");

        bookingsRef.whereEqualTo("name", booking.getName()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                bookingsRef.document(document.getId()).delete();
            }
        });
    }

    // Booking model class with added eventManagerId field
    public static class Booking {
        private String name;
        private String age;
        private String eventName;
        private String contact;
        private String email;


        public Booking() { }

        public Booking(String name, String age, String eventName, String contact, String email) {
            this.name = name;
            this.age = age;
            this.eventName = eventName;
            this.contact = contact;
            this.email = email;

        }

        public String getName() { return name; }
        public String getAge() { return age; }
        public String getEventName() { return eventName; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }

    }

    // RecyclerView Adapter class
    public static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        private List<Booking> bookingList;
        private OnItemClickListener listener;

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebooking_card, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            Booking booking = bookingList.get(position);
            holder.nameTextView.setText(booking.getName());
            holder.ageTextView.setText(String.valueOf(booking.getAge() + " years"));
            holder.eventNameTextView.setText(booking.getEventName());
            holder.contactTextView.setText(booking.getContact());
            holder.emailTextView.setText(booking.getEmail());

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
        }

        @Override
        public int getItemCount() {
            return bookingList.size();
        }

        // ViewHolder class for RecyclerView
        public class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, ageTextView, eventNameTextView, contactTextView, emailTextView;
            ImageView deleteButton;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.text_name);
                ageTextView = itemView.findViewById(R.id.text_age);
                eventNameTextView = itemView.findViewById(R.id.text_eventName);
                contactTextView = itemView.findViewById(R.id.text_contact);
                emailTextView = itemView.findViewById(R.id.text_email);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }

        }
    }
}
