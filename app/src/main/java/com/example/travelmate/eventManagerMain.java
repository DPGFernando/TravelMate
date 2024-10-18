package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.travelmate.databinding.ActivityEventManagerMainBinding;
import com.example.travelmate.databinding.ActivityTouristGuideMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class eventManagerMain extends AppCompatActivity {

    FirebaseAuth fAuth;
    ActivityEventManagerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        fAuth = FirebaseAuth.getInstance();

        FirebaseUser user = fAuth.getCurrentUser();
        if(!user.isEmailVerified()){
            Toast.makeText(eventManagerMain.this, "Email is not verified", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, loggin.class);
            startActivity(i);
        }

        binding = ActivityEventManagerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new EHomeFragment());

        binding.eventbottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new EHomeFragment());
            } else if (itemId == R.id.add) {
                replaceFragment(new EAddFragment());
            } else if (itemId == R.id.bookings) {
                replaceFragment(new EBookingFragment());
            }

            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.replace_eventlayout, fragment).commit();
    }
}