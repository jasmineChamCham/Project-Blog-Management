package com.example.blogapp.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.blogapp.R;
import com.example.blogapp.databinding.ActivityMyProfileBinding;

public class MyProfileActivity extends AppCompatActivity {
    private ActivityMyProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }
}