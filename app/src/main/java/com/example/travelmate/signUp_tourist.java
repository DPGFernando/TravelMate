package com.example.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.HashMap;


public class signUp_tourist extends AppCompatActivity {
    EditText First_Name, Last_Name, Email, Password, Re_Password, Mobile_Num, Passport_ID;
    Button SingUpB;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    ImageView profileImage;
    Button changeProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_tourist);



            First_Name = (EditText) findViewById(R.id.First_Name);
            Last_Name = (EditText) findViewById(R.id.Last_Name);
            Email= (EditText) findViewById(R.id.Email);
            Password=(EditText) findViewById(R.id.Password);
            Re_Password = (EditText) findViewById(R.id.Re_Password);
            Mobile_Num = (EditText) findViewById(R.id.Mobile_Num);
            Passport_ID = (EditText) findViewById(R.id.Passport_ID);
            SingUpB =  findViewById(R.id.SingUpB);
            profileImage = findViewById(R.id.profileImage);
            changeProfileButton = findViewById(R.id.changeProfileButton);




            fAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();

            SingUpB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // String Passport_ID = Passport_ID.getText().toString.trim;

                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String,Object> user = new HashMap<>();
                    user.put("fName",First_Name);
                    user.put("lName", Last_Name);
                    user.put("Email",Email);
                    user.put("Password",Password);
                    user.put("RePassword",Re_Password);
                    user.put("mNum",Mobile_Num);
                    user.put("Passport",Passport_ID);



                }
            });

            changeProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(openGalleryIntent, 1000);

                }
            });






    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (requestCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                profileImage.setImageURI(imageUri);
            }
        }
    }
}