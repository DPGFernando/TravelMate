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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EAddFragment extends Fragment {

    EditText eventName, date, venue, startsAt, endsAt, price, contact;
    TextView fileNameTextView, uploadImageText;
    ImageView uploadPhoto;
    Button AddEventButton, removeFileButton;
    ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    DocumentReference documentReference;
    DocumentReference documentReference1;

    Uri photoimg;
    String userId, documentId, phoneNum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_e_add, container, false);


        eventName = view.findViewById(R.id.eventName);
        date = view.findViewById(R.id.date);
        venue = view.findViewById(R.id.venue);
        startsAt = view.findViewById(R.id.startsAt);
        endsAt = view.findViewById(R.id.endsAt);
        price = view.findViewById(R.id.price);
        uploadPhoto = view.findViewById(R.id.uploadPhoto);
        fileNameTextView = view.findViewById(R.id.fileNameTextView);
        removeFileButton = view.findViewById(R.id.removeFileButton);
        AddEventButton = view.findViewById(R.id.AddEventButton);
        contact = view.findViewById(R.id.contact);
        progressBar = view.findViewById(R.id.progressBar);

        removeFileButton.setVisibility(View.GONE);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        AddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });

        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        removeFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelectedFile();
            }
        });

        return view;
    }

    private void addEvent() {
        String eName = eventName.getText().toString();
        String evDate = date.getText().toString();
        String evVenue = venue.getText().toString();
        String evStart = startsAt.getText().toString();
        String evEnd = endsAt.getText().toString();
        String evPrice = price.getText().toString();
        String evContact = contact.getText().toString();

        if (eName.isEmpty()) {
            eventName.setError("Event Name is required");
            return;
        }
        if (evDate.isEmpty()) {
            date.setError("Date is required");
            return;
        }
        if (evVenue.isEmpty()) {
            venue.setError("Venue is required");
            return;
        }
        if (evStart.isEmpty()) {
            startsAt.setError("Start Time is required");
            return;
        }
        if (evEnd.isEmpty()) {
            endsAt.setError("End Time is required");
            return;
        }
        if (evPrice.isEmpty()) {
            price.setError("Price is required");
            return;
        }
        if(evContact.isEmpty()){
            contact.setError("Contact Number is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        userId = fAuth.getCurrentUser().getUid();

        documentReference = fStore.collection("EventManager").document(userId);
        documentReference1 = documentReference.collection("Events").document();


        documentId = documentReference1.getId();

        uploadImages(photoimg, userId, documentId);


    }

    public void openGallery() {
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            photoimg = data.getData();
            uploadPhoto.setImageURI(photoimg);

            String fileName = getFileNameFromUri(photoimg);
            fileNameTextView.setText(fileName);

            removeFileButton.setEnabled(true);
            removeFileButton.setVisibility(View.VISIBLE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        File file = new File(path);
        return file.getName();
    }

    private void removeSelectedFile() {
        photoimg = null;
        uploadPhoto.setImageResource(R.drawable.img);
        fileNameTextView.setText("No file selected");

        removeFileButton.setEnabled(false);
        removeFileButton.setVisibility(View.GONE);
    }

    private void uploadImages(Uri photoUri, String userID, String documentId) {
        if (photoUri != null) {
            final StorageReference profileImageRef = storageReference.child("Events/" + userID  + "/" + documentId + ".jpg");
            profileImageRef.putFile(photoUri).addOnSuccessListener(taskSnapshot -> {
                profileImageRef.getDownloadUrl().addOnSuccessListener(profileImageUrl -> {
                    uploadData(profileImageUrl.toString());
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });

        }
    }

    private void uploadData(String imageUrl) {

        String eName = eventName.getText().toString();
        String evDate = date.getText().toString();
        String evVenue = venue.getText().toString();
        String evStart = startsAt.getText().toString();
        String evEnd = endsAt.getText().toString();
        String evPrice = price.getText().toString();
        String evContact = contact.getText().toString();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", eName);
        eventData.put("date", evDate);
        eventData.put("venue", evVenue);
        eventData.put("startsAt", evStart);
        eventData.put("endsAt", evEnd);
        eventData.put("entranceFee", evPrice);
        eventData.put("photoimg", imageUrl);
        eventData.put("managerId", userId);
        eventData.put("contact", evContact);

        documentReference1.set(eventData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.v("TAG", "onSuccess: Event created for " + userId);
                Toast.makeText(getContext(), "Event Added", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(getContext(), eventManagerMain.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("TAG", "Failed: " + e.toString());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error adding event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
