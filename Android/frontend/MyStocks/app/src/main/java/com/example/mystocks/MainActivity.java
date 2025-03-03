package com.example.mystocks;// MainActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView splashImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        Toolbar toolbar = findViewById(R.id.toolbar);
        splashImageView = findViewById(R.id.splashImageView);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Show progress bar
//        progressBar.setVisibility(ProgressBar.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashImageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }, 1000);

        // Simulate loading process for 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide progress bar
                progressBar.setVisibility(ProgressBar.GONE);

                // Navigate to HomeActivity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Optional: Prevents user from going back to splash screen using back button
            }
        }, 3000);
    }
}
