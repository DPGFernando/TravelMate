package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private MaterialButton loginButton, createAccountButton;
    private ImageView logoImageView;
    private TextView travelMateTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginbtn);
        createAccountButton = findViewById(R.id.createbtn);
        logoImageView = findViewById(R.id.imageView);
        travelMateTextView = findViewById(R.id.travelmate);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signUpIntent = new Intent(MainActivity.this, com.example.travelmate.SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

    }
}