package com.example.travelmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.travelmate.databinding.ActivityTouristGuideMainBinding;

public class touristGuideMain extends AppCompatActivity {

    ActivityTouristGuideMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding =ActivityTouristGuideMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new THomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new THomeFragment());
            } else if (itemId == R.id.add) {
                replaceFragment(new TAddFragment());
            } else if (itemId == R.id.bookings) {
                replaceFragment(new TBookingFragment());
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
        getSupportFragmentManager().beginTransaction().replace(R.id.replace_layout, fragment).commit();
    }

}