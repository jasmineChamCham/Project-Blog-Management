package com.example.blogapp.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.blogapp.model.Blog;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BlogRepository {
    FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> options;
    DatabaseReference blogsRef;
    public static FirebaseAuth mAuth;

    public BlogRepository() {
        database = FirebaseDatabase.getInstance();
        blogsRef = database.getReference().child("blogs");
        options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("jasmine@gmail.com", "jasmine123")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("DEBUG", "login successful");
                        } else {
                            Log.d("DEBUG", "login failed");
                        }
                    }
                });
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
