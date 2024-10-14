package com.example.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TAddFragment extends Fragment {

    EditText nodays, packName, description, price;
    TextView textView,fileNameTextView, uploadImageText;
    ImageView uploadPhoto, imageView;
    Button AddPackButton, removeFileButton;
    ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    Uri photoimg;
    String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_t_add, container, false);

        imageView = view.findViewById(R.id.imageView);
        uploadPhoto = view.findViewById(R.id.uploadPhoto);
        nodays = view.findViewById(R.id.nodays);
        packName = view.findViewById(R.id.packName);
        description = view.findViewById(R.id.description);
        textView = view.findViewById(R.id.textView);
        price = view.findViewById(R.id.price);
        AddPackButton = view.findViewById(R.id.AddPackButton);
        progressBar = view.findViewById(R.id.progressBar);
        uploadImageText = view.findViewById(R.id.uploadImageText);
        fileNameTextView = view.findViewById(R.id.fileNameTextView);
        removeFileButton = view.findViewById(R.id.removeFileButton);


        removeFileButton.setVisibility(View.VISIBLE);
        removeFileButton.setEnabled(false);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getContext(), Interface.class));
            getActivity().finish();
        }

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] words = s.toString().trim().split("\\s+");
                if (words.length > 100) {
                    description.setError("Description cannot exceed 100 words");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text is changed
            }
        });


        AddPackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPackage();
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

    private void addPackage() {
        String pName = packName.getText().toString();
        String des = description.getText().toString();
        String pr = price.getText().toString();
        String nDays = nodays.getText().toString();

        String[] words = des.trim().split("\\s+");
        if (words.length > 100) {
            description.setError("Description cannot exceed 100 words");
            return;
        }

        if (pName.isEmpty()) {
            packName.setError("Package Name is required");
            return;
        }
        if (des.isEmpty()) {
            description.setError("Description is required");
            return;
        }





        if (pr.isEmpty()) {
            price.setError("Price is required");
            return;
        }
        if (nDays.isEmpty()) {
            nodays.setError("Number of Days is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        userId = fAuth.getCurrentUser().getUid();
        uploadImages(photoimg, userId);

        DocumentReference documentReference = fStore.collection("packages").document(userId);
        Map<String, Object> packagedata = new HashMap<>();
        packagedata.put("packName", pName);
        packagedata.put("description", des);
        packagedata.put("price", pr);
        packagedata.put("nodays", nDays);
        packagedata.put("photoimg", photoimg.toString());

        documentReference.set(packagedata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.v("TAG", "onSuccess: Package created for " + userId);
                Toast.makeText(getContext(), "Package Added", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                getActivity().finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("TAG", "Failed: " + e.toString());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error adding package", Toast.LENGTH_SHORT).show();
            }
        });
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
        removeFileButton.setVisibility(View.VISIBLE);  
    }


    private void uploadImages(Uri profileimg, String userID) {
        if (photoimg != null) {
            StorageReference photoRef = storageReference.child("packages/" + userID + "/photo.jpg");
            photoRef.putFile(profileimg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.v("TAG", "Image uploaded successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}