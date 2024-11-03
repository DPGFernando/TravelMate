package com.example.travelmate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class touristProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvContact, username, uemail;
    private EditText etName, etEmail, etPassword, etContact;
    private ImageView image, backButton;
    private MaterialButton btnEdit, logOutbtn, btnProfileEdit;
    private Button saveChanges;
    private GridView bookingGridView;


    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    StorageReference firebaseStorage;
    List<MyBooking> bookingList;
    MyBookingAdapter adapter;
    FirebaseFirestore db;
    Uri imageUri;
    DocumentReference documentReference;
    StorageReference fileRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_profile);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        username = findViewById(R.id.username);
        uemail = findViewById(R.id.email_usern);
        backButton = findViewById(R.id.backButton);
        tvContact = findViewById(R.id.contact_947);
        image = findViewById(R.id.image);
        saveChanges = findViewById(R.id.saveChanges);
        btnEdit = findViewById(R.id.pen);
        btnProfileEdit = findViewById(R.id.pen1);
        logOutbtn = findViewById(R.id.logOut);

        db = FirebaseFirestore.getInstance();
        bookingList = new ArrayList<>();
        adapter = new MyBookingAdapter(this, bookingList);


        bookingGridView = findViewById(R.id.bookingGridView);

        bookingGridView.setAdapter(adapter);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        fileRef = firebaseStorage.child("Tourist/" + userID + "/profile.jpg");
        documentReference = fStore.collection("Tourist").document(userID);

        loadUserData();
        fetchBookingFromFirestore();


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
                Intent intent = new Intent(touristProfile.this,editTourist.class);
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
                        Toast.makeText(touristProfile.this, "Image upload succeessfully", Toast.LENGTH_SHORT).show();
                        saveChanges.setEnabled(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(touristProfile.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(touristProfile.this, touristMain.class);
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
                String name = value.getString("fName") + " " + value.getString("lName");
                String email = value.getString("Email");
                String contact = value.getString("mNum");


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

    private void fetchBookingFromFirestore() {

        CollectionReference guideRef = db.collection("Tourist").document(userID).collection("Bookings");

        // Query to get bookings where the touristGuideId matches the current guide's ID
        guideRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document1 : task.getResult()) {
                    MyBooking booking = document1.toObject(MyBooking.class);
                    //booking.setDocumentId(document1.getId());
                    bookingList.add(booking);
                }
                adapter.notifyDataSetChanged();

            }
        });

    }

    public static class MyBooking{
        private String Id;
        private String packageName;
        private String packageId;

        public MyBooking(String Id, String packageName, String packageId) {
            this.Id = Id;
            this.packageName = packageName;
            this.packageId = packageId;
        }

        public MyBooking() {}

        public String getId() {
            return Id;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getPackageId() {
            return packageId;
        }
    }

    public static class MyBookingAdapter extends BaseAdapter {
        private Context context;
        private List<MyBooking> bookings;
        StorageReference fileRef, firebaseStorage, fileRef1;

        public MyBookingAdapter(Context context, List<MyBooking> bookings) {
            this.context = context;
            this.bookings = bookings;
        }

        public int getCount() {
            return bookings.size();
        }

        @Override
        public Object getItem(int position) {
            return bookings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MyBooking booking = bookings.get(position);
            holder.nameTextView.setText(booking.getPackageName());

            firebaseStorage = FirebaseStorage.getInstance().getReference();
            fileRef = firebaseStorage.child("Packages/" + booking.getId() + "/" + booking.getPackageId() + ".jpg");
            fileRef1 = firebaseStorage.child("Events/" + booking.getId() + "/" + booking.getPackageId() + ".jpg");

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

            fileRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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