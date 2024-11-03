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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class touristGuideMain extends AppCompatActivity {

    FirebaseAuth fAuth;
    ActivityTouristGuideMainBinding binding;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);


        FirebaseUser user = fAuth.getCurrentUser();
        if(!user.isEmailVerified()){
            Toast.makeText(touristGuideMain.this, "Email is not verified", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, loggin.class);
            startActivity(i);
        }

        binding =ActivityTouristGuideMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new THomeFragment(), bundle);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new THomeFragment(), bundle);
            } else if (itemId == R.id.add) {
                replaceFragment(new TAddFragment(), bundle);
            } else if (itemId == R.id.bookings) {
                replaceFragment(new TBookingFragment(), bundle);
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

    private void replaceFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.replace_layout, fragment).commit();
    }

}