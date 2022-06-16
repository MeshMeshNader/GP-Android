package com.grad.gp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grad.gp.Models.UserDataModel;
import com.grad.gp.R;
import com.grad.gp.Utils.AESCrypt;
import com.grad.gp.Utils.CustomProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private final String TAG = "Signup";

    //View
    Button mSignUp;
    EditText mName, mEmail, mPhoneNumber, mPassword, mConfirmPassword;
    TextView mHaveAnAccount;
    Map<String, String> mImagesURL;


    //Firebase
    FirebaseAuth mAuth;
    DatabaseReference UsersRef;

    //ProgressBar
    CustomProgress mCustomProgress = CustomProgress.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();

    }

    private void initViews() {
        //Button
        mSignUp = findViewById(R.id.signup_signup_btn);
        mSignUp.setOnClickListener(v -> createAccount());
        //Text View
        mHaveAnAccount = findViewById(R.id.signup_already_have_acc);
        mHaveAnAccount.setOnClickListener(v -> alreadyHaveAnAccount());
        //Edit Text
        mName = findViewById(R.id.signup_name_et);
        mEmail = findViewById(R.id.signup_email_et);
        mPhoneNumber = findViewById(R.id.signup_phone_et);
        mPassword = findViewById(R.id.signup_password_et);
        mConfirmPassword = findViewById(R.id.signup_confirm_password_et);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference("Users");
        mImagesURL = new HashMap<>();
    }


    private void alreadyHaveAnAccount() {
        startActivity(new Intent(Signup.this, Login.class));
        finish();
    }


    private void createAccount() {
        if (validate()) {
            mCustomProgress.showProgress(this, "Please Wait...!", false);

            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                saveAccountData();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(Signup.this, "Error Occurred: " + message, Toast.LENGTH_LONG).show();
                                mCustomProgress.hideProgress();
                            }

                        }
                    });
        }
    }


    private void saveAccountData() {
        String password;
        try {
            password = AESCrypt.encrypt(mPassword.getText().toString());
        } catch (Exception e) {
            Log.e(TAG, "saveAccountUserData: " + e.getMessage());
            password = mPassword.getText().toString();
        }

        String currentUserID = mAuth.getCurrentUser().getUid();

        Map<String, String> map = new HashMap<>();

        UserDataModel dataModel = new UserDataModel(currentUserID,
                mName.getText().toString(),
                mEmail.getText().toString(),
                mPhoneNumber.getText().toString(),
                password,map,mImagesURL,map);

        UsersRef.child(currentUserID).setValue(dataModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.e(TAG, "onComplete: data has been saved successfully");
                    mCustomProgress.hideProgress();
                    Toast.makeText(Signup.this, "Please Login to your account", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Signup.this, Login.class));
                    finish();
                    mAuth.signOut();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(Signup.this, "Error Occurred :" + message, Toast.LENGTH_LONG).show();
                    mCustomProgress.hideProgress();
                }

            }
        });

    }


    private boolean validate() {
        if (mName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_LONG).show();
            return false;
        } else if (mEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your email address", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPhoneNumber.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your phone number", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPhoneNumber.getText().toString().length() < 11 || mPhoneNumber.getText().toString().length() > 11) {
            Toast.makeText(this, "Enter valid phone number (11 digits)", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPassword.getText().toString().length() < 8) {
            mPassword.setError("Password < 8");
            Toast.makeText(this, "Your password cannot be less than 8 characters", Toast.LENGTH_LONG).show();
            return false;
        } else if (mConfirmPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please, Confirm your password", Toast.LENGTH_LONG).show();
            return false;
        } else if (!(mPassword.getText().toString().equals(mConfirmPassword.getText().toString()))) {
            mConfirmPassword.setError("Passwords do not matched");
            Toast.makeText(this, "Please, Confirm your password", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}