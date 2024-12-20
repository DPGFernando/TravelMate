package com.example.travelmate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class loggin extends AppCompatActivity {


    private ImageView icon;
    private TextView title;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private Button loginButton;
    private TextView signUpTextView;
    private TextView clearText;
    private FirebaseFirestore fStore;
    private CollectionReference touristCollectionReference;
    private CollectionReference touristGuideCollectionReference;
    private CollectionReference eventManagerCollectionReference;
    String userID;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggin);


        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        rememberMeCheckBox = findViewById(R.id.remember_me);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.sign_up);
        clearText = findViewById(R.id.text2);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        touristCollectionReference = fStore.collection("Tourist");
        touristGuideCollectionReference = fStore.collection("EventManager");
        eventManagerCollectionReference = fStore.collection("TouristGuide");


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }


        });


        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Password Reset");
                passwordResetDialog.setMessage("Enter your email");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(loggin.this, "Reset link was sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(loggin.this, "Reset Link was not sent" + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordResetDialog.create().show();
            }
        });


        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loggin.this, signUpPage.class);
                startActivity(intent);
                handleSignUp();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button
            }
        });
    }


    private void handleLogin() {

        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();


        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter username or email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please enter password");
            return;
        }


        fAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(loggin.this, "Logged in successfully ", Toast.LENGTH_SHORT).show();
                    userID = fAuth.getCurrentUser().getUid();
                    loadingInterface(userID);
                } else {
                    Toast.makeText(loggin.this, "Email or password is wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void loadingInterface(String userId) {
        fStore.collection("Tourist").document(userId)
                .get()
                .addOnCompleteListener(touristTask -> {
                    if (touristTask.isSuccessful() && touristTask.getResult().exists()) {
                        // User is a Tourist
                        Intent intent = new Intent(this, touristMain.class);
                        intent.putExtra("userID", userId);
                        startActivity(intent);
                        finish();
                    } else {
                        // Check if user is a Tourist Guide
                        fStore.collection("TouristGuide").document(userId)
                                .get()
                                .addOnCompleteListener(guideTask -> {
                                    if (guideTask.isSuccessful() && guideTask.getResult().exists()) {
                                        // User is a Tourist Guide
                                        Intent intent = new Intent(this, touristGuideMain.class);
                                        intent.putExtra("userID", userId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Check if user is an Event Manager
                                        fStore.collection("EventManager").document(userId)
                                                .get()
                                                .addOnCompleteListener(managerTask -> {
                                                    if (managerTask.isSuccessful() && managerTask.getResult().exists()) {
                                                        // User is an Event Manager
                                                        Intent intent = new Intent(this, eventManagerMain.class);
                                                        intent.putExtra("userID", userId);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // User role not found
                                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    private void handleForgotPassword() {

        Toast.makeText(this, "Forgot Password clicked.", Toast.LENGTH_SHORT).show();
    }


    private void handleSignUp() {


        Toast.makeText(this, "Sign Up clicked.", Toast.LENGTH_SHORT).show();
    }
}