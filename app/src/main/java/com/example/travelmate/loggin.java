package com.example.travelmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;



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



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }





        });


        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loggin.this, ForgotActivity.class);
                startActivity(intent);
                handleForgotPassword();
            }
        });


        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(loggin.this, SignupActivity.class);
                startActivity(intent);
                handleSignUp();
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


                fAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(loggin.this, "Logged in successfully ", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent(loggin.this, LoginActivity.class);
                            startActivity(intent);
                             } else {
                            Toast.makeText(loggin.this, "Error ", Toast.LENGTH_SHORT).show();
                        }
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