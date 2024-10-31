package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class eventPaymentGateway extends AppCompatActivity {

    String managerId;
    PaymentButtonContainer paymentButtonContainer;
    String touristGuideId, touristId, packageId, packageName, touristName, touristContact, touristEmail, packagePrice;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_payment_gateway);

        managerId = getIntent().getStringExtra("managerId");

        paymentButtonContainer = findViewById(R.id.bookButton);
        touristId = getIntent().getStringExtra("touristId");
        packageId = getIntent().getStringExtra("eventId");
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        DocumentReference touristDoc = fStore.collection("Tourist").document(touristId);
        DocumentReference eventManagerDoc = fStore.collection("EventManager").document(managerId);
        DocumentReference packageDoc = eventManagerDoc.collection("Events").document(packageId);
        DocumentReference bookingDoc = eventManagerDoc.collection("Bookings").document();
        DocumentReference touristBookingDoc = touristDoc.collection("Bookings").document();

        touristDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                touristName = value.getString("fName") + " " + value.getString("lName");
                touristContact = value.getString("mNum");
                touristEmail = value.getString("Email");
            }
        });

        packageDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                packageName = value.getString("eventName");
                packagePrice = value.getString("entranceFee");
            }
        });


        paymentButtonContainer.setup(
                createOrderActions -> {
                    ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                    purchaseUnits.add(
                            new PurchaseUnit.Builder()
                                    .amount(
                                            new Amount.Builder()
                                                    .currencyCode(CurrencyCode.USD)
                                                    .value(packagePrice)
                                                    .build()
                                    ).build()
                    );
                    OrderRequest order = new OrderRequest(
                            OrderIntent.CAPTURE,
                            new AppContext.Builder()
                                    .userAction(UserAction.PAY_NOW)
                                    .build(),
                            purchaseUnits
                    );
                    createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                },
                approval -> {
                    Log.i("TAG", "OrderId: " + approval.getData().getOrderId());
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("touristName", touristName);
                    booking.put("touristContact", touristContact);
                    booking.put("touristEmail", touristEmail);
                    booking.put("packageName", packageName);
                    booking.put("touristId", touristId);


                    Map<String, Object> touristBooking = new HashMap<>();
                    touristBooking.put("packageName", packageName);
                    touristBooking.put("packageId", packageId);
                    touristBooking.put("Id", managerId);

                    touristBookingDoc.set(touristBooking);

                    bookingDoc.set(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Booking Add Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(eventPaymentGateway.this, EventDetailActivity.class);
                            intent.putExtra("touristGuideId", touristGuideId);
                            intent.putExtra("packageId", packageId);
                            intent.putExtra("touristId", touristId);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(eventPaymentGateway.this, "Error : " + e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    });



                }
        );


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}