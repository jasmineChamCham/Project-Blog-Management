package com.example.blogapp.viewmodel;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.blogapp.model.Blog;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class BlogRepository {
    FirebaseDatabase database;
    public FirebaseRecyclerOptions<Blog> optionAll, optionPublished, optionDrafts, optionTrashed;
    DatabaseReference blogsRef;
    Context context;

    public BlogRepository(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        blogsRef = database.getReference().child("blogs");
        optionAll = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef, Blog.class)
                .build();
        optionPublished = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Published"), Blog.class)
                .build();
        optionDrafts = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Drafts"), Blog.class)
                .build();
        optionTrashed = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsRef.orderByChild("status").equalTo("Trashed"), Blog.class)
                .build();
    }

    public void addNote(String title, String content, String createTime, String userID, int likeNumber, int viewNumber, String category, String status) {

        String id = blogsRef.push().getKey();
        blogsRef.child(id).setValue(new Blog(id, title, content, createTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context,"Add blog successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"Add blog fail!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public Blog getBlogById(String id){
        DatabaseReference getRef = database.getReference().child("blogs").child(id);
        final Blog[] data = {null};
        getRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data[0] = dataSnapshot.getValue(Blog.class);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(context,"Id not exits",Toast.LENGTH_SHORT);
            }
        });
        return data[0];
    }
    public void updateNote(String id, String title, String content, String createTime, String userID, int likeNumber, int viewNumber, String category, String status) {
        blogsRef.child(id).setValue(new Blog(id, title, content, createTime, userID, likeNumber, viewNumber, category, status))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(context,"Edit blog successful!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context,"Edit blog fail!", Toast.LENGTH_SHORT).show();
                    }
                }
        });
    }
}
