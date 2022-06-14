package com.grad.gp.Auth;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.grad.gp.Home.HomePage;
import com.grad.gp.R;
import com.grad.gp.Utils.CustomProgress;

public class Login extends AppCompatActivity {

    Button mLogin;
    EditText mEmail, mPassword;
    TextView mCreateAnAccount;
    FirebaseAuth mAuth;
    CustomProgress mCustomProgress = CustomProgress.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

    }

    private void initViews() {

        mEmail = findViewById(R.id.login_email_et);
        mPassword = findViewById(R.id.login_password_et);
        mLogin = findViewById(R.id.login_login_btn);
        mLogin.setOnClickListener(v -> loginToTheAccount());
        mCreateAnAccount = findViewById(R.id.login_create_an_acc);
        mCreateAnAccount.setOnClickListener(v -> createAnAccount());
        mAuth = FirebaseAuth.getInstance();

    }

    private void createAnAccount() {
        startActivity(new Intent(Login.this, Signup.class));
        finish();
    }


    private void loginToTheAccount() {
        if (validate()) {
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            mCustomProgress.showProgress(this, "Logging in!!...", false);


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        VerifyEmailAddress();
                    } else {
                        String messsage = task.getException().getMessage();
                        Toast.makeText(Login.this, "Error Occurred: " + messsage, Toast.LENGTH_LONG).show();
                        mCustomProgress.hideProgress();
                    }
                }
            });
        }
    }

    private void VerifyEmailAddress() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.reload();


        mCustomProgress.hideProgress();
        Toast.makeText(Login.this, "Welcome to " + getResources().getString(R.string.app_name), Toast.LENGTH_LONG).show();
        startActivity(new Intent(Login.this, HomePage.class));
        finish();


    }

    private boolean validate() {
        if (mEmail.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your email", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_LONG).show();
            return false;
        } else if (mPassword.getText().toString().length() < 8) {
            mPassword.setError("Password < 8");
            Toast.makeText(this, "Password cannot be less than 8 characters", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


}