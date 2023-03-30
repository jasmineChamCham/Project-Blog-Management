package com.example.blogapp.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.blogapp.model.Blog;
import com.example.blogapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DBHelper {
    FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> options;
    DatabaseReference blogsRef;
//    public FirebaseRecyclerOptions<Blog> options;
//    DatabaseReference usersRef;

    public DBHelper() {
        database = FirebaseDatabase.getInstance();
        blogsRef = database.getReference().child("blogs");
        options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();

//        usersRef = database.getReference().child("users");
//        options = new FirebaseRecyclerOptions.Builder<User>()
//                .setQuery(usersRef, User.class)
//                .build();
    }

    public void addNote(String title, String content, String createdTime, String userID, int likeNumber, int viewNumber, String category, String status) {

        String id = blogsRef.push().getKey();
        blogsRef.child(id).setValue(new Blog(id, title, content, createdTime, userID, likeNumber, viewNumber, category, status))
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
