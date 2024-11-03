package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.travelmate.databinding.ActivityTouristGuideMainBinding;
import com.example.travelmate.databinding.ActivityTouristMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class touristMain extends AppCompatActivity {

    FirebaseAuth fAuth;
    ActivityTouristMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tourist_main);

        fAuth = FirebaseAuth.getInstance();

        FirebaseUser user = fAuth.getCurrentUser();
        if(!user.isEmailVerified()){
            Toast.makeText(touristMain.this, "Email is not verified", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, loggin.class);
            startActivity(i);
        }

        binding = ActivityTouristMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.events) {
                replaceFragment(new EventFragment());
            } else if (itemId == R.id.touristGuide) {
                replaceFragment(new TouristGuideFragment());
            } else if (itemId == R.id.map) {
                replaceFragment(new MapFragment());
            } else if (itemId == R.id.contact) {
                replaceFragment(new ContactFragment());
            }

            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.replace_layout, fragment).commit();
    }
}