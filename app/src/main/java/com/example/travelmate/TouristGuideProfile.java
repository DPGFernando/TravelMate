package com.example.travelmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import java.text.BreakIterator;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class TouristGuideProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPassword, tvContact;
    private EditText etName, etEmail, etPassword, etContact;
    private MaterialButton btnEdit, btnSave;

    // Firebase Database reference
    private DatabaseReference databaseReference;

    private String userId = "yourUserId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_guide_profile);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPassword = findViewById(R.id.password_);
        tvContact = findViewById(R.id.contact_947);

        etName = new EditText(this);
        etEmail = new EditText(this);
        etPassword = new EditText(this);
        etContact = new EditText(this);

        btnEdit = findViewById(R.id.pen);
        btnSave = findViewById(R.id.frame_12);

        try {
            FirebaseDatabase.getInstance("Users").wait(Long.parseLong(userId));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        loadUserData();

        btnEdit.setOnClickListener(new View.OnClickListener() {



            public void onClick(View v) {
                enableEditing();
            }

            private void enableEditing() {
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }

            private void saveChanges() {
            }
        });
    }

    private void loadUserData() {
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);


                    tvName.setText(name);
                    tvEmail.setText(email);
                    tvPassword.setText(password);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        }


}

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

        }
    }


}