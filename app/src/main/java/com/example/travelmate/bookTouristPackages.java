package com.example.travelmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class bookTouristPackages extends AppCompatActivity {

    TextView packageTitle, packageDes, packagePrice, packageDays;
    ImageView packageImage;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String touristGuideId, packageId, touristId;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_tourist_packages);

        packageId = getIntent().getStringExtra("packageId");
        touristGuideId = getIntent().getStringExtra("touristGuideId");
        touristId = getIntent().getStringExtra("touristId");

        Log.d("packageId", packageId);
        Log.d("touristGuideId", touristGuideId);
        Log.d("touristId", touristId);

        packageTitle = findViewById(R.id.packageTitle);
        packageDes = findViewById(R.id.packageDes);
        packagePrice = findViewById(R.id.packagePrice);
        packageDays = findViewById(R.id.packageDays);
        packageImage = findViewById(R.id.packageImage);
        btn = findViewById(R.id.bookPackage);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fStore.collection("TouristGuide").document(touristGuideId);
        StorageReference fileRef = storageReference.child("Packages/"+ touristGuideId +"/" + packageId + ".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(packageImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getMessage());
            }
        });

        DocumentReference documentReference1 = documentReference.collection("Packages").document(packageId);
        documentReference1.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                packageTitle.setText(value.getString("packName"));
                packageDes.setText(value.getString("description"));
                packagePrice.setText("$" + value.getString("price"));
                packageDays.setText(value.getString("nodays") + "days");
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(bookTouristPackages.this, paymentGateway.class);
                intent.putExtra("touristGuideId", touristGuideId);
                intent.putExtra("packageId", packageId);
                intent.putExtra("touristId", touristId);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


}