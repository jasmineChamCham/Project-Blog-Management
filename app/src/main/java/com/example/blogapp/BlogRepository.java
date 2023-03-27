package com.example.blogapp;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.blogapp.model.Blog;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BlogRepository {
    FirebaseDatabase database;
    FirebaseRecyclerOptions<Blog> options;
    DatabaseReference blogsRef;

    public BlogRepository() {
        database = FirebaseDatabase.getInstance();
        blogsRef = database.getReference().child("blogs");
        options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();
    }

    public void addNote(String title, String content, String createTime, String userID, int likeNumber, int viewNumber, String category, String status) {

        String id = blogsRef.push().getKey();
        blogsRef.child(id).setValue(new Blog(id, title, content, createTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG","success add");
                        } else {
                            Log.d("DEBUG","success add");
                        }
                    }
                });
    }
}
