package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the button and blank space view by their IDs
        //Button buttonClick = findViewById(R.id.button_click);


        /* Set an OnClickListener for the button
        buttonClick.setOnClickListener(new View.OnClickListener() {
           /* @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, loggin.class);
                startActivity(intent);
            }*/
        }
    }
