package com.grad.gp.Home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.grad.gp.R;

public class HomePage extends AppCompatActivity {

    ImageView mAlzheimer , mVisuallyImpaired;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        initViews();

    }

    private void initViews() {
        mAlzheimer = findViewById(R.id.alzheimer_btn);
        mVisuallyImpaired = findViewById(R.id.visually_impaired);

        mAlzheimer.setOnClickListener(v -> goToAlzhimerPage());
        mVisuallyImpaired.setOnClickListener(v -> goToVisuallyImpaired());
    }

    private void goToVisuallyImpaired() {
        Intent i = new Intent(HomePage.this, VisuallyImpaired.class);
        startActivity(i);

    }

    private void goToAlzhimerPage() {
        Intent i = new Intent(HomePage.this, Zhaimer.class);
        startActivity(i);

    }
}